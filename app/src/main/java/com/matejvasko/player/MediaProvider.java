package com.matejvasko.player;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import com.matejvasko.player.paging.MediaItemDataSource;
import com.matejvasko.player.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContentResolverCompat;

public final class MediaProvider {

    private static volatile MediaProvider instance;

    private Cursor songCursor;
    private Cursor albumCursor;
    private Cursor albumSongsCursor;

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
                getAlbumSortOrder(),
                null
        );
    }

    public int getSongCursorSize() {
        return songCursor.getCount();
    }

    public int getAlbumSongCursorSize() {
        return albumSongsCursor.getCount();
    }

    public int getAlbumCursorSize() {
        return albumCursor.getCount();
    }

    public List<MediaItemData> getMediaItemDataAtRange(int startPosition, int endPosition, int flag) {
        List<MediaItemData> mediaItems = new ArrayList<>();

        for (int position = startPosition; position < endPosition; ++position) {
            MediaItemData mediaItemData = getMediaItemDataAtPosition(position, flag);
            if (mediaItemData != null)
                mediaItems.add(mediaItemData);
        }

        return mediaItems;
    }

    public MediaItemData getMediaItemDataAtPosition(int position, int flag) {

        Cursor cursor;

        if (flag == MediaItemDataSource.SONG_DATA_SOURCE) {
            cursor = songCursor;
        } else {
            cursor = albumCursor;
        }

        if (!cursor.moveToPosition(position)) {
            return null;
        }

        return createMediaItemData(cursor, flag);
    }

    public List<MediaItemData> getAlbumSongs(String albumId) {
        System.out.println("album id inside getAlbumSongs:" + albumId);
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

        List<MediaItemData> songs = new ArrayList<>();

        for (int i = 0; i < albumSongsCursor.getCount(); i++) {
            albumSongsCursor.moveToNext();
            songs.add(createMediaItemData(albumSongsCursor, MediaItemDataSource.SONG_DATA_SOURCE, true));
        }

        return songs;
    }

    public MediaItemData getSongFromAlbum(int position) {
        albumSongsCursor.moveToPosition(position);
        return createMediaItemData(albumSongsCursor, MediaItemDataSource.SONG_DATA_SOURCE);
    }

    private MediaItemData createMediaItemData(Cursor cursor, int flag) {
        return createMediaItemData(cursor, flag, false);
    }

    private MediaItemData createMediaItemData(Cursor cursor, int flag, boolean isFromAlbum) {
        if (flag == MediaItemDataSource.SONG_DATA_SOURCE) {
            return new MediaItemData.Builder(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                    .setBrowseable(false)
                    .setFromAlbum(isFromAlbum)
                    .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                    .setSubtitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                    .setAlbumArtUri(Uri.parse("content://media/external/audio/albumart/" + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                    .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
                    .setCursorPosition(cursor.getPosition())
                    .build();
        } else {
            return new MediaItemData.Builder(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums._ID)))
                    .setBrowseable(true)
                    .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)))
                    .setSubtitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)))
                    .setAlbumArtUri(Uri.parse("content://media/external/audio/albumart/" + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID))))
                    .build();
        }
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
                + "(" + MediaStore.Audio.Media.IS_MUSIC + " !=0)"
                + "AND (" + MediaStore.Audio.Media.IS_ALARM + " ==0)"
                + "AND (" + MediaStore.Audio.Media.IS_NOTIFICATION + " ==0)"
                + "AND (" + MediaStore.Audio.Media.IS_PODCAST + " ==0)"
                + "AND (" + MediaStore.Audio.Media.IS_RINGTONE + " ==0)"
                + ")";
    }

    private String getAlbumSongsSelection(String albumId) {
        return "(" + MediaStore.Audio.Media.ALBUM_ID + "==" + albumId + ")";
    }

    private String getSongSortOrder() {
        return MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC";
    }

    private String getAlbumSortOrder() {
        return MediaStore.Audio.Albums.ALBUM + " COLLATE NOCASE ASC";
    }

    MediaMetadataCompat getMediaMetadata(int cursorPosition, boolean playingAlbum) {
        Cursor cursor;
        if (playingAlbum) {
            cursor = albumSongsCursor;
        } else {
            cursor = songCursor;
        }

        cursor.moveToPosition(cursorPosition);

        return createMediaMetadata(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
        );
    }

    private MediaMetadataCompat createMediaMetadata(
            String mediaId,
            String title,
            String artist,
            String albumId,
            String mediaUri,
            long duration) {

        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumId)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUri)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, Utils.getBitmapFromMediaStore(Uri.parse("content://media/external/audio/albumart/" + albumId)))
                .build();
    }

}
