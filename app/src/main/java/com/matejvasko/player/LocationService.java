package com.matejvasko.player;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.utils.UtilsCallback;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private final static long UPDATE_INTERVAL = 10 * 1000;
    private final static long FASTEST_INTERVAL = UPDATE_INTERVAL / 2;

    private Location userLocationDatabase;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Intent stopIntent = new Intent(this, StopServiceReceiver.class);
            stopIntent.setAction("STOP");
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 12, stopIntent, 0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .addAction(R.drawable.ic_skip_next_black_24dp, "STOP", stopPendingIntent)
                    .setContentTitle("player")
                    .setContentText("You are now broadcasting your position").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_STICKY;
    }


    private void getLocation() {
        final LocationRequest locationRequestHighAccuracy = new LocationRequest();
        locationRequestHighAccuracy
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        Utils.getCurrentLocationOnce(new UtilsCallback() {
            @Override
            public void onResult(final Location userLocationServiceStart) {
                final GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference("user_locations"));
                geoFire.setLocation(Authentication.getCurrentUserUid(), new GeoLocation(userLocationServiceStart.getLatitude(), userLocationServiceStart.getLongitude()), new GeoFire.CompletionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: " + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                            userLocationDatabase = userLocationServiceStart;




                            // start location pooling
                            locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    final Location newLocation = locationResult.getLastLocation();
                                    if (newLocation != null) {



                                        if (newLocation.distanceTo(userLocationDatabase) > 10f) {
                                            // save new location to DB
                                            geoFire.setLocation(Authentication.getCurrentUserUid(), new GeoLocation(newLocation.getLatitude(), newLocation.getLongitude()), new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {
                                                    if (error != null) {
                                                        Log.w(TAG, "onLocationResult:There was an error saving the new location to GeoFire: " + error);
                                                    } else {
                                                        userLocationDatabase = newLocation;
                                                        Log.d(TAG, "onLocationResult: latitude: " + newLocation.getLatitude());
                                                        Log.d(TAG, "onLocationResult: longitude: " + newLocation.getLongitude());
                                                        Log.d(TAG, "onLocationResult: New location saved on server successfully!");
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d(TAG, "onLocationResult: latitude: " + newLocation.getLatitude());
                                            Log.d(TAG, "onLocationResult: longitude: " + newLocation.getLongitude());
                                            Log.d(TAG, "onLocationResult: NOT SAVING TO THE DATABASE");
                                        }




                                    }
                                }
                            };

                            fusedLocationClient.requestLocationUpdates(locationRequestHighAccuracy, locationCallback,  Looper.myLooper());
                            // end location pooling




                        }
                    }
                });
            }
        });



    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);
        
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG, "onTrimMemory: ");
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory: ");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind: ");
        super.onRebind(intent);
    }


}
