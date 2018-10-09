package com.matejvasko.player.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.R;

import java.util.LinkedList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder>  {

    private final LinkedList<String> songList;
    private LayoutInflater inflater;

    public SongListAdapter(Context context, LinkedList<String> songList) {
        inflater = LayoutInflater.from(context);
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.song_item, parent, false);

        return new SongViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.SongViewHolder holder, int position) {
        String mCurrent = songList.get(position);
        holder.songItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView songItemView;
        final SongListAdapter adapter;

        SongViewHolder(View itemView, SongListAdapter adapter) {
            super(itemView);
            songItemView = itemView.findViewById(R.id.song_item);
            this.adapter = adapter;
        }

        @Override
        public void onClick(View v) {

        }
    }
}

