package com.matejvasko.player.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.matejvasko.player.App;

import java.util.HashMap;
import java.util.Map;

public class Utils {

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
