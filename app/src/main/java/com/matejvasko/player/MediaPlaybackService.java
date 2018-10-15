package com.matejvasko.player;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MediaPlaybackService");

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        setSessionToken(mediaSession.getSessionToken());
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
            Log.d(TAG, "onPlay: MediaSessionCallback");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
                mediaSession.setActive(true);  // we want to be active media button receiver (accepting i.e: headphones media buttons)
                mediaPlayer = MediaPlayer.create(MediaPlaybackService.this, R.raw.jazz_in_paris);
                mediaPlayer.start();

                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING, 0l, 1f);
                mediaSession.setPlaybackState(stateBuilder.build());
            }
        }

        @Override
        public void onPause() {
            mediaPlayer.pause();
            stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED, 0l, 1f);
            mediaSession.setPlaybackState(stateBuilder.build());
            Log.d(TAG, "onPause: MediaSessionCallback");
        }

        @Override
        public void onStop() {
            // abandon audio focus here
            Log.d(TAG, "onStop: MediaSessionCallback");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(onAudioFocusChangeListener);
            mediaSession.setActive(false);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
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

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }
}
