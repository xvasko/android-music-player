package com.matejvasko.player;

import android.app.Application;
import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;

public class App extends Application {
    private static Context appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
    public static Context getAppContext() {
        return appContext;
    }
}
