package com.matejvasko.player.firebase;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.authentication.Authentication;

import androidx.annotation.NonNull;

public class FirebaseDatabaseManager {

    private static DatabaseReference rootDatabase =  FirebaseDatabase.getInstance().getReference();
    public static DatabaseReference currentUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(Authentication.getCurrentUserUid());

    public static void retrieveCurrentUserData(final FirebaseDatabaseManagerCallback callback) {

        rootDatabase.child("users").child(Authentication.getCurrentUserUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("email", email);
                bundle.putString("thumb_image", thumbImage);

                callback.onResult(bundle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
