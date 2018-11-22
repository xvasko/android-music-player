package com.matejvasko.player.viewmodels;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.matejvasko.player.Album;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.Song;
import com.matejvasko.player.SongsDataSourceFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Song> song;

    public MutableLiveData<Song> getNowPlaying() {
        if (song == null) {
            song = new MutableLiveData<>();
        }
        return song;
    }

    public LiveData<PagedList<MediaItemData>> getSongs(MediaBrowserCompat mediaBrowser) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPageSize(20)
                .build();
        return new LivePagedListBuilder<>(new SongsDataSourceFactory(mediaBrowser), config).build();
    }

    public void mediaItemClicked(Song song) {
        if (song.isFromSongTab()) {
            System.out.println("mediaItemClicked from song tab");
            getNowPlaying().setValue(song);
        } else {
            System.out.println("mediaItemClicked from album tab");
        }
    }

}
