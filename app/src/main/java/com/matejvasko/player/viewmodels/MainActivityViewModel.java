package com.matejvasko.player.viewmodels;

import com.matejvasko.player.models.Album;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.paging.AlbumDataSourceFactory;
import com.matejvasko.player.paging.SongDataSourceFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MainActivityViewModel extends ViewModel {

    public LiveData<PagedList<Song>> getSongs() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new SongDataSourceFactory(), config).build();
    }

    public LiveData<PagedList<Album>> getAlbums() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new AlbumDataSourceFactory(), config).build();
    }

}
