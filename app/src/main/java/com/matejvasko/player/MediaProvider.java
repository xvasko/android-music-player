package com.matejvasko.player;

import android.database.Cursor;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.matejvasko.player.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    List<MediaBrowserCompat.MediaItem> getSongsAtRange(int startPosition, int endPosition) {
        List<MediaBrowserCompat.MediaItem> songs = new ArrayList<>();
        for (int position = startPosition; position < endPosition; ++position) {
            MediaBrowserCompat.MediaItem mediaItem = getSongAtPosition(position);
            if (mediaItem != null)
                songs.add(mediaItem);
        }

        return songs;
    }

    public MediaBrowserCompat.MediaItem getSongAtPosition(int position) {
        if (!songCursor.moveToPosition(position)) {
            System.out.println("THERE IS NOTHING AT POSITION: " + position);
            return null;
        }
        return createMediaItemData(songCursor);
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



    private MediaBrowserCompat.MediaItem createMediaItemData(Cursor cursor) {
        MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                .setMediaId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                .setSubtitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))
                .setIconUri(Uri.parse("content://media/external/audio/albumart/" + cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))))
                .build();
        MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescription, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

        return mediaItem;
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

    private String getSongByIdSelection(String mediaId) {
        return "(" + MediaStore.Audio.Media._ID + "==" + mediaId + ")";
    }

    public MediaMetadataCompat getMediaMetadata(String mediaId) {
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
