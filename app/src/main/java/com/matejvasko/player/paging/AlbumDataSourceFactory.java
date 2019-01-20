package com.matejvasko.player.paging;

import com.matejvasko.player.models.Album;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class AlbumDataSourceFactory extends DataSource.Factory<Integer, Album> {
    @NonNull
    @Override
    public DataSource<Integer, Album> create() {
        return new AlbumDataSource();
    }
}
