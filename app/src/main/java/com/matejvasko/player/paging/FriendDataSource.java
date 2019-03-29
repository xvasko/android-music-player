package com.matejvasko.player.paging;

import android.util.Log;

import com.matejvasko.player.models.Friend;
import com.matejvasko.player.utils.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

public class FriendDataSource extends ItemKeyedDataSource<String, Friend> {

    private static final String TAG = "FriendDataSource";

    private FirebaseRepository firebaseRepository;

    FriendDataSource() {
        firebaseRepository = new FirebaseRepository();
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<Friend> callback) {
        Log.d(TAG, "loadInitial: ");
        firebaseRepository.getFriends(params.requestedLoadSize, new FirebaseRepository.MyCallback() {
            @Override
            public void onResult(List<Friend> friends) {
                System.out.println("onResult inside datasource friends size: " + friends.size());
                callback.onResult(friends);
            }
        });
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<Friend> callback) {
        Log.d(TAG, "loadAfter: ");

    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<Friend> callback) {
        Log.d(TAG, "loadBefore: ");
    }

    @NonNull
    @Override
    public String getKey(@NonNull Friend item) {
        return item.getName();
    }

}
