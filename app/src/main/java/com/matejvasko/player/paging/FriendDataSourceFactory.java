package com.matejvasko.player.paging;

import com.matejvasko.player.models.User;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

public class FriendDataSourceFactory extends DataSource.Factory<String, User> {
    @NonNull
    @Override
    public ItemKeyedDataSource<String, User> create() {
        return new FriendDataSource();
    }
}
