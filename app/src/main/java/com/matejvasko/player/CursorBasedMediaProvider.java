package com.matejvasko.player;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContentResolverCompat;

public class CursorBasedMediaProvider {

    private final Cursor cursor;

    CursorBasedMediaProvider(Context context) {
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

    public int getMediaSize() {
        return cursor.getCount();
    }

    public Song getSongAtPosition(int position) {
        if (!cursor.moveToPosition(position))
            return null;

        return createSong(cursor);
    }


    public List<Song> getSongsAtRange(int startPosition, int endPosition) {
        List<Song> songs = new ArrayList<>();
        for (int position = startPosition; position < endPosition; ++position) {
            Song song = getSongAtPosition(position);
            if (song != null)
                songs.add(song);
        }

        return songs;
    }

    private Song createSong(Cursor cursor) {
        Song song = new Song(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
        return song;
    }


    private Uri getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
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
        return MediaStore.Audio.Media.ARTIST + " COLLATE LOCALIZED ASC";
    }

}
