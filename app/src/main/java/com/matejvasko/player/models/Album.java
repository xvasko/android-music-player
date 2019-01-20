package com.matejvasko.player.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Album {

    public String id;
    public String title;
    public String artist;
    public Uri albumArtUri;
    public int cursorPosition;

    public Album(String id, String title, String artist, Uri albumArtUri, int cursorPosition) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.cursorPosition = cursorPosition;
    }

    public static DiffUtil.ItemCallback<Album> DIFF_CALLBACK = new DiffUtil.ItemCallback<Album>() {
        @Override
        public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
            return oldItem.equals(newItem);
        }
    };
}
