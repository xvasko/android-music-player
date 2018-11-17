package com.matejvasko.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.viewmodels.NowPlaying;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.media.MediaBrowserServiceCompat;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    private int state;
    int shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;

    private String fileName;

    private MediaProvider mediaProvider;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private MediaPlayer mediaPlayer;
    private MediaSessionCallback mediaSessionCallback;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MediaPlaybackService");

        mediaProvider = MediaProvider.getInstance();

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

        final Observer<Song> nowPlayingObserver = new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                metadataBuilder
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.data)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration);

                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, Utils.getBitmapFromMediaStore(song.iconUri));

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

    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Set the state to "paused" because it most closely matches the state
                    // in MediaPlayer with regards to available state transitions compared
                    // to "stop".
                    // Paused allows: seekTo(), start(), pause(), stop()
                    // Stop allows: stop()
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                    mediaSessionCallback.onSkipToNext();
                }
            });
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public class MediaSessionCallback extends MediaSessionCompat.Callback {
//        @Override
//        public void onPlayFromMediaId(String mediaId, Bundle extras) {
//            super.onPlayFromMediaId(mediaId, extras);
//            // TODO init mediaPlayer once
//            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                mediaPlayer.release();
//                mediaPlayer = null;
//            }
//            mediaPlayer = MediaPlayer.create(MediaPlaybackService.this, Uri.parse(mediaId));
//            mediaPlayer.start();
//
//        }

        List<Integer> queue = new ArrayList<>();

        int pointer = -1;

        @Override
        public void onPlay() {
            System.out.println("onPlay:");
            System.out.println("queue size: " + queue.size());
            System.out.println("pointer: " + pointer);
            System.out.println(queue.toString());

            String newFileName = NowPlaying.getNowPlaying().getValue().data;

            boolean mediaChanged = (fileName == null || !fileName.equals(newFileName));

            // media did not change, continue playback
            if (!mediaChanged) {
                mediaPlayer.start();
                setNewState(PlaybackStateCompat.STATE_PLAYING);
                System.out.println("Media did not change!");
                return;
            }

            if (NowPlaying.getNowPlaying().getValue().isFromSongTab()) {
                System.out.println("From Song Tab");
                queue = new ArrayList<>();
                pointer = 0;
                queue.add(NowPlaying.getNowPlaying().getValue().cursorPosition);
            } else if (NowPlaying.getNowPlaying().getValue().isFromAlbumTab()) {
                System.out.println("From Album Tab");

            } else {
                if (pointer + 1 == queue.size()) {
                    pointer++;
                    queue.add(NowPlaying.getNowPlaying().getValue().cursorPosition);
                } else if (pointer + 1 > queue.size()){
                    queue.add(NowPlaying.getNowPlaying().getValue().cursorPosition);
                }
            }

            releaseMediaPlayer();
            initializeMediaPlayer();

            fileName = newFileName;

            try {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);

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

            releaseMediaPlayer();

            mediaSession.setActive(false);
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

        Random rand = new Random();

        @Override
        public void onSkipToNext() {

            pointer++;

            if (pointer < queue.size()) {
                NowPlaying.getNowPlaying().setValue(mediaProvider.getSongAtPosition(queue.get(pointer)));
            } else {
                if (MediaPlaybackService.this.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    NowPlaying.getNowPlaying().setValue(mediaProvider.getSongAtPosition(getRandomSongPosition()));
                } else {
                    NowPlaying.getNowPlaying().setValue(mediaProvider.getSongAtPosition((queue.get(pointer - 1) + 1) % mediaProvider.getSongCursorSize()));
                }

            }

            Log.d(TAG, "onSkipToNext: " + pointer);
        }

        @Override
        public void onSkipToPrevious() {

            if (pointer > 0) {
                pointer--;
            } else {
                return;
            }

            NowPlaying.getNowPlaying().setValue(mediaProvider.getSongAtPosition(queue.get(pointer)));

            Log.d(TAG, "onSkipToPrevious: " + pointer);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);

            MediaPlaybackService.this.shuffleMode = shuffleMode;
            Log.d(TAG, "onSetShuffleMode: ");
        }

        private int getRandomSongPosition() {
            return rand.nextInt(mediaProvider.getSongCursorSize());
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

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        Log.d(TAG,"onLoadChildren: 2 params");

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new MediaBrowserCompat.MediaItem(new MediaDescriptionCompat.Builder().setMediaId("12").setTitle("ALBUM TITLE").build(), 0));
        result.sendResult(mediaItems);

    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result, @NonNull Bundle options) {
        result.detach();

        if (!(options.containsKey(MediaBrowserCompat.EXTRA_PAGE) && options.containsKey(MediaBrowserCompat.EXTRA_PAGE_SIZE)))
            return;

        int page     = options.getInt(MediaBrowserCompat.EXTRA_PAGE);
        int pageSize = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE);
        options.putInt("songs_count", mediaProvider.getSongCursorSize());

        List<Song> songs = getSongsPage(page, pageSize);

        List<MediaBrowserCompat.MediaItem> mediaItems = Utils.mapToMediaItems(songs);
        result.sendResult(mediaItems);

        Log.d(TAG, "onLoadChildren: ");
    }

    private List<Song> getSongsPage(int page, int pageSize) {
        int startPosition = page * pageSize;
        if (startPosition + pageSize <= mediaProvider.getSongCursorSize())
            return mediaProvider.getSongsAtRange(startPosition, startPosition + pageSize);
        else
            return mediaProvider.getSongsAtRange(startPosition, mediaProvider.getSongCursorSize());
    }

}
