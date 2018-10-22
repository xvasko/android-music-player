package com.matejvasko.player.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matejvasko.player.R;
import com.matejvasko.player.viewmodels.NowPlaying;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder>  {

    private final LinkedList<String> songList;
    private LayoutInflater inflater;

    private Cursor cursor;
    private final Context activity;

    private Map<String, Bitmap> map = new HashMap<>();

    public SongListAdapter(Context context, LinkedList<String> songList) {
        activity = context;
        inflater = LayoutInflater.from(context);
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongListAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull SongListAdapter.SongViewHolder holder, int position) {

        cursor.moveToPosition(position);
        holder.songItemView.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        holder.artistItemView.setText(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
        holder.songUri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        holder.songTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        holder.artistTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

        Bitmap bitmap = getBitmapFromMediaStore(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap);
            holder.artistArt = bitmap;
        } else {
            holder.imageView.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_launcher_background));
        }
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imageView;
        final TextView songItemView;
        final TextView artistItemView;
        final SongListAdapter adapter;
        String songUri;
        String songTitle;
        String artistTitle;
        Bitmap artistArt;

        SongViewHolder(View itemView, SongListAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.cover_item);
            songItemView = itemView.findViewById(R.id.song_item);
            artistItemView = itemView.findViewById(R.id.artist_item);

            this.adapter = adapter;
        }

        @Override
        public void onClick(View v) {
            NowPlaying.getNowPlaying().setValue(new NowPlaying.Song(songUri, artistArt,songTitle, artistTitle));
        }
    }

    private Cursor swapCursor(Cursor cursor) {
        if (this.cursor == cursor) {
            return null;
        }
        Cursor oldCursor = this.cursor;
        this.cursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void changeCursor(Cursor cursor)  {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
    }

    private Bitmap getBitmapFromMediaStore(String albumId) {
        if (map.containsKey(albumId)) {
            return map.get(albumId);
        } else {
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                return map.put(albumId, bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                return map.put(albumId, null);
            }
        }
    }
}

