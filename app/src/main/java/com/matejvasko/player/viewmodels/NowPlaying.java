package com.matejvasko.player.viewmodels;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;

public class NowPlaying {

    private static MutableLiveData<Song> song;


    public static MutableLiveData<Song> getNowPlaying() {
        if (song == null) {
            song = new MutableLiveData<>();
        }
        return song;
    }


    public static class Song {
        public String songUri;
        public Bitmap albumArt;
        public String songTitle;
        public String artistTitle;

        public Song(String songUri, Bitmap albumArt, String songTitle, String artistTitle) {
            this.songUri = songUri;
            this.albumArt = albumArt;
            this.songTitle = songTitle;
            this.artistTitle = artistTitle;
        }
    }

}
