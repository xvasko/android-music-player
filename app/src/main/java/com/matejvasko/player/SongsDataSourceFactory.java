package com.matejvasko.player;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class SongsDataSourceFactory  extends DataSource.Factory<Integer, Song> {

    private final MediaBrowserCompat mediaBrowser;

    public SongsDataSourceFactory(MediaBrowserCompat mediaBrowser) {
        this.mediaBrowser = mediaBrowser;
    }

    @NonNull
    @Override
    public DataSource<Integer, Song> create() {
        return new SongsDataSource(mediaBrowser);
    }
}
