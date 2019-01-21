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
import com.matejvasko.player.R;
import com.matejvasko.player.models.Album;
import com.matejvasko.player.utils.Utils;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumListAdapter
        extends PagedListAdapter<Album, AlbumListAdapter.AlbumViewHolder> {

    private Context context;

    public AlbumListAdapter(Context context) {
        super(Album.DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumListAdapter.AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        if (album != null) {
            holder.bindTo(album);
        } else {
            holder.clear();
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Album album;

        private ImageView albumArt;
        private TextView albumTitle;
        private TextView albumArtist;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            albumArt = itemView.findViewById(R.id.item_album_art);
            albumTitle = itemView.findViewById(R.id.item_album_title);
            albumArtist = itemView.findViewById(R.id.item_album_artist);
        }

        void bindTo(Album album) {
            this.album = album;

            Bitmap iconBitmap = Utils.getBitmapFromMediaStore(album.albumArtUri);
            if (iconBitmap != null) {
                albumArt.setImageBitmap(iconBitmap);
            } else {
                albumArt.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            }
            albumTitle.setText(album.title);
            albumArtist.setText(album.artist);
        }

        void clear() {
            albumTitle.setText("...");
            albumArtist.setText("...");
        }

        @Override
        public void onClick(View v) {
            Bundle extras = new Bundle();
            extras.putParcelable("album", album);
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.animation)
                    .build();
            NavController navController = Navigation.findNavController(((MainActivity) context), R.id.nav_host_fragment);
            navController.navigate(R.id.albumFragment, extras, navOptions);
        }
    }
}
