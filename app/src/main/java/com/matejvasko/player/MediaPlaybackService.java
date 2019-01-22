package com.matejvasko.player;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.matejvasko.player.models.Song;
import com.matejvasko.player.utils.SharedPref;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    private int state;
    int shuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;

    private MediaProvider mediaProvider;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private MediaPlayer mediaPlayer;
    private MediaSessionCallback mediaSessionCallback;
    private ServiceManager serviceManager;
    private MediaNotificationManager mediaNotificationManager;
    private boolean serviceInStartedState;

    private SharedPref sharedPref = SharedPref.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MediaPlaybackService");

        serviceManager = new ServiceManager();
        mediaNotificationManager = new MediaNotificationManager(this);
        mediaProvider = MediaProvider.getInstance();

        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSessionCallback = new MediaSessionCallback();
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSession.setPlaybackState(stateBuilder.build());

        setSessionToken(mediaSession.getSessionToken());
    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        // User has swiped the app in task manager
//        System.out.println("onTaskRemoved, calling stop self");
//        stopSelf();
//    }

//    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
//        @Override
//        public void onAudioFocusChange(int focusChange) {
//            Log.d(TAG, "onAudioFocusChange:");
//            switch (focusChange) {
//                case AudioManager.AUDIOFOCUS_GAIN:
//                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_GAIN"); // this is what you get when returning from transient losses
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"); // lower the volume temporarily (i.e.: notification)
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS_TRANSIENT"); // other app is asking you to pause temporarily
//                    break;
//                case AudioManager.AUDIOFOCUS_LOSS:
//                    Log.d(TAG, "onAudioFocusChange: AUDIOFOCUS_LOSS"); // stop playback
//                    break;
//            }
//        }
//    };

    private void initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
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

    MediaMetadataCompat mediaMetadata;
    String fileName;
    Song song;

    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        QueueManager queueManager = new QueueManager();


        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);

            song = sharedPref.getSong();
            setMediaSessionMetadata(song);

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                queueManager.resetQueue();
                queueManager.addItem(song.cursorPosition);
            }

            onPlay();
            Log.d(TAG, "onCustomAction:");
        }

        /**
         * On explicit item click.
         */
        @Override
        public void onPrepare() {

        }

        @Override
        public void onPlay() {
            boolean mediaChanged = (fileName == null || !fileName.equals(song.fileName));
            mediaSession.setActive(true);

            // media did not change, continue playback
            if (!mediaChanged) {
                mediaPlayer.start();
                setNewState(PlaybackStateCompat.STATE_PLAYING);
                System.out.println("Media did not change!");
                return;
            }

            releaseMediaPlayer();
            initializeMediaPlayer();

            fileName = song.fileName;

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

        @Override
        public void onSkipToNext() {

            Song nextSong;

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                nextSong = queueManager.skipToNext();
            } else {
                if (song.isFromAlbum) {
                    nextSong = mediaProvider.getAlbumSongs(song.albumId).get((song.cursorPosition + 1) % mediaProvider.getAlbumSongCursorSize());
                } else {
                    nextSong = mediaProvider.getSongAtPosition((song.cursorPosition + 1) % mediaProvider.getSongCursorSize());
                }
            }

            song = nextSong;
            sharedPref.setSong(nextSong);

            setMediaSessionMetadata(nextSong);
            onPlay();
            Log.d(TAG, "onSkipToNext: ");
        }

        @Override
        public void onSkipToPrevious() {

            Song previousSong;

            if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                previousSong = queueManager.skipToPrevious();
                if (previousSong == null) {
                    return;
                }
            } else {
                int position = song.cursorPosition - 1;
                if (song.isFromAlbum) {
                    if (song.cursorPosition == 0) {
                        position = mediaProvider.getAlbumSongCursorSize() - 1;
                    }
                    previousSong = mediaProvider.getAlbumSongs(song.albumId).get(position);
                } else {
                    if (song.cursorPosition == 0) {
                        position = mediaProvider.getSongCursorSize() - 1;
                    }
                    previousSong = mediaProvider.getSongAtPosition(position);
                }
            }

            song = previousSong;
            sharedPref.setSong(previousSong);

            setMediaSessionMetadata(previousSong);
            onPlay();
            Log.d(TAG, "onSkipToPrevious: ");
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            MediaPlaybackService.this.shuffleMode = shuffleMode;
            queueManager.resetQueueFromShuffle();
            Log.d(TAG, "onSetShuffleMode: " + shuffleMode);
        }

        private void setMediaSessionMetadata(Song song) {
            mediaMetadata = song.getMetadata();
            mediaSession.setMetadata(mediaMetadata);
        }
    }

    private void setNewState(@PlaybackStateCompat.State int newState) {
        state = newState;
        final long reportPosition;

        reportPosition = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

        stateBuilder.setState(newState, reportPosition, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
        onPlaybackStateChange(stateBuilder.build());
    }

    private void onPlaybackStateChange(PlaybackStateCompat state) {
        // Manage the started state of this service.
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                serviceManager.moveServiceToStartedState(state);
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                serviceManager.updateNotificationForPause(state);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                serviceManager.moveServiceOutOfStartedState();
                break;
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // https://www.youtube.com/watch?v=iIKxyDRjecU 39:00
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        Log.d(TAG, "onLoadChildren: 2 params");

        result.sendResult(null);
    }

    private class ServiceManager {

        private void moveServiceToStartedState(PlaybackStateCompat state) {
            Notification notification =
                    mediaNotificationManager.getNotification(mediaMetadata, state, getSessionToken());

            if (!serviceInStartedState) {
                ContextCompat.startForegroundService(
                        MediaPlaybackService.this,
                        new Intent(MediaPlaybackService.this, MediaPlaybackService.class));
                serviceInStartedState = true;
            }

            startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void updateNotificationForPause(PlaybackStateCompat state) {
            stopForeground(false);
            Notification notification = mediaNotificationManager.getNotification(mediaMetadata, state, getSessionToken());
            mediaNotificationManager.getNotificationManager().notify(MediaNotificationManager.NOTIFICATION_ID, notification);
        }

        private void moveServiceOutOfStartedState() {
            stopForeground(true);
            stopSelf();
            serviceInStartedState = false;
        }
    }

    private class QueueManager {

        // TODO save queue to shared preferences as set and retrieve it after service is recreated
        // TODO cut the queue so it won't grow to unbearable sizes
        List<Integer> queue = new ArrayList<>();
        Random rand = new Random();
        int pointer = -1;

        Song skipToNext() {

            pointer++;

            if (pointer < queue.size()) {
                if (song.isFromAlbum) {
                    return mediaProvider.getAlbumSongs(song.albumId).get(queue.get(pointer));
                } else {
                    return mediaProvider.getSongAtPosition(queue.get(pointer));
                }
            } else {
                if (song.isFromAlbum) {
                    int randomSongPosition = getRandomSongPosition();
                    queue.add(randomSongPosition);
                    return mediaProvider.getAlbumSongs(song.albumId).get(randomSongPosition);
                } else {
                    int randomSongPosition = getRandomSongPosition();
                    queue.add(randomSongPosition);
                    return mediaProvider.getSongAtPosition(randomSongPosition);
                }
            }

        }

        Song skipToPrevious() {

            if (pointer <= 0) {
                return null;
            }

            pointer--;

            if (song.isFromAlbum) {
                return mediaProvider.getAlbumSongs(song.albumId).get(queue.get(pointer));
            } else {
                return mediaProvider.getSongAtPosition(queue.get(pointer));
            }
        }

        private int getRandomSongPosition() {
            if (song.isFromAlbum) {
                return mediaProvider.getAlbumSongs(song.albumId).get(rand.nextInt(mediaProvider.getAlbumSongCursorSize())).cursorPosition;
            } else {
                return mediaProvider.getSongAtPosition(rand.nextInt(mediaProvider.getSongCursorSize())).cursorPosition;
            }
        }

        void resetQueue() {
            pointer = -1;
            queue = new ArrayList<>();
        }

        void resetQueueFromShuffle() {
            resetQueue();
            addItem(song.cursorPosition);
        }

        void addItem(int cursorPosition) {
            pointer++;
            queue.add(cursorPosition);
        }
    }

}
