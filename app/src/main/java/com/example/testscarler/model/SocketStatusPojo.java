package com.example.testscarler.model;

public class SocketStatusPojo {

    private String mTime;
    private String mDownload;
    private String mUpload;

    public SocketStatusPojo(String mTime, String mDownload, String mUpload) {
        this.mTime = mTime;
        this.mDownload = mDownload;
        this.mUpload = mUpload;
    }
    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmDownload() {
        return mDownload;
    }

    public void setmDownload(String mDownload) {
        this.mDownload = mDownload;
    }

    public String getmUpload() {
        return mUpload;
    }

    public void setmUpload(String mUpload) {
        this.mUpload = mUpload;
    }

}
