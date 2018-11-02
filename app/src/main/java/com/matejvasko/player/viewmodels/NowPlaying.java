package com.matejvasko.player.viewmodels;

import com.matejvasko.player.Song;

import androidx.lifecycle.MutableLiveData;

public class NowPlaying {

    private static MutableLiveData<Song> song;

    public static MutableLiveData<Song> getNowPlaying() {
        if (song == null) {
            song = new MutableLiveData<>();
        }
        return song;
    }

}
