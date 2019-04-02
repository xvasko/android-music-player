package com.matejvasko.player.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.R;

import java.util.List;

import androidx.annotation.NonNull;
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

        private TextView requestId;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            requestId = itemView.findViewById(R.id.request_id);
        }

        void bindTo(String reqId) {
            requestId.setText(reqId);
        }

    }

}
