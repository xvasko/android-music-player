package com.matejvasko.player.models;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Song {

    public String id;
    public boolean isFromAlbum;
    public String title;
    public String artist;
    public String albumArtUri;
    public long duration;
    public int cursorPosition;

    public Song(String id, boolean isFromAlbum, String title, String artist, String albumArtUri, long duration, int cursorPosition) {
        this.id = id;
        this.isFromAlbum = isFromAlbum;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.duration = duration;
        this.cursorPosition = cursorPosition;
    }

    public static DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.equals(newItem);
        }
    };

}
