package com.matejvasko.player.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.models.Friend;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class FriendListAdapter
        extends PagedListAdapter<Friend, FriendListAdapter.FriendViewHolder> {

    private static final String TAG = "FriendListAdapter";

    private DatabaseReference userDatabase;

    public FriendListAdapter() {
        super(Friend.DIFF_CALLBACK);
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

    class FriendViewHolder extends RecyclerView.ViewHolder {

        private ImageView userThumbImage, userOnline;
        private TextView userName, userEmail;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userThumbImage = itemView.findViewById(R.id.item_online_friend_user_image);
            userName = itemView.findViewById(R.id.item_online_friend_user_name);
            userEmail = itemView.findViewById(R.id.item_online_friend_user_email);
            userOnline = itemView.findViewById(R.id.item_online_friend_online_circle);
        }

        void bindTo(final String userId) {
            userDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String email = dataSnapshot.child("email").getValue().toString();

                    if (dataSnapshot.hasChild("online")) {
                        Boolean isOnline = (boolean) dataSnapshot.child("online").getValue();
                        if (isOnline) {
                            userOnline.setVisibility(View.VISIBLE);
                        } else {
                            userOnline.setVisibility(View.INVISIBLE);
                        }

                    }

                    userName.setText(name);
                    userEmail.setText(email);
                    if (!thumbImage.equals("default")) {
                        // TODO You cannot start a load on a not yet attached View or a Fragment where getActivity()
                        Glide.with(App.getAppContext()).load(thumbImage).placeholder(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                    } else {
                        Glide.with(App.getAppContext()).load(R.drawable.ic_perm_identity_black_24dp).into(userThumbImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        void clear() {
            userName.setText("...");
        }

    }

}
