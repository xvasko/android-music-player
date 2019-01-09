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
import com.matejvasko.player.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class MediaItemDataListAdapter extends PagedListAdapter<MediaItemData, RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private final Context context;
    private final MainActivity activity;

    public MediaItemDataListAdapter(Context context) {
        super(MediaItemData.DIFF_CALLBACK);
        this.context = context;
        this.activity = (MainActivity) context;
    }

    @NonNull
    @Override
    public MediaItemDataListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(itemView, activity);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaItemData mediaItemData = getItem(position);

        if (mediaItemData != null) {
            ((SongViewHolder) holder).bindTo(mediaItemData);
        } else {
            ((SongViewHolder) holder).clear();
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return (getItem(position).title.charAt(0) + "").toUpperCase();
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
            if (mediaItemData.isBrowseable) {
//                FragmentManager fragmentManager = activity.getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                Bundle bundle = new Bundle();
//                bundle.putString("album_id", mediaItemData.mediaId);
//                bundle.putString("album_title", mediaItemData.title);
//                AlbumFragment albumFragment = new AlbumFragment();
//                albumFragment.setArguments(bundle);
//                fragmentTransaction.replace(R.id.album_fragment_container, albumFragment, "albumFragment");
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
            } else {
                System.out.println("MEDIAITEMDATA CURSOR POSITION: " + mediaItemData.cursorPosition);
                activity.customAction("playSong", mediaItemData);
            }

        }
    }

}
