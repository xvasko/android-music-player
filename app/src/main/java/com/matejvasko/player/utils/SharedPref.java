package com.matejvasko.player.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.matejvasko.player.App;
import com.matejvasko.player.MediaItemData;

public class SharedPref {

    private static final String SAVED_SONG_SHARED_PREFERENCES = "saved_song_shared_preferences";
    private static final String CURRENT_ALBUM_ID = "current_album_id";
    private static final String CURRENT_SONG_POSITION = "current_song_position";
    private static final String CURRENT_SONG_DURATION = "current_song_duration";

    private static volatile SharedPref instance;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static SharedPref getInstance() {
        if (instance == null) {
            synchronized (SharedPref.class) {
                if (instance == null) {
                    instance = new SharedPref();
                }
            }
        }
        return instance;
    }

    SharedPref() {
        sharedPreferences = App.getAppContext().getSharedPreferences(SAVED_SONG_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setCurrentSong(MediaItemData mediaItemData) {
        editor.putInt("cursor_position", mediaItemData.cursorPosition);
        editor.putBoolean("is_from_album", mediaItemData.isFromAlbum);
        editor.commit();
    }

    public int getCurrentSongCursorPosition() {
        return sharedPreferences.getInt("cursor_position", -1);
    }

    public boolean isCurrentSongFromAlbum() {
        return sharedPreferences.getBoolean("is_from_album", false);
    }

    public void setCurrentAlbumId(String albumId) {
        editor.putString(CURRENT_ALBUM_ID, albumId);
        editor.apply();
    }

    public String getCurrentAlbumId() {
        return sharedPreferences.getString(CURRENT_ALBUM_ID, null);
    }

    public void setCurrentSongPosition(long position) {
        editor.putLong(CURRENT_SONG_POSITION, position);
        editor.commit();
    }

    public long getCurrentSongPosition() {
        return sharedPreferences.getLong(CURRENT_SONG_POSITION, 0);
    }

    public void setCurrentSongDuration(long duration) {
        editor.putLong(CURRENT_SONG_DURATION, duration);
        editor.commit();
    }

    public long getCurrentSongDuration() {
        return sharedPreferences.getLong(CURRENT_SONG_DURATION, 0);
    }

}
