package com.matejvasko.player.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSongsListAdapter extends RecyclerView.Adapter<AlbumSongsListAdapter.SongViewHolder> {

    private List<MediaItemData> songs;

    public AlbumSongsListAdapter(List<MediaItemData> songs) {
        this.songs = songs;
    }

    @NonNull
    @Override
    public AlbumSongsListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_song_item, parent, false);

        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumSongsListAdapter.SongViewHolder holder, int position) {
        MediaItemData mediaItemData = songs.get(position);
        holder.number.setText("0");
        holder.title.setText(mediaItemData.title);
        holder.duration.setText(mediaItemData.mediaId);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {

        TextView number, title, duration;

         SongViewHolder(@NonNull View itemView) {
            super(itemView);
            number   = itemView.findViewById(R.id.album_song_item_index);
            title    = itemView.findViewById(R.id.album_song_item_title);
            duration = itemView.findViewById(R.id.album_song_item_duration);
        }
    }

}
