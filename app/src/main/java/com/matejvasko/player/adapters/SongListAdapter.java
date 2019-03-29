package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.activities.MainActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter
        extends PagedListAdapter<Song, SongListAdapter.SongViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private Context context;

    public SongListAdapter(Context context) {
        super(Song.DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getItem(position);
        if (song != null) {
            holder.bindTo(song);
        } else {
            holder.clear();
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Song song = getItem(position);
        if (song == null) {
            return "";
        } else {
            return (song.title.charAt(0) + "").toUpperCase();
        }
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Song song;

        private ImageView songArt;
        private TextView songTitle;
        private TextView songArtist;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            songArt = itemView.findViewById(R.id.item_song_art);
            songTitle = itemView.findViewById(R.id.item_song_title);
            songArtist = itemView.findViewById(R.id.item_song_artist);
        }

        void bindTo(Song song) {
            this.song = song;

            Bitmap iconBitmap = Utils.getBitmapFromMediaStore(Uri.parse(song.albumArtUri));
            if (iconBitmap != null) {
                songArt.setImageBitmap(iconBitmap);
            } else {
                songArt.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            }
            songTitle.setText(song.title);
            songArtist.setText(song.artist);
        }

        void clear() {
            songTitle.setText("...");
            songArtist.setText("...");
        }

        @Override
        public void onClick(View v) {
            ((MainActivity) context).playSong(song);
        }
    }

}
