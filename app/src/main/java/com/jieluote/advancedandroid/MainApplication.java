package com.jieluote.advancedandroid;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getName();
    private static MainApplication sMainApplication;

    public static MainApplication getInstance() {
        return sMainApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sMainApplication = this;
        Log.d(TAG, "MainApplication onCreate");
    }
}
