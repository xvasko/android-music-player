package com.matejvasko.player.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.R;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private Button profileFriendRequestAction;

    private ProgressDialog progressDialog;

    private String friendshipState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        prepareUI();

        friendshipState = "not_friends";

        FirebaseDatabaseManager.getUserData(userId, new FirebaseDatabaseManagerCallback() {
            @Override
            public void onResult(Bundle userDataBundle) {
                profileName.setText(userDataBundle.getString("name"));
                profileEmail.setText(userDataBundle.getString("email"));
                String image = userDataBundle.getString("image");
                if (!image.equals("default")) {
                    Glide.with(getApplicationContext()).load(image).placeholder(R.drawable.ic_perm_identity_black_24dp).into(profileImage);
                }
                FirebaseDatabaseManager.getFriendshipState(userId, new FirebaseDatabaseManagerCallback() {
                    @Override
                    public void onResult(String friendshipState) {
                        switch (friendshipState) {
                            case "not_friends":
                                ProfileActivity.this.friendshipState = friendshipState;
                                profileFriendRequestAction.setText("Send Friend Request");
                                break;
                            case "friends":
                                ProfileActivity.this.friendshipState = friendshipState;
                                profileFriendRequestAction.setText("Unfriend");
                                break;
                            case "request_received":
                                ProfileActivity.this.friendshipState = friendshipState;
                                profileFriendRequestAction.setText("Accept Friend Request");
                                break;
                            case "request_sent":
                                ProfileActivity.this.friendshipState = friendshipState;
                                profileFriendRequestAction.setText("Cancel Friend Request");
                                break;
                            default:
                                Log.e(TAG, "onResult: unknown friendship state");
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "onFailure: database error");
                        progressDialog.dismiss();
                    }
                });
            }
        });

        profileFriendRequestAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (friendshipState.equals("not_friends")) {
                    FirebaseDatabaseManager.sendFriendRequest(userId, new FirebaseDatabaseManagerCallback() {
                        @Override
                        public void onSuccess() {
                            friendshipState = "request_sent";
                            profileFriendRequestAction.setText("Cancel Friend Request");
                            Toast.makeText(ProfileActivity.this, "Friend request sent successfully", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(ProfileActivity.this, "Failed sending friend request", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (friendshipState.equals("request_sent")) {
                    FirebaseDatabaseManager.cancelFriendRequest(userId, new FirebaseDatabaseManagerCallback() {
                        @Override
                        public void onSuccess() {
                            friendshipState = "not_friends";
                            profileFriendRequestAction.setText("Send Friend Request");
                            Toast.makeText(ProfileActivity.this, "Friend request cancelled successfully", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(ProfileActivity.this, "Failed cancelling friend request", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (friendshipState.equals("request_received")) {
                    FirebaseDatabaseManager.acceptFriendRequest(userId, new FirebaseDatabaseManagerCallback() {
                        @Override
                        public void onSuccess() {
                            friendshipState = "friends";
                            profileFriendRequestAction.setText("Unfriend");
                            Toast.makeText(ProfileActivity.this, "Friend request accepted successfully", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(ProfileActivity.this, "Failed accepting friend request", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (friendshipState.equals("friends")) {
                    FirebaseDatabaseManager.unfriend(userId, new FirebaseDatabaseManagerCallback() {
                        @Override
                        public void onSuccess() {
                            friendshipState = "not_friends";
                            profileFriendRequestAction.setText("Send Friend Request");
                            Toast.makeText(ProfileActivity.this, "Friend removed successfully", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(ProfileActivity.this, "Failed removing friend", Toast.LENGTH_LONG).show();
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
