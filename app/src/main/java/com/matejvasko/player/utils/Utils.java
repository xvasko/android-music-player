package com.matejvasko.player.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;

import com.matejvasko.player.App;
import com.matejvasko.player.MediaItemData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static List<MediaItemData> mapToMediaItemData(List<MediaBrowserCompat.MediaItem> children) {
        List<MediaItemData> mediaItemsData = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : children) {
            MediaItemData mediaItemData = new MediaItemData(
                    mediaItem.getDescription().getMediaId(),
                    mediaItem.getDescription().getTitle().toString(),
                    mediaItem.getDescription().getSubtitle().toString(),
                    mediaItem.getDescription().getIconUri(),
                    mediaItem.isBrowsable());
            mediaItemsData.add(mediaItemData);
        }

        return mediaItemsData;
    }

    public static String millisecondsToString(long mills) {
        int seconds = (int) (mills / 1000) % 60 ;
        int minutes = (int) ((mills / (1000*60)) % 60);
//        int hours   = (int) ((mills / (1000*60*60)) % 24);

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

}
