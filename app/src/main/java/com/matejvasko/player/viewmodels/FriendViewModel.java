package com.matejvasko.player.viewmodels;

import com.matejvasko.player.models.Friend;
import com.matejvasko.player.paging.FriendDataSourceFactory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public class FriendViewModel extends ViewModel {

    public LiveData<PagedList<Friend>> getFriends() {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(2)
                .build();
        return new LivePagedListBuilder<>(new FriendDataSourceFactory(), config).build();
    }

}
