package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.MainActivity;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.utils.Utils;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends PagedListAdapter<MediaItemData, SongListAdapter.SongViewHolder>  {

    private final Context context;
    private final MainActivity activity;

    public SongListAdapter(Context context, MainActivity activity) {
        super(MediaItemData.DIFF_CALLBACK);
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(itemView, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.SongViewHolder holder, int position) {
        MediaItemData mediaItemData = getItem(position);
        if (mediaItemData != null) {
            holder.bindTo(mediaItemData);
        } else {
            holder.clear();
        }
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageView;
        final TextView songItemView;
        final TextView artistItemView;
        final MainActivity activity;

        private MediaItemData mediaItemData;

        SongViewHolder(View itemView, MainActivity activity) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.cover_item);
            songItemView = itemView.findViewById(R.id.song_item);
            artistItemView = itemView.findViewById(R.id.artist_item);

            this.activity = activity;
        }

        void bindTo(MediaItemData mediaItemData) {
            //song.setFromSongTab(true);
            this.mediaItemData = mediaItemData;

            songItemView.setText(mediaItemData.title);
            artistItemView.setText(mediaItemData.subtitle);
            Bitmap iconBitmap = Utils.getBitmapFromMediaStore(mediaItemData.albumArtUri);
            if (iconBitmap == null) {
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            } else {
                imageView.setImageBitmap(iconBitmap);
            }
        }

        void clear() {
            songItemView.setText("...");
            artistItemView.setText("...");
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
        }

        @Override
        public void onClick(View v) {
            activity.playFromMediaId(mediaItemData);
        }
    }

}
