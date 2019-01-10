package com.matejvasko.player.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.App;
import com.matejvasko.player.MainActivity;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.R;
import com.matejvasko.player.utils.SharedPref;
import com.matejvasko.player.utils.Utils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSongsListAdapter extends RecyclerView.Adapter<AlbumSongsListAdapter.SongViewHolder> {

    private MainActivity mainActivity;
    private List<MediaItemData> songs;
    private String albumId;

    private SharedPref sharedPref = SharedPref.getInstance();

    public AlbumSongsListAdapter(Context context, List<MediaItemData> songs, String albumId) {
        this.songs = songs;
        this.mainActivity = (MainActivity) context;
        this.albumId = albumId;
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
        holder.mediaItemData = mediaItemData;
        holder.number.setText(position + 1 + "");
        holder.title.setText(mediaItemData.title);
        holder.duration.setText(Utils.millisecondsToString(mediaItemData.duration));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        MediaItemData mediaItemData;
        TextView number, title, duration;

         SongViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            number   = itemView.findViewById(R.id.album_song_item_index);
            title    = itemView.findViewById(R.id.album_song_item_title);
            duration = itemView.findViewById(R.id.album_song_item_duration);
        }

        @Override
        public void onClick(View view) {
            sharedPref.setCurrentAlbumId(albumId);
            mainActivity.customAction("playSongFromAlbum", mediaItemData);
        }
    }

}
