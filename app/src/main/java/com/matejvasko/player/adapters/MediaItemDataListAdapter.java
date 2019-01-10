package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.MainActivity;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.R;
import com.matejvasko.player.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class MediaItemDataListAdapter
        extends PagedListAdapter<MediaItemData, RecyclerView.ViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final Context context;

    public MediaItemDataListAdapter(Context context) {
        super(MediaItemData.DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;

        if (viewType == TYPE_SONG) {
            itemView = layoutInflater.inflate(R.layout.item_song, parent, false);
            return new SongViewHolder(itemView);
        } else {
            itemView = layoutInflater.inflate(R.layout.item_cardview, parent, false);
            return new AlbumViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaItemData mediaItemData = getItem(position);
        if (mediaItemData != null) {
            if (mediaItemData.isBrowseable) {
                ((AlbumViewHolder) holder).bindTo(mediaItemData);
            } else {
                ((SongViewHolder) holder).bindTo(mediaItemData);
            }
        }
    }

    private static final int TYPE_ALBUM = 0;
    private static final int TYPE_SONG = 1;

    @Override
    public int getItemViewType(int position) {
        MediaItemData mediaItemData = getItem(position);
        if (mediaItemData != null) {
            return mediaItemData.isBrowseable ? TYPE_ALBUM : TYPE_SONG;
        } else {
            return TYPE_SONG;
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        MediaItemData mediaItemData = getItem(position);
        if (mediaItemData == null) {
            return "";
        } else {
            return (mediaItemData.title.charAt(0) + "").toUpperCase();
        }

    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView;
        TextView songItemView;
        TextView artistItemView;

        private MediaItemData mediaItemData;

        SongViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.cover_item);
            songItemView = itemView.findViewById(R.id.song_item);
            artistItemView = itemView.findViewById(R.id.artist_item);
        }

        void bindTo(MediaItemData mediaItemData) {
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

        @Override
        public void onClick(View v) {
            System.out.println("MEDIAITEMDATA CURSOR POSITION: " + mediaItemData.cursorPosition);
            ((MainActivity) context).customAction("playSong", mediaItemData);
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView albumImage;
        TextView albumTitle;
        CardView cardView;

        private MediaItemData mediaItemData;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            albumImage = itemView.findViewById(R.id.album_img_id);
            albumTitle = itemView.findViewById(R.id.album_title_id);
            cardView = itemView.findViewById(R.id.cardview_id);
        }

        void bindTo(MediaItemData mediaItemData) {
            this.mediaItemData = mediaItemData;

            albumTitle.setText(mediaItemData.title);
            Bitmap iconBitmap = Utils.getBitmapFromMediaStore(mediaItemData.albumArtUri);
            if (iconBitmap == null) {
                albumImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            } else {
                albumImage.setImageBitmap(iconBitmap);
            }
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString("album_id", mediaItemData.mediaId);
            bundle.putString("album_title", mediaItemData.title);
            NavController navController = Navigation.findNavController(((MainActivity) context), R.id.nav_host_fragment);
            navController.navigate(R.id.albumFragment, bundle);
        }
    }

}
