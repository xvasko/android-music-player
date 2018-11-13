package com.matejvasko.player.utils;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;

import com.matejvasko.player.Song;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final String SONG_DURATION = "song_duration";

    public static List<MediaBrowserCompat.MediaItem> mapToMediaItems(List<Song> songs) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            Bundle extras = new Bundle();
            extras.putLong(SONG_DURATION, song.duration);
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
                    .build();
            songs.add(song);
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

}
