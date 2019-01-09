package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.App;
import com.matejvasko.player.MainActivity;
import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.R;
import com.matejvasko.player.fragments.library.AlbumFragment;
import com.matejvasko.player.utils.Utils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.AlbumViewHolder> {

    private Context context;
    private List<MediaItemData> data;

    public RecyclerViewAdapter(Context context, List<MediaItemData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_cardview, parent, false);

        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, final int position) {

        holder.albumTitle.setText(data.get(position).title);
        MediaItemData mediaItemData = data.get(position);
        Bitmap iconBitmap = Utils.getBitmapFromMediaStore(mediaItemData.albumArtUri);
        if (iconBitmap == null) {
            holder.albumImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
        } else {
            holder.albumImage.setImageBitmap(iconBitmap);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("album_id", data.get(position).mediaId);
                bundle.putString("album_title", data.get(position).title);
                NavController navController = Navigation.findNavController(((MainActivity) context), R.id.nav_host_fragment);
                navController.navigate(R.id.albumFragment, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView albumImage;
        TextView albumTitle;
        CardView cardView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            albumImage = itemView.findViewById(R.id.album_img_id);
            albumTitle = itemView.findViewById(R.id.album_title_id);
            cardView = itemView.findViewById(R.id.cardview_id);

        }
    }

}
