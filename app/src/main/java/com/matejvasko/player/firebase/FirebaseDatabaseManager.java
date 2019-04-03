package com.matejvasko.player.firebase;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.authentication.Authentication;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseDatabaseManager {

    public static DatabaseReference rootDatabase =  FirebaseDatabase.getInstance().getReference();

    public static DatabaseReference getCurrentUserDatabase() {
        return FirebaseDatabase.getInstance().getReference().child("users").child(Authentication.getCurrentUserUid());
    }

    public static void getUserData(String userUid, final FirebaseDatabaseManagerCallback callback) {
        rootDatabase.child("users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("email", email);
                bundle.putString("image", image);
                bundle.putString("thumb_image", thumbImage);

                callback.onResult(bundle);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getFriendshipState(final String ofUser, final FirebaseDatabaseManagerCallback callback) {
        rootDatabase.child("friend_requests").child(Authentication.getCurrentUserUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(ofUser)) {
                    String request_type = dataSnapshot.child(ofUser).child("request_type").getValue().toString();
                    if (request_type.equals("received")) {
                        callback.onResult("request_received");
                    } else if (request_type.equals("sent")) {
                        callback.onResult("request_sent");
                    }
                } else {
                    rootDatabase.child("friends").child(Authentication.getCurrentUserUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(ofUser)) {
                                callback.onResult("friends");
                            } else {
                                callback.onResult("not_friends");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            callback.onFailure();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure();
            }
        });
    }

    public static void sendFriendRequest(String toUser, final FirebaseDatabaseManagerCallback callback) {
        String currentUser = Authentication.getCurrentUserUid();
        String newNotificationId = rootDatabase.child("notifications").child(toUser).push().getKey();

        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("from", currentUser);
        notificationData.put("type", "request");

        Map pushMap = new HashMap<>();
        pushMap.put("friend_requests/" + currentUser + "/" + toUser + "/request_type", "sent");
        pushMap.put("friend_requests/" + toUser + "/" + currentUser + "/request_type", "received");
        pushMap.put("notifications/" + toUser + "/" + newNotificationId, notificationData);

        rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
            }
        });
    }

    public static void acceptFriendRequest(String fromUser, final FirebaseDatabaseManagerCallback callback) {
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        Map pushMap = new HashMap();
        pushMap.put("friends/" + Authentication.getCurrentUserUid() + "/" + fromUser, currentDate);
        pushMap.put("friends/" + fromUser + "/" + Authentication.getCurrentUserUid(), currentDate);
        pushMap.put("friend_requests/" + Authentication.getCurrentUserUid() + "/" + fromUser, null);
        pushMap.put("friend_requests/" + fromUser + "/" + Authentication.getCurrentUserUid(), null);

        rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure();
                }
            }
        });
    }

    public static void cancelFriendRequest(final String toUser, final FirebaseDatabaseManagerCallback callback) {
        rootDatabase.child("friend_requests").child(Authentication.getCurrentUserUid()).child(toUser).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    rootDatabase.child("friend_requests").child(toUser).child(Authentication.getCurrentUserUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task1) {
                           if (task1.isSuccessful()) {
                               callback.onSuccess();
                           } else {
                               callback.onFailure();
                           }
                        }
                    });
                } else {
                    callback.onFailure();
                }
            }
        });
    }

    public static void unfriend(String user, final FirebaseDatabaseManagerCallback callback) {
        Map pushMap = new HashMap();
        pushMap.put("friends/" + Authentication.getCurrentUserUid() + "/" + user, null);
        pushMap.put("friends/" + user + "/" + Authentication.getCurrentUserUid(), null);

        rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    callback.onSuccess();

                } else {
                    callback.onFailure();
                }
            }
        });
    }

    public static void setUserOnline(boolean isOnline) {
        if (Authentication.getCurrentUser() != null) {
            FirebaseDatabaseManager.getCurrentUserDatabase().child("online").setValue(isOnline);
        }
    }
    
}
