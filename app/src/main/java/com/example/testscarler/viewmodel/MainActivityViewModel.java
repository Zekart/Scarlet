package com.example.testscarler.viewmodel;

import android.app.Application;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testscarler.model.SocketStatusPojo;
import com.example.testscarler.repository.SocketRepository;
import com.tinder.scarlet.Lifecycle;
import com.tinder.scarlet.ShutdownReason;
import com.tinder.scarlet.lifecycle.LifecycleRegistry;
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle;

public class MainActivityViewModel extends AndroidViewModel {

    private SocketRepository repository;
    private LiveData<SocketStatusPojo> mTimeLastConnection = new MutableLiveData<>();;
    private LifecycleRegistry lifecycleRegistry;
    private final PackageManager packageManager;
    private LiveData<String> mSummary;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        lifecycleRegistry = new LifecycleRegistry(0L);
        lifecycleRegistry.onNext(Lifecycle.State.Started.INSTANCE);
        packageManager = application.getPackageManager();
        startService();
    }

    public LiveData<SocketStatusPojo> getTimeLastGetData(){

        System.out.println(mTimeLastConnection.getValue());

        return mTimeLastConnection;
    }

    public LiveData<String> getAllTraff(){
        return mSummary;
    }

    private Lifecycle getLifecycleCombined(){
        return AndroidLifecycle.ofApplicationForeground(getApplication()).combineWith(lifecycleRegistry);
    }

    public void stopService(){
        lifecycleRegistry.onNext(new Lifecycle.State.Stopped.WithReason(ShutdownReason.GRACEFUL));
    }

    public void startService(){
        lifecycleRegistry = new LifecycleRegistry(0L);
        lifecycleRegistry.onNext(Lifecycle.State.Started.INSTANCE);
        repository = new SocketRepository(getLifecycleCombined(),packageManager);
        mTimeLastConnection = repository.getTime();
        mSummary = repository.getAllData();
    }
}
