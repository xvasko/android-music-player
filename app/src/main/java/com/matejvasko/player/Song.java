package com.matejvasko.player;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Song {

    private int id;
    public String filePath;
    public String title;

    Song(String filePath, String title) {
        this.filePath = filePath;
        this.title = title;
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
