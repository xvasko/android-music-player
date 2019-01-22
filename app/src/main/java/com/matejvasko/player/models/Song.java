package com.matejvasko.player.models;

import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;

import com.matejvasko.player.utils.Utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class Song {

    public String id;
    public boolean isFromAlbum;
    public String title;
    public String artist;
    public String albumArtUri;
    public long duration;
    public int cursorPosition;
    public String fileName;
    public String albumId;

    public Song(String id, boolean isFromAlbum, String title, String artist, String albumArtUri, long duration, int cursorPosition, String fileName, String albumId) {
        this.id = id;
        this.isFromAlbum = isFromAlbum;
        this.title = title;
        this.artist = artist;
        this.albumArtUri = albumArtUri;
        this.duration = duration;
        this.cursorPosition = cursorPosition;
        this.fileName = fileName;
        this.albumId = albumId;
    }

    public MediaMetadataCompat getMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, fileName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, Utils.getBitmapFromMediaStore(Uri.parse(albumArtUri)))
                .build();
    }

    public static DiffUtil.ItemCallback<Song> DIFF_CALLBACK = new DiffUtil.ItemCallback<Song>() {
        @Override
        public boolean areItemsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Song oldItem, @NonNull Song newItem) {
            return oldItem.equals(newItem);
        }
    };

}
