package com.matejvasko.player.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.matejvasko.player.R;
import com.matejvasko.player.databinding.ActivityProfileBinding;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.models.User;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private Button profileFriendRequestAction;

    private ProgressDialog progressDialog;

    private String friendshipState;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        prepareUI();

        friendshipState = "not_friends";

        FirebaseDatabaseManager.getUserData(userId, new FirebaseDatabaseManagerCallback() {
            @Override
            public void onResult(User user) {
                binding.setUser(user);
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

        binding.profileFriendRequestActionButton.setOnClickListener(new View.OnClickListener() {
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
        profileFriendRequestAction = findViewById(R.id.profile_friend_request_action_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading user data");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
}
