package com.matejvasko.player.paging;

import com.matejvasko.player.models.Friend;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

public class FriendDataSourceFactory extends DataSource.Factory<String, Friend> {
    @NonNull
    @Override
    public ItemKeyedDataSource<String, Friend> create() {
        return new FriendDataSource();
    }
}
