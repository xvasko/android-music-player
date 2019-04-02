package com.matejvasko.player.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private Button profileFriendRequestAction;

    private ProgressDialog progressDialog;

    private DatabaseReference userDatabase;
    private DatabaseReference friendReqDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference rootDatabase;
    private FirebaseUser currentUser;

    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requests");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        prepareUI();

        currentState = "not_friends";

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileName.setText(name);
                profileEmail.setText(email);

                if (!image.equals("default")) {
                    Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_perm_identity_black_24dp).into(profileImage);
                }

                friendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {
                            String request_type = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if (request_type.equals("received")) {
                                currentState = "request_received";
                                profileFriendRequestAction.setText("Accept Friend Request");
                            } else if (request_type.equals("sent")) {
                                currentState = "request_sent";
                                profileFriendRequestAction.setText("Cancel Friend Request");
                            }

                        } else {
                            friendDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        currentState = "friends";
                                        profileFriendRequestAction.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileFriendRequestAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentState.equals("not_friends")) {

                    String newNotificationId = rootDatabase.child("notifications").child(userId).push().getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", currentUser.getUid());
                    notificationData.put("type", "request");

                    Map pushMap = new HashMap<>();
                    pushMap.put("friend_requests/" + currentUser.getUid() + "/" + userId + "/request_type", "sent");
                    pushMap.put("friend_requests/" + userId + "/" + currentUser.getUid() + "/request_type", "received");
                    pushMap.put("notifications/" + userId + "/" + newNotificationId, notificationData);

                    rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                currentState = "request_sent";
                                profileFriendRequestAction.setText("Cancel Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend request sent successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed sending friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

                if (currentState.equals("request_sent")) {
                    friendReqDatabase.child(currentUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendReqDatabase.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        currentState = "not_friends";
                                        profileFriendRequestAction.setText("Send Friend Request");
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed cancelling friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                if (currentState.equals("request_received")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map pushMap = new HashMap();
                    pushMap.put("friends/" + currentUser.getUid() + "/" + userId, currentDate);
                    pushMap.put("friends/" + userId + "/" + currentUser.getUid(), currentDate);
                    pushMap.put("friend_requests/" + currentUser.getUid() + "/" + userId, null);
                    pushMap.put("friend_requests/" + userId + "/" + currentUser.getUid(), null);

                    rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                currentState = "friends";
                                profileFriendRequestAction.setText("Unfriend");
                                Toast.makeText(ProfileActivity.this, "Friend request accepted successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed accepting friend request", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

                if (currentState.equals("friends")) {

                    Map pushMap = new HashMap();
                    pushMap.put("friends/" + currentUser.getUid() + "/" + userId, null);
                    pushMap.put("friends/" + userId + "/" + currentUser.getUid(), null);

                    rootDatabase.updateChildren(pushMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                currentState = "not_friends";
                                profileFriendRequestAction.setText("Send Friend Request");
                                Toast.makeText(ProfileActivity.this, "Friend removed successfully", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed removing friend", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void prepareUI() {
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profileFriendRequestAction = findViewById(R.id.profile_friend_request_action_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading user data");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
}
