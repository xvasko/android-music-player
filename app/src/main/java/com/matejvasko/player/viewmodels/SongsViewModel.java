package com.matejvasko.player.viewmodels;

import android.support.v4.media.MediaBrowserCompat;

import com.matejvasko.player.Song;
import com.matejvasko.player.SongsDataSourceFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class SongsViewModel extends ViewModel {


    public LiveData<PagedList<Song>> getSongs(MediaBrowserCompat mediaBrowser) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new SongsDataSourceFactory(mediaBrowser), config).build();
    }
}
