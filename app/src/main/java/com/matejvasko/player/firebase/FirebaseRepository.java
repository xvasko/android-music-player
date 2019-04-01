package com.matejvasko.player.firebase;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.models.Friend;

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

    public void getFriends(int count, final ItemKeyedDataSource.LoadInitialCallback<Friend> callback) {
        Log.d(TAG, "getFriends: ");
        friendDatabase.orderByKey().limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriends() - onDataChange: ");
                List<Friend> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = new Friend();
                    friend.setUid(friendSnapshot.getKey());
                    friends.add(friend);
                }
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFriendsAfter(int count, String afterKey, final ItemKeyedDataSource.LoadCallback<Friend> callback) {
        Log.d(TAG, "getFriendsAfter: ");
        friendDatabase.orderByKey().startAt(afterKey).limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriendsAfter() - onDataChange: ");
                List<Friend> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = new Friend();
                    friend.setUid(friendSnapshot.getKey());
                    friends.add(friend);
                }

                System.out.println("friends after size: " + friends.size());
                System.out.println("uid: " + friends.get(0).getUid());
                friends.remove(0);
                System.out.println("friends after cut size: " + friends.size());
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getFriendsBefore(int count, String beforeKey, final ItemKeyedDataSource.LoadCallback<Friend> callback) {
        Log.d(TAG, "getFriendsBefore: ");
        friendDatabase.orderByKey().endAt(beforeKey).limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriendsBefore() - onDataChange: ");
                List<Friend> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = new Friend();
                    friend.setUid(friendSnapshot.getKey());
                    friends.add(friend);
                }

                System.out.println("friends before size: " + friends.size());
                System.out.println("uid: " + friends.get(0).getUid());
                friends.remove(friends.size() - 1);
                System.out.println("friends before cut size: " + friends.size());
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
