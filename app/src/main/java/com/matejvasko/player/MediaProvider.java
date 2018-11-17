package com.matejvasko.player;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContentResolverCompat;

public final class MediaProvider {

    private static volatile MediaProvider instance;

    private  Cursor songCursor;
    private  Cursor albumCursor;
    private  Cursor albumSongsCursor;

    public static MediaProvider getInstance() {
        if (instance == null) {
            synchronized (MediaProvider.class) {
                if (instance == null) {
                    instance = new MediaProvider();
                }
            }
        }
        return instance;
    }

    MediaProvider() {
        songCursor = ContentResolverCompat.query(
                App.getAppContext().getContentResolver(),
                getSongUri(),
                getSongProjection(),
                getSongSelection(),
                null,
                getSongSortOrder(),
                null
        );
        albumCursor = ContentResolverCompat.query(
                App.getAppContext().getContentResolver(),
                getAlbumUri(),
                getAlbumProjection(),
                null,
                null,
                null,
                null
        );
    }

    public List<Album> getAlbums() {
        List<Album> albums = new ArrayList<>();
        for (int i = 0; i < albumCursor.getCount(); i++) {
            albumCursor.moveToPosition(i);
            albums.add(new Album(
                    albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Albums._ID)),
                    albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)),
                    null,
                    albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST))));
        }

        return albums;
    }

    public List<Song> getAlbumSongs(String albumId) {
        if (albumId == null || albumId.isEmpty()) {
            throw new IllegalArgumentException();
        }

        albumSongsCursor = ContentResolverCompat.query(
                App.getAppContext().getContentResolver(),
                getSongUri(),
                getSongProjection(),
                getAlbumSongsSelection(albumId),
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

    int getSongCursorSize() {
        return songCursor.getCount();
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
        if (!songCursor.moveToPosition(position)) {
            System.out.println("THERE IS NOTHING AT POSITION: " + position);
            return null;
        }
        return createSong(songCursor);
    }

    private Song createSong(Cursor cursor) {
        return new Song.Builder(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                .setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                .setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                .setIconUri(Uri.parse("content://media/external/audio/albumart/" + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                .setCursorPosition(cursor.getPosition())
                .setAlbumId(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))
                .build();
    }

    private Uri getSongUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    private Uri getAlbumUri() {
        return MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    }

    private String[] getSongProjection() {
        return new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
        };
    }

    private String[] getAlbumProjection() {
        return new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART
        };
    }

    private String getSongSelection() {
        return "("
                + "(" + MediaStore.Audio.Media.IS_MUSIC + " !=0 )"
                + "AND (" + MediaStore.Audio.Media.IS_ALARM + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_NOTIFICATION + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_PODCAST + " ==0 )"
                + "AND (" + MediaStore.Audio.Media.IS_RINGTONE + " ==0 )"
                + ")";
    }

    private String getAlbumSongsSelection(String albumId) {
        return "(" + MediaStore.Audio.Media.ALBUM_ID + "==" + albumId + ")";
    }

    @Nullable
    private String getSongSortOrder() {
        return MediaStore.Audio.Media.TITLE + " ASC";
    }

}
