package com.matejvasko.player.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.matejvasko.player.App;
import com.matejvasko.player.models.Song;

public class SharedPref {

    private static final String SAVED_SONG_SHARED_PREFERENCES = "saved_song_shared_preferences";
    private static final String CURRENT_ALBUM_ID = "current_album_id";
    private static final String CURRENT_SONG_POSITION = "current_song_position";
    private static final String CURRENT_SONG_DURATION = "current_song_duration";

    private static volatile SharedPref instance;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

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

    private SharedPref() {
        sharedPreferences = App.getAppContext().getSharedPreferences(SAVED_SONG_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    public void setSong(Song song) {
        editor.putString("SongJSON", gson.toJson(song));
        editor.commit();
    }

    public Song getSong() {
        String json = sharedPreferences.getString("SongJSON", "");
        return gson.fromJson(json, Song.class);
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
