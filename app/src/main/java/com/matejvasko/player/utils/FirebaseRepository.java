package com.matejvasko.player.utils;

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

public class FirebaseRepository {

    private DatabaseReference friendDatabase;
    private FirebaseUser currentUser;

    public interface MyCallback {
        void onResult(List<Friend> friends);
    }

    public FirebaseRepository() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUser.getUid());
    }

    public void getFriends(int count, final MyCallback callback) {
        System.out.println("getFriends()");
        friendDatabase.orderByKey().limitToFirst(count).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.println("onDataChange()");
                List<Friend> friends = new ArrayList<>();

                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    Friend friend = new Friend();
                    friend.setName(friendSnapshot.getKey());
                    friends.add(friend);
                }
                System.out.println("friends.size(): " + friends.size());
                callback.onResult(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
