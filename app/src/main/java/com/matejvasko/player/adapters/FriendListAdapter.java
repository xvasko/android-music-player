package com.matejvasko.player.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.matejvasko.player.App;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.databinding.ItemFriendBinding;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;
import com.matejvasko.player.models.User;
import com.matejvasko.player.utils.Utils;

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
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemFriendBinding binding = ItemFriendBinding.inflate(layoutInflater, parent, false);

        return new FriendViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: ");
        User user = getItem(position);
        if (user != null) {
            holder.bindTo(user);
        }
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String userId = "";

        private ItemFriendBinding binding;

        FriendViewHolder(ItemFriendBinding binding) {
            super(binding.getRoot());
            itemView.setOnClickListener(this);
            this.binding = binding;
        }

        void bindTo(User user) {
            FirebaseDatabaseManager.getUserData(user.getUid(), new FirebaseDatabaseManagerCallback() {
                @Override
                public void onResult(User user) {
                    if (user != null) {
                        userId = user.getUid();
                        binding.setUser(user);
                        binding.executePendingBindings();
                    }
                }
            });
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
