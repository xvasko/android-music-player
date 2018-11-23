package com.matejvasko.player.viewmodels;

import android.support.v4.media.MediaBrowserCompat;

import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.paging.MediaItemDataSource;
import com.matejvasko.player.paging.MediaItemDataSourceFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MainActivityViewModel extends ViewModel {

    public LiveData<PagedList<MediaItemData>> getSongs(MediaBrowserCompat mediaBrowser) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new MediaItemDataSourceFactory(mediaBrowser, MediaItemDataSource.SONG_DATA_SOURCE), config).build();
    }

    public LiveData<PagedList<MediaItemData>> getAlbums(MediaBrowserCompat mediaBrowser) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new MediaItemDataSourceFactory(mediaBrowser, MediaItemDataSource.ALBUM_DATA_SOURCE), config).build();
    }

}
