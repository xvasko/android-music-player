package com.matejvasko.player.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.matejvasko.player.App;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String SONG_DURATION = "song_duration";
    private static final String CURSOR_POSITION = "cursor_position";

    public static List<MediaBrowserCompat.MediaItem> mapToMediaItems(List<Song> songs) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            Bundle extras = new Bundle();
            extras.putLong(SONG_DURATION, song.duration);
            extras.putInt(CURSOR_POSITION, song.cursorPosition);
            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setMediaId(song.id + "")
                    .setMediaUri(Uri.parse(song.data))
                    .setTitle(song.title)
                    .setSubtitle(song.artist)
                    .setIconUri(song.iconUri)
                    .setExtras(extras)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescription, 0);
            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }

    public static List<Song> mapToSongs(List<MediaBrowserCompat.MediaItem> children) {
        List<Song> songs = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : children) {
            Song song = new Song.Builder(Integer.valueOf(mediaItem.getMediaId()))
                    .setData(mediaItem.getDescription().getMediaUri().toString())
                    .setTitle(mediaItem.getDescription().getTitle().toString())
                    .setArtist(mediaItem.getDescription().getSubtitle().toString())
                    .setIconUri(mediaItem.getDescription().getIconUri())
                    .setDuration(mediaItem.getDescription().getExtras().getLong(SONG_DURATION))
                    .setCursorPosition(mediaItem.getDescription().getExtras().getInt(CURSOR_POSITION))
                    .build();
            songs.add(song);
        }

        return songs;
    }




    public static List<MediaItemData> mapToMediaItemData(List<MediaBrowserCompat.MediaItem> children) {
        List<MediaItemData> songs = new ArrayList<>();
        for (MediaBrowserCompat.MediaItem mediaItem : children) {
            MediaItemData mediaItemData = new MediaItemData(
                    mediaItem.getDescription().getMediaId(),
                    mediaItem.getDescription().getTitle().toString(),
                    mediaItem.getDescription().getSubtitle().toString(),
                    mediaItem.getDescription().getIconUri(),
                    mediaItem.isBrowsable());
            songs.add(mediaItemData);
        }

        return songs;
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
