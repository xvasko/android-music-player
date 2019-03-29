package com.matejvasko.player.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.R;
import com.matejvasko.player.models.Friend;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class FriendListAdapter
        extends PagedListAdapter<Friend, FriendListAdapter.FriendViewHolder> {

    private static final String TAG = "FriendListAdapter";

    public FriendListAdapter() {
        super(Friend.DIFF_CALLBACK);
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
            holder.bindTo(friend);
        } else {
            holder.clear();
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {

        private TextView friendName;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            friendName = itemView.findViewById(R.id.item_online_friend_user_name);
        }

        void bindTo(Friend friend) {
            friendName.setText(friend.getName());
        }

        void clear() {
            friendName.setText("...");
        }

    }

}
