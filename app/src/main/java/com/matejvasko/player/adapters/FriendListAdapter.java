package com.matejvasko.player.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.models.Friend;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class FriendListAdapter
        extends PagedListAdapter<Friend, FriendListAdapter.FriendViewHolder> {

    private static final String TAG = "FriendListAdapter";

    private DatabaseReference userDatabase;
    private Context context;

    public FriendListAdapter(Context context) {
        super(Friend.DIFF_CALLBACK);
        this.context = context;
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        // userDatabase.keepSynced(true); TODO https://firebase.google.com/docs/database/android/offline-capabilities
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
        Friend friend = getItem(position);
        if (friend != null) {
            holder.bindTo(friend.getUid());
        } else {
            holder.clear();
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String userId = "";

        private ImageView userThumbImage, userOnline, userLike;
        private TextView userName, userSong;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            userThumbImage = itemView.findViewById(R.id.item_online_friend_user_image);
            userName = itemView.findViewById(R.id.item_online_friend_user_name);
            userSong = itemView.findViewById(R.id.item_online_friend_user_song);
            userOnline = itemView.findViewById(R.id.item_online_friend_online_circle);
//            userLike = itemView.findViewById(R.id.item_online_friend_like);
        }

        void bindTo(final String userId) {
            this.userId = userId;
            userDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();

                    if (dataSnapshot.hasChild("online")) {
                        Boolean isOnline = (boolean) dataSnapshot.child("online").getValue();
                        if (isOnline) {
                            userOnline.setVisibility(View.VISIBLE);
                        } else {
                            userOnline.setVisibility(View.INVISIBLE);
                        }

                    }

                    if (dataSnapshot.hasChild("current_song")) {
                        String song =  dataSnapshot.child("current_song").getValue().toString();
                        userSong.setText(song);
                    }

                    userName.setText(name);

                    if (!thumbImage.equals("default")) {
                        // TODO You cannot start a load on a not yet attached View or a Fragment where getActivity()
                        Glide.with(App.getAppContext()).load(thumbImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                    } else {
                        Glide.with(App.getAppContext()).load(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                    }

//                    userLike.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(App.getAppContext(), "Clicked on LIKE! " + userId, Toast.LENGTH_LONG).show();
//                        }
//                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

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
