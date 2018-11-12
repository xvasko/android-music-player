package com.matejvasko.player;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContentResolverCompat;

public class AlbumProvider {

    private Context context;
    private final Cursor cursor;
    private Cursor albumSongsCursor;

    public AlbumProvider(Context context) {
        this.context = context;
        cursor = ContentResolverCompat.query(
                context.getContentResolver(),
                getUri(),
                getProjection(),
                null,
                null,
                null,
                null
        );
    }

    private Uri getUri() {
        return MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART
        };
    }

    public List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            albums.add(new Album(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
                    null));
        }

        return albums;
    }

    public List<Song> getAlbumSongs(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            throw new IllegalArgumentException();
        }

        albumSongsCursor = ContentResolverCompat.query(
                context.getContentResolver(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION
                },
         "(" + MediaStore.Audio.Media.ALBUM_ID + "==" + albumId + ")",
                null,
                null,
                null
        );

        List<Song> songs = new ArrayList<>();

        for (int i = 0; i < albumSongsCursor.getCount(); i++) {
            albumSongsCursor.moveToNext();
            songs.add(createSong(albumSongsCursor));
        }

        return songs;
    }

    private Song createSong(Cursor cursor) {
        return new Song.Builder(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                .setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                .setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                .setIconUri(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                .build();
    }

}
