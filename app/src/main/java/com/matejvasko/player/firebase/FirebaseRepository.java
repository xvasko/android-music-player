package com.matejvasko.player.firebase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.models.User;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.ItemKeyedDataSource;

public class FirebaseRepository {

    private static final String TAG = "FirebaseRepository";

    private DatabaseReference friendDatabase;
    private FirebaseUser currentUser;

    public FirebaseRepository() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUser.getUid());
    }

    public void getFriends(int count, final ItemKeyedDataSource.LoadInitialCallback<User> callback) {
        Log.d(TAG, "getFriends: ");
        friendDatabase.orderByKey().limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriends() - onDataChange: ");
                final List<User> users = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = new User();
                    user.setUid(userSnapshot.getKey());
                    users.add(user);
                }
                callback.onResult(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFriendsAfter(int count, String afterKey, final ItemKeyedDataSource.LoadCallback<User> callback) {
        Log.d(TAG, "getFriendsAfter: ");
        friendDatabase.orderByKey().startAt(afterKey).limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriendsAfter() - onDataChange: ");
                List<User> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    User friend = new User();
                    friend.setUid(friendSnapshot.getKey());
                    friends.add(friend);
                }

                friends.remove(0);
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFriendsBefore(int count, String beforeKey, final ItemKeyedDataSource.LoadCallback<User> callback) {
        Log.d(TAG, "getFriendsBefore: ");
        friendDatabase.orderByKey().endAt(beforeKey).limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriendsBefore() - onDataChange: ");
                List<User> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    User friend = new User();
                    friend.setUid(friendSnapshot.getKey());
                    friends.add(friend);
                }

                friends.remove(friends.size() - 1);
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
