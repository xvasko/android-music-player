package com.matejvasko.player.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.matejvasko.player.R;
import com.matejvasko.player.activities.ProfileActivity;
import com.matejvasko.player.authentication.Authentication;
import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.firebase.FirebaseDatabaseManagerCallback;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class RequestListAdapter extends RecyclerView.Adapter<RequestListAdapter.RequestViewHolder> {

    private Context context;
    private List<String> requests;

    public RequestListAdapter(Context context, List<String> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        String reqId = requests.get(position);
        if (reqId != null) {
            holder.bindTo(reqId);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView requestFrom;
        private Button acceptButton, ignoreButton;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            requestFrom = itemView.findViewById(R.id.request_from);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            ignoreButton = itemView.findViewById(R.id.request_ignore_button);
        }

        void bindTo(final String userId) {
            requestFrom.setText(userId);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabaseManager.acceptFriendRequest(userId, new FirebaseDatabaseManagerCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context, "User request accepted successfully", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(context, "Failed accepting friend request", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }

    }

}
