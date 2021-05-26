package com.example.testscarler.repository;

import android.annotation.SuppressLint;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testscarler.model.SocketStatusPojo;
import com.example.testscarler.model.Subscriber;
import com.example.testscarler.model.Ticker;
import com.example.testscarler.services.GdaxServices;
import com.tinder.scarlet.Lifecycle;
import com.tinder.scarlet.Scarlet;
import com.tinder.scarlet.Stream;
import com.tinder.scarlet.WebSocket;
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter;
import com.tinder.scarlet.retry.LinearBackoffStrategy;
import com.tinder.scarlet.websocket.okhttp.OkHttpClientUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SocketRepository {

    private String logD = "observ";
    private String GDAX_URL = "wss://ws-feed.gdax.com";
    private OkHttpClient okHttpClient;
    private Scarlet scarlet;
    private GdaxServices gdaxServices;
    private Lifecycle mLifecycle;
    private MutableLiveData<SocketStatusPojo> mReceiveData;
    private MutableLiveData<String> mSummaryData;
    private HttpLoggingInterceptor logging;
    private PackageManager packageManager;
    private List<ApplicationInfo> appList;
    private long lastDownload = 0;
    private long lastUpload = 0;

    private long allDownload = 0;
    private long allUpload = 0;

    public SocketRepository(Lifecycle lifecycle, PackageManager manager) {
        this.mLifecycle = lifecycle;
        mReceiveData = new MutableLiveData<>();
        mSummaryData = new MutableLiveData<>();
        this.packageManager = manager;
        initSocketListener();
    }

    private void initSocketListener(){

        logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        scarlet = new Scarlet.Builder()
                .webSocketFactory(OkHttpClientUtils.newWebSocketFactory(okHttpClient,GDAX_URL))
                .addMessageAdapterFactory(new GsonMessageAdapter.Factory())
                .backoffStrategy(new LinearBackoffStrategy(2000))
                .lifecycle(mLifecycle)
                //.addStreamAdapterFactory(new RxJava2StreamAdapterFactory())
                .build();

        gdaxServices = scarlet.create(GdaxServices.class);

        if (gdaxServices !=null){

            Subscriber subscriber = new Subscriber();
            subscriber.setProduct_ids(List.of("BTC-USD"));
            subscriber.setChannels(List.of("ticker"));

            gdaxServices.observeWebSocketEvent().start(new Stream.Observer<WebSocket.Event>() {
                @Override
                public void onNext(WebSocket.Event event) {
                    //Log.d(logD,"Found event: " + event);
                    try{
                        if (event instanceof WebSocket.Event.OnConnectionOpened){
                            gdaxServices.sendSubscribe(subscriber);
                        }

                        if (event instanceof WebSocket.Event.OnConnectionClosed){
                            Log.d(logD,"Connection closed ");
                        }
//
//                    if (event instanceof WebSocket.Event.OnConnectionClosing){
//
//                    }
//
//                    if (event instanceof WebSocket.Event.OnConnectionFailed){
//
//                    }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(@NonNull Throwable throwable) {
                    Log.d(logD,"observer Thr " + throwable);
                }

                @Override
                public void onComplete() {
                    Log.d(logD,"onComplete");
                }
            });

            gdaxServices.observeTicker().start(new Stream.Observer<Ticker>() {
                @SuppressLint("SimpleDateFormat")
                @Override
                public void onNext(Ticker ticker) {

                    //bitcoinData.setText("Bitcoin. Price:" + ticker.getPrice() +". Time: "+ ticker.getTime());
                    Log.d(logD,"Loging:" + logging.toString());
                    Log.d(logD,"Bitcoin. Price:" + ticker.getPrice() +". Time: "+ ticker.getTime());
                    String timeStamp = new SimpleDateFormat("yy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

                    appList = packageManager.getInstalledApplications(0);

                    for (ApplicationInfo appInfo : appList) {
                        String appLabel = (String) packageManager.getApplicationLabel(appInfo);
                        int uid = appInfo.uid;
                        if (appLabel.contains("TestScarler")){

                            String rx = getFormatDownloadSize(TrafficStats.getUidRxBytes(uid));
                            String tx = getFormatUploadSize(TrafficStats.getUidTxBytes(uid));

                            lastDownload = TrafficStats.getUidRxBytes(uid);
                            lastUpload = TrafficStats.getUidTxBytes(uid);

                            if (allDownload == 0 && allUpload == 0){
                                allDownload = TrafficStats.getUidRxBytes(uid);
                                allUpload = TrafficStats.getUidTxBytes(uid);

                                rx = getFormatDownloadSize(TrafficStats.getUidRxBytes(uid) - allDownload);
                                tx = getFormatUploadSize(TrafficStats.getUidTxBytes(uid) - allUpload);
                            }


                            if (!rx.isEmpty() && !tx.isEmpty()){
                                mReceiveData.postValue(new SocketStatusPojo(timeStamp,rx,tx));
                            }

                            mSummaryData.postValue(getAllDataString(TrafficStats.getUidRxBytes(uid) - allDownload,TrafficStats.getUidTxBytes(uid) - allUpload));
                            //Log.d(logD, "app:" + appLabel + "Uid: " + uid  + " Download: " + rx + " Upload: " + tx);
                        }
                    }
                }

                @Override
                public void onError(Throwable t) {
                    @SuppressLint("SimpleDateFormat")
                    String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    mReceiveData.postValue(new SocketStatusPojo(timeStamp,"Error data",""));
                    Log.d(logD,"onError Ticker" + t);
                }

                @Override
                public void onComplete() {
                    Log.d(logD,"onComplete Ticker");
                }
            });
        }
    }

    private String getFormatDownloadSize(long bytes) {
        long bt = bytes - lastDownload;
        long kb = bt/1024;
        if (kb >= 1){
             return kb + " Kb";
        }else if(bt > 0){
             return bt + " b";
        }else{
            return "";
        }
//        long bt = bytes - lastDownload;
//        long kilobytes = bt/ 1024;
//        Log.d(logD,"Byte: " + bytes + " last Download: " + lastDownload + "Bt: " + bt);
//        long megabytes = kilobytes / 1024;
//        return kilobytes + " Kb";
    }

    private String getFormatUploadSize(long bytes) {
        long bt = bytes - lastUpload;
        long kb = bt/1024;
        if (kb >= 1){
            return kb + " Kb";
        }else if(bt > 0){
            return bt + " b";
        }else{
            return "";
        }
        //long megabytes = kb / 1024;
        //Log.d(logD,"Bt: " + bt +"Byte: " + bytes + " last Upload: " + lastUpload);
        //return kb + " Kb";
    }

    private String getAllDataString(long down, long upl){
        long kbD = down/1024;
        long kbU = upl/1024;

        return "Summary: [ Download: " + kbD + "Kb. Upload: " + kbU + " Kb ]";
    }

    public LiveData<SocketStatusPojo> getTime(){
        return mReceiveData;
    }

    public LiveData<String> getAllData(){
        return mSummaryData;
    }
}
