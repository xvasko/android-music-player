package com.matejvasko.player.paging;

import android.support.v4.media.MediaBrowserCompat;

import com.matejvasko.player.MediaItemData;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;

public class MediaItemDataSourceFactory extends DataSource.Factory<Integer, MediaItemData> {

    private final MediaBrowserCompat mediaBrowser;
    private final int flag;

    public MediaItemDataSourceFactory(MediaBrowserCompat mediaBrowser, int flag) {
        this.mediaBrowser = mediaBrowser;
        this.flag = flag;
    }

    @NonNull
    @Override
    public DataSource<Integer, MediaItemData> create() {
        return new MediaItemDataSource(mediaBrowser, flag);
    }
}
