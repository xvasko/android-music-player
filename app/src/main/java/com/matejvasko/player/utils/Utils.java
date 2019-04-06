package com.matejvasko.player.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.matejvasko.player.App;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class Utils {

    public static void getCurrentLocationOnce(final UtilsCallback callback) {
        LocationManager locationManager = (LocationManager) App.getAppContext().getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(App.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                callback.onResult(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        locationManager.requestSingleUpdate(criteria, locationListener, null);
    }

    public static String millisecondsToString(long mills) {
        int seconds = (int) (mills / 1000) % 60 ;
        int minutes = (int) ((mills / (1000*60)) % 60);
//        int hours   = (int) ((mills / (1000*60*60)) % 24); // TODO prepare for files longer than 59:59

        if (seconds < 10) {
            return minutes + ":0" + seconds;
        }

        return minutes + ":" + seconds;
    }

    private static Map<Uri, Bitmap> map = new HashMap<>();

    public static Bitmap getBitmapFromMediaStore(Uri iconUri) {
        if (map.containsKey(iconUri)) {
            return map.get(iconUri);
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(App.getAppContext().getContentResolver(), iconUri);
                map.put(iconUri, bitmap);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                map.put(iconUri, null);
                return null;
            }
        }
    }

    public static int densityPixelToPixel(int densityPixel) {
        final float scale = App.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (densityPixel * scale + 0.5f);
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getLastSeen(long time) {
        long now = System.currentTimeMillis();

        final long diff = now - time;
        if (diff < HOUR_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < DAY_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else {
            if (diff / DAY_MILLIS <= 9) {
                return diff / DAY_MILLIS + "d";
            } else {
                return "<9d";
            }
        }
    }

}
