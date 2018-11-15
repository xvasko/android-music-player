package com.matejvasko.player;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContentResolverCompat;

class SongProvider {

    private final Cursor cursor;

    SongProvider(Context context) {
        cursor = ContentResolverCompat.query(
                context.getContentResolver(),
                getUri(),
                getProjection(),
                getSelection(),
                null,
                getSortOrder(),
                null
        );
    }

    int getMediaSize() {
        return cursor.getCount();
    }

    List<Song> getSongsAtRange(int startPosition, int endPosition) {
        List<Song> songs = new ArrayList<>();
        for (int position = startPosition; position < endPosition; ++position) {
            Song song = getSongAtPosition(position);
            if (song != null)
                songs.add(song);
        }

        return songs;
    }

    public Song getSongAtPosition(int position) {
        if (!cursor.moveToPosition(position)) {
            System.out.println("THERE IS NOTHING AT POSITION: " + position);
            return null;
        }
        return createSong(cursor);
    }

    private Song createSong(Cursor cursor) {
        return new Song.Builder(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                .setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                .setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                .setIconUri(ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                .setcursorPosition(cursor.getPosition())
                .build();
    }

    private Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };
    }

    private String getSelection() {
        return "("
                + "(" + MediaStore.Audio.Media.IS_MUSIC + " !=0 )"
                + "AND (" + MediaStore.Audio.Media.IS_ALARM + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_NOTIFICATION + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_PODCAST + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_RINGTONE + " ==0 )"
                + ")";
    }

    @Nullable
    private String getSortOrder() {
        return MediaStore.Audio.Media.TITLE + " ASC";
    }

}
