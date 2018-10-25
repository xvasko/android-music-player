package com.matejvasko.player;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;

import androidx.lifecycle.Observer;
import androidx.media.MediaBrowserServiceCompat;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.matejvasko.player.viewmodels.NowPlaying;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    private int state;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private MediaPlayer mediaPlayer;
    private MediaSessionCallback mediaSessionCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MediaPlaybackService");

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSessionCallback = new MediaSessionCallback();
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSession.setPlaybackState(stateBuilder.build());

        setSessionToken(mediaSession.getSessionToken());

        final Observer<NowPlaying.Song> nowPlayingObserver = new Observer<NowPlaying.Song>() {
            @Override
            public void onChanged(NowPlaying.Song song) {
                metadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.songUri)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.songTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistTitle)
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, song.albumArt)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.songDuration);
                mediaSession.setMetadata(metadataBuilder.build());
                mediaSessionCallback.onPlay();
            }
        };

        NowPlaying.getNowPlaying().observeForever(nowPlayingObserver);
    }

    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "onAudioFocusChange:");
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN"); // this is what you get when returning from transient losses
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"); // lower the volume temporarily (i.e.: notification)
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT"); // other app is asking you to pause temporarily
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS"); // stop playback
                    break;
            }
        }
    };

    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            // TODO init mediaPlayer once
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
                mediaSession.setActive(true);  // we want to be active media button receiver (accepting i.e: headphones media buttons)
                mediaPlayer = MediaPlayer.create(MediaPlaybackService.this, Uri.parse(NowPlaying.getNowPlaying().getValue().songUri));
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        // Set the state to "paused" because it most closely matches the state
                        // in MediaPlayer with regards to available state transitions compared
                        // to "stop".
                        // Paused allows: seekTo(), start(), pause(), stop()
                        // Stop allows: stop()
                        setNewState(PlaybackStateCompat.STATE_PAUSED);
                    }
                });
                mediaPlayer.start();

                setNewState(PlaybackStateCompat.STATE_PLAYING);
            }

            Log.d(TAG, "onPlay: MediaSessionCallback");
        }

        @Override
        public void onPause() {
            mediaPlayer.pause();
            setNewState(PlaybackStateCompat.STATE_PAUSED);

            Log.d(TAG, "onPause: MediaSessionCallback");
        }

        @Override
        public void onStop() {
            // abandon audio focus here
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            mediaSession.setActive(false);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            setNewState(PlaybackStateCompat.STATE_STOPPED);

            Log.d(TAG, "onStop: MediaSessionCallback");
        }

        @Override
        public void onSeekTo(long pos) {
            if (mediaPlayer != null) {
                mediaPlayer.seekTo((int) pos);
            }

            setNewState(state);
        }

    }

    private void setNewState(@PlaybackStateCompat.State int newState) {
        state = newState;
        final long reportPosition;

        reportPosition = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

        stateBuilder.setState(newState, reportPosition,1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // https://www.youtube.com/watch?v=iIKxyDRjecU 39:00
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
       return null;
    }

    private CursorBasedMediaProvider mediaProvider;

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG,"onLoadChildren");
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result, @NonNull Bundle options) {
        result.detach();

        if (!(options.containsKey(MediaBrowserCompat.EXTRA_PAGE) && options.containsKey(MediaBrowserCompat.EXTRA_PAGE_SIZE)))
            return;

        // TODO create just once
        mediaProvider = new CursorBasedMediaProvider(this);

        int page     = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
        int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
        List<Song> songs = getSongsPage(page, pageSize);

        List<MediaBrowserCompat.MediaItem> mediaItems = mapToMediaItems(songs);
        result.sendResult(mediaItems);

        Log.d(TAG, "onLoadChildren: " + result);
    }

    private List<Song> getSongsPage(int page, int pageSize) {
        int startPosition = page * pageSize;
        if (startPosition + pageSize <= mediaProvider.getMediaSize())
            return mediaProvider.getSongsAtRange(startPosition, startPosition + pageSize);
        else
            return mediaProvider.getSongsAtRange(startPosition, mediaProvider.getMediaSize());
    }

    private List<MediaBrowserCompat.MediaItem> mapToMediaItems(List<Song> songs) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                    .setTitle(song.title)
                    .setMediaId(song.filePath)
                    .build();
            MediaBrowserCompat.MediaItem mediaItem = new MediaBrowserCompat.MediaItem(mediaDescription, 0);
            mediaItems.add(mediaItem);
        }

        return mediaItems;
    }

}
