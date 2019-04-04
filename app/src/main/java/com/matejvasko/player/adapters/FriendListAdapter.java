package com.matejvasko.player.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.models.User;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class FriendListAdapter
        extends PagedListAdapter<User, FriendListAdapter.FriendViewHolder> {

    private static final String TAG = "FriendListAdapter";

    private Context context;

    public FriendListAdapter(Context context) {
        super(User.DIFF_CALLBACK);
        this.context = context;
        Log.d(TAG, "FriendListAdapter:");
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_friend, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        User user = getItem(position);
        if (user != null) {
            holder.bindTo(user);
        } else {
            holder.clear();
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String userId = "";

        private ImageView userThumbImage, userOnline;
        private TextView userName, userSong;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            userThumbImage = itemView.findViewById(R.id.item_online_friend_user_image);
            userName = itemView.findViewById(R.id.item_online_friend_user_name);
            userSong = itemView.findViewById(R.id.item_online_friend_user_song);
            userOnline = itemView.findViewById(R.id.item_online_friend_online_circle);
        }

        void bindTo(User user) {
            FirebaseDatabaseManager.getUserData(user.getUid(), new FirebaseDatabaseManagerCallback() {
                @Override
                public void onResult(User user) {
                    if (user != null) {
                        userId = user.getUid();
                        userName.setText(user.getName());
                        String thumbImage = user.getThumbImage();
                        if (!thumbImage.equals("default")) {
                            Glide.with(App.getAppContext()).load(thumbImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                        } else {
                            Glide.with(App.getAppContext()).load(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                        }
                        if (user.getOnline() != null) {
                            if (user.getOnline()) {
                                userOnline.setVisibility(View.VISIBLE);
                            } else {
                                userOnline.setVisibility(View.INVISIBLE);
                            }
                        }
                        if (user.getCurrentSong() != null) {
                            userSong.setText(user.getCurrentSong());
                        }
                    }
                }
            });
        }

        void clear() {
            userName.setText("...");
        }

        @Override
        public void onClick(View v) {
            if (!userId.equals("")) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("user_id", userId);
                context.startActivity(intent);
            }
        }
    }

}
