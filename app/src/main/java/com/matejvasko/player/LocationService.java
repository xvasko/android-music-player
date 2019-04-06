package com.matejvasko.player;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.utils.UtilsCallback;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient fusedLocationClient;
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("player")
                    .setContentText("You are now broadcasting your position").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
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
                            fusedLocationClient.requestLocationUpdates(locationRequestHighAccuracy, new LocationCallback() {
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
                                    },
                                    Looper.myLooper());
                            // end location pooling




                        }
                    }
                });
            }
        });



    }

}
