package com.matejvasko.player;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;

import androidx.annotation.NonNull;

public class App extends Application {

    private static final String TAG = "App";
    
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        appContext = this;
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static Context getAppContext() {
        return appContext;
    }
}
