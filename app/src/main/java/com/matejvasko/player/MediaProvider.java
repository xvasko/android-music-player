package com.matejvasko.player;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.matejvasko.player.paging.MediaItemDataSource;
import com.matejvasko.player.utils.Utils;

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

    public int getSongCursorSize() {
        return songCursor.getCount();
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

    private MediaItemData getMediaItemDataAtPosition(int position, int flag) {

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
            songs.add(createMediaItemData(albumSongsCursor, MediaItemDataSource.SONG_DATA_SOURCE));
        }

        return songs;
    }

    private MediaItemData createMediaItemData(Cursor cursor, int flag) {
        if (flag == MediaItemDataSource.SONG_DATA_SOURCE) {
            return new MediaItemData.Builder(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                    .setBrowseable(false)
                    .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                    .setSubtitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                    .setAlbumArtUri(Uri.parse("content://media/external/audio/albumart/" + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                    .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))
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

    @Nullable
    private String getSongSortOrder() {
        return MediaStore.Audio.Media.TITLE + " ASC";
    }

    private String getSongByIdSelection(String mediaId) {
        return "(" + MediaStore.Audio.Media._ID + "==" + mediaId + ")";
    }

    MediaMetadataCompat getMediaMetadata(String mediaId) {
        Cursor mediaItemByIdCursor = ContentResolverCompat.query(
                App.getAppContext().getContentResolver(),
                getSongUri(),
                getSongProjection(),
                getSongByIdSelection(mediaId),
                null,
                getSongSortOrder(),
                null);

        mediaItemByIdCursor.moveToNext();

        return createMediaMetadata(
                mediaItemByIdCursor.getString(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                mediaItemByIdCursor.getString(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                mediaItemByIdCursor.getString(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                mediaItemByIdCursor.getString(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                mediaItemByIdCursor.getString(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                mediaItemByIdCursor.getLong(mediaItemByIdCursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
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
