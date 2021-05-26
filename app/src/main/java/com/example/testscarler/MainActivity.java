package com.example.testscarler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testscarler.adapter.LogerAdapter;
import com.example.testscarler.model.SocketStatusPojo;
import com.example.testscarler.model.Subscriber;
import com.example.testscarler.model.Ticker;
import com.example.testscarler.services.GdaxServices;
import com.example.testscarler.viewmodel.MainActivityViewModel;
import com.tinder.scarlet.Lifecycle;
import com.tinder.scarlet.Scarlet;
import com.tinder.scarlet.ShutdownReason;
import com.tinder.scarlet.Stream;
import com.tinder.scarlet.WebSocket;
import com.tinder.scarlet.lifecycle.LifecycleRegistry;
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle;
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter;
import com.tinder.scarlet.retry.LinearBackoffStrategy;
import com.tinder.scarlet.websocket.okhttp.OkHttpClientUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.reactivex.annotations.NonNull;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private String logD = "observ";
    private TextView bitcoinData;
    private Button btnSocketStartController,btnSocketStopController;
    private TextView txtTimeReceived;
    private LifecycleRegistry lifecycleRegistry;
    private GdaxServices gdaxServices;
    private MainActivityViewModel mMainViewModel;
    private RecyclerView mRecyclerSocket;
    private LogerAdapter mAdapterLoger;
    private List<SocketStatusPojo> mListSocketData;
    private String mAllSummaryTraf = "";

    private String applicationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "TestScarlet::wakelockTag");
        wakeLock.acquire();

        initRecyclerData();

        btnSocketStartController = findViewById(R.id.btn_start_socket);
        btnSocketStopController = findViewById(R.id.btn_stop_socket);
        txtTimeReceived = findViewById(R.id.txt_receive_time);

        btnSocketStartController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initObservers();
                //mMainViewModel.startService();
            }
        });

        btnSocketStopController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainViewModel.stopService();
            }
        });


        initObservers();

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);

        //check();
    }

    private void check(){
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        PackageManager packageManager = getPackageManager();
        try {
            applicationId = String.valueOf(packageManager.getApplicationInfo("com.example.testscarler", PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    //Init recycler for get data from socket
    private void initRecyclerData(){
        mRecyclerSocket = findViewById(R.id.rcv_soket_status);

        if (mRecyclerSocket!=null){
            mListSocketData = new ArrayList<>();
            mRecyclerSocket.setLayoutManager(new LinearLayoutManager(this));
            mAdapterLoger = new LogerAdapter(this, mListSocketData);
            mRecyclerSocket.setAdapter(mAdapterLoger);
        }
    }

    private void initObservers(){
        mMainViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        if (mMainViewModel!=null){
            //Observer to get last time get data
            mMainViewModel.getTimeLastGetData().observe(this, socketData ->{
                if (socketData!=null){
                    txtTimeReceived.setText(socketData.getmTime());
                    mListSocketData.add(socketData);
                }else{
                    mListSocketData.add(new SocketStatusPojo("null","null","null"));
                }
                mAdapterLoger.notifyDataSetChanged();
                mRecyclerSocket.scrollToPosition(mAdapterLoger.getItemCount()-1);
            });

            mMainViewModel.getAllTraff().observe(this, value->{
                mAllSummaryTraf = value;
            });
        }
    }

    private void saveLogData(){
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File directory = Environment.getStorageDirectory();
        String filename = "logScarlet.txt";
        File file = new File(directory,filename);

        StringBuilder builder = new StringBuilder();

        try {
            FileOutputStream fos = new FileOutputStream(file);

            for(SocketStatusPojo g: mListSocketData) {
               builder.append(g.getmTime()).append(" Download: ").append(g.getmDownload()).append(" Upload: ").append(g.getmUpload()).append("\n");
            }

            builder.append(mAllSummaryTraf);

            fos.write(builder.toString().getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLogData();
    }
}