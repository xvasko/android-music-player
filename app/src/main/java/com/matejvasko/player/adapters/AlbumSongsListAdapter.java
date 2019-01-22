package com.matejvasko.player.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.MainActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.models.Album;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.utils.SharedPref;
import com.matejvasko.player.utils.Utils;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumSongsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Album album;
    private List<Song> songs;

    private SharedPref sharedPref = SharedPref.getInstance();

    public AlbumSongsListAdapter(Context context, Album album, List<Song> songs) {
        this.context = context;
        this.album = album;
        this.songs = songs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;

        if (viewType == 1) {
            itemView = layoutInflater.inflate(R.layout.item_album_first_item, parent, false);
            return new FirstItemHolder(itemView);
        } else {
            itemView = layoutInflater.inflate(R.layout.item_album_song, parent, false);
            return new SongViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof FirstItemHolder) {
            ((FirstItemHolder) holder).bindTo();
        } else {
            Song song = songs.get(position - 1);
            ((SongViewHolder) holder).bindTo(song, position);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }

    @Override
    public int getItemCount() {
        return songs.size() + 1;
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Song song;

        private TextView songNumber;
        private TextView songTitle;
        private TextView songDuration;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            songNumber = itemView.findViewById(R.id.item_album_song_number);
            songTitle = itemView.findViewById(R.id.item_album_song_title);
            songDuration = itemView.findViewById(R.id.item_album_song_duration);
        }

        void bindTo(Song song, int position) {
            this.song = song;
            itemView.setOnClickListener(this);

            songNumber.setText(String.valueOf(position));
            songTitle.setText(song.title);
            songDuration.setText(Utils.millisecondsToString(song.duration));
        }

        @Override
        public void onClick(View view) {
            sharedPref.setCurrentAlbumId(album.id);
            ((MainActivity) context).playSongFromAlbum(song);
        }
    }

    class FirstItemHolder extends RecyclerView.ViewHolder {

        private ImageView albumArt;
        private TextView albumTitle;
        private TextView albumBy;

        FirstItemHolder(@NonNull View itemView) {
            super(itemView);

            albumArt = itemView.findViewById(R.id.item_album_first_item_album_art);
            albumTitle = itemView.findViewById(R.id.item_album_first_item_title);
            albumBy = itemView.findViewById(R.id.item_album_first_item_album_by);
        }

        void bindTo() {
            Bitmap iconBitmap = Utils.getBitmapFromMediaStore(album.albumArtUri);
            if (iconBitmap != null) {
                albumArt.setImageBitmap(iconBitmap);
            } else {
                albumArt.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            }
            albumTitle.setText(album.title);
            albumBy.setText(String.format("Album by %s", album.artist));
        }
    }

}
