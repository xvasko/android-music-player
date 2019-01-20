package com.matejvasko.player.paging;

import com.matejvasko.player.models.Song;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class SongDataSourceFactory extends DataSource.Factory<Integer, Song> {
    @NonNull
    @Override
    public DataSource<Integer, Song> create() {
        return new SongDataSource();
    }
}
