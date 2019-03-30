package com.matejvasko.player.paging;

import android.util.Log;

import com.matejvasko.player.models.Friend;
import com.matejvasko.player.utils.FirebaseRepository;

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
        Log.d(TAG, "loadInitial: requested load size: " + params.requestedLoadSize);
        Log.d(TAG, "loadInitial: requested initial key: " + params.requestedInitialKey);
        firebaseRepository.getFriends(params.requestedLoadSize, callback);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<Friend> callback) {
        Log.d(TAG, "loadAfter: ");
        Log.d(TAG, "loadAfter: requested load size: " + params.requestedLoadSize);
        Log.d(TAG, "loadAfter: key: " + params.key);
        firebaseRepository.getFriendsAfter(params.requestedLoadSize, params.key, callback);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<Friend> callback) {
        Log.d(TAG, "loadBefore: ");
        Log.d(TAG, "loadBefore: requested load size: " + params.requestedLoadSize);
        Log.d(TAG, "loadBefore: key: " + params.key);
        firebaseRepository.getFriendsBefore(params.requestedLoadSize, params.key, callback);
    }

    @NonNull
    @Override
    public String getKey(@NonNull Friend item) {
        return item.getName();
    }

}
