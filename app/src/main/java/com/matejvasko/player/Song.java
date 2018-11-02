package com.matejvasko.player;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Song {

    public int id;
    public String data;
    public String title;
    public String artist;
    public Uri iconUri;
    public long duration;

    public static class Builder {
        private int id;
        private String data;
        private String title;
        private String artist;
        private Uri iconUri;
        private long duration;

        public Builder(int id) {
            this.id = id;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setIconUri(Uri iconUri) {
            this.iconUri = iconUri;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Song build() {
            Song song = new Song();
            song.id = this.id;
            song.data = this.data;
            song.title = this.title;
            song.artist = this.artist;
            song.iconUri = this.iconUri;
            song.duration = this.duration;

            return song;
        }
    }

    private Song() {

    }

    public static DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        Song song = (Song) obj;
        return song.id == this.id;
    }

}
