package com.matejvasko.player;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


import androidx.core.content.ContentResolverCompat;

public class AlbumProvider {

    private final Cursor cursor;

    AlbumProvider(Context context) {
        cursor = ContentResolverCompat.query(
                context.getContentResolver(),
                getUri(),
                getProjection(),
                null,
                null,
                null,
                null
        );

        for (int i=1; i <= cursor.getCount(); i++) {
            cursor.moveToNext();
            System.out.println("ALBUM: "+cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
        }

    }

    private Uri getUri() {
        return MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    }

    private String[] getProjection() {
        return new String[]{
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST
        };
    }

}
