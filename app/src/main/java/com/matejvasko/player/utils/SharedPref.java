package com.matejvasko.player.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.matejvasko.player.App;
import com.matejvasko.player.models.Song;

public class SharedPref {

    private static final String SAVED_SONG_SHARED_PREFERENCES = "saved_song_shared_preferences";

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
        String json = sharedPreferences.getString("SongJSON", null);
        return gson.fromJson(json, Song.class);
    }

    public void setShuffle(boolean isShuffle) {
        editor.putBoolean("isShuffle", isShuffle);
        editor.commit();
    }

    public boolean isShuffle() {
        return sharedPreferences.getBoolean("isShuffle", false);
    }

    public void setBottomSheetState(int bottomSheetState) {
        editor.putInt("bottomSheetState", bottomSheetState);
        editor.commit();
    }

    public int getBottomSheetState() {
        return sharedPreferences.getInt("bottomSheetState", BottomSheetBehavior.STATE_COLLAPSED);
    }

}
