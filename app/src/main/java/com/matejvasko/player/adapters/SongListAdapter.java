package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.viewmodels.NowPlaying;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends PagedListAdapter<Song, SongListAdapter.SongViewHolder>  {

    private final Context context;

    private Map<Uri, Bitmap> map = new HashMap<>();

    public SongListAdapter(Context context) {
        super(Song.DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.SongViewHolder holder, int position) {
        Song song = getItem(position);
        if (song != null) {
            holder.bindTo(song);
        } else {
            holder.clear();
        }

    }


    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageView;
        final TextView songItemView;
        final TextView artistItemView;
        final SongListAdapter adapter;

        private Song song;

        SongViewHolder(View itemView, SongListAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.cover_item);
            songItemView = itemView.findViewById(R.id.song_item);
            artistItemView = itemView.findViewById(R.id.artist_item);

            this.adapter = adapter;
        }

         void bindTo(Song song) {
            this.song = song;

            songItemView.setText(song.title);
            artistItemView.setText(song.artist);
            Bitmap iconBitmap = getBitmapFromMediaStore(song.iconUri);
            if (iconBitmap == null) {
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher_background));
            } else {
                imageView.setImageBitmap(iconBitmap);
            }
        }

        void clear() {
            songItemView.setText("Loading...");
            artistItemView.setText("Loading...");
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_launcher_background));
        }

        @Override
        public void onClick(View v) {
            NowPlaying.getNowPlaying().setValue(song);
        }
    }

    private Bitmap getBitmapFromMediaStore(Uri iconUri) {
        if (map.containsKey(iconUri)) {
            return map.get(iconUri);
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), iconUri);
                map.put(iconUri, bitmap);
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
                map.put(iconUri, null);
                return null;
            }
        }
    }
}

