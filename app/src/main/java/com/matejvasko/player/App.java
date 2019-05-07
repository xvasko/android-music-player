package com.matejvasko.player;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class App extends Application {

    private static final String TAG = "App";

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        appContext = this;
    }

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate: ");
        super.onTerminate();
    }
}
