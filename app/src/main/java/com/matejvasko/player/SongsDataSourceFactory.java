package com.matejvasko.player;

import android.support.v4.media.MediaBrowserCompat;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class SongsDataSourceFactory  extends DataSource.Factory<Integer, MediaItemData> {

    private final MediaBrowserCompat mediaBrowser;

    public SongsDataSourceFactory(MediaBrowserCompat mediaBrowser) {
        this.mediaBrowser = mediaBrowser;
    }

    @NonNull
    @Override
    public DataSource<Integer, MediaItemData> create() {
        return new SongsDataSource(mediaBrowser);
    }
}
