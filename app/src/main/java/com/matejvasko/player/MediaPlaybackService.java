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

import com.matejvasko.player.paging.MediaItemDataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // User has swiped the app in task manager
        stopSelf();
    }

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
    String newFileName;
    int currentSongPosition;
    boolean playningAlbum;

    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        QueueManager queueManager = new QueueManager();

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);

            if (action.equals("playSongFromAlbum")) {
                playningAlbum = true;


            } else if (action.equals("playSong")) {
                playningAlbum = false;

            } else {
                throw new IllegalArgumentException();
            }

            currentSongPosition = extras.getInt("cursor_position");

            System.out.println("currentSongPosition: " + currentSongPosition);

            queueManager.resetQueue();
            queueManager.addItem(currentSongPosition);

            setMediaSessionMetadata(currentSongPosition);
            onPlay();

        }

        @Override
        public void onPlay() {
            boolean mediaChanged = (fileName == null || !fileName.equals(newFileName));
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

            fileName = newFileName;

            try {
                mediaPlayer.setDataSource(fileName);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
            setNewMetadata(mediaMetadata);

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
            setMediaSessionMetadata(queueManager.skipToNext());
            onPlay();
            Log.d(TAG, "onSkipToNext: ");
        }

        @Override
        public void onSkipToPrevious() {
            int cursorPosition = queueManager.skipToPrevious();
            if (cursorPosition >= 0) {
                setMediaSessionMetadata(cursorPosition);
                onPlay();
            }
            Log.d(TAG, "onSkipToPrevious: ");
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            MediaPlaybackService.this.shuffleMode = shuffleMode;
            queueManager.resetQueueFromShuffle();
            Log.d(TAG, "onSetShuffleMode: " + shuffleMode);
        }

        private void setMediaSessionMetadata(int cursorPosition) {
            mediaMetadata = mediaProvider.getMediaMetadata(cursorPosition, playningAlbum);
            mediaSession.setMetadata(mediaMetadata);
            newFileName = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
        }
    }

    private void setNewState(@PlaybackStateCompat.State int newState) {
        state = newState;
        final long reportPosition;

        reportPosition = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

        stateBuilder.setState(newState, reportPosition,1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
        onPlaybackStateChange(stateBuilder.build());
    }

    private void setNewMetadata(MediaMetadataCompat mediaMetadata){
        mediaSession.setMetadata(mediaMetadata);
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
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }
       return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        Log.d(TAG,"onLoadChildren: 2 params");

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

        List<Integer> queue = new ArrayList<>();
        Random rand = new Random();
        int pointer = -1;

        int skipToNext() {

            pointer++;

            int cursorPosition;
            if (pointer < queue.size()) {
                cursorPosition = queue.get(pointer);
                currentSongPosition = cursorPosition;
                return cursorPosition;
            } else {
                if (shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    cursorPosition = getRandomSongPosition();
                } else {
                    cursorPosition = getSongAtPosition(currentSongPosition + 1);
                }
                currentSongPosition = cursorPosition;
                queue.add(cursorPosition);

                return cursorPosition;
            }
        }

        int skipToPrevious() {

            if (pointer <= 0) {
                return -1;
            }

            pointer--;
            int cursorPosition = queue.get(pointer);
            currentSongPosition = cursorPosition;

            return cursorPosition;
        }

        private int getRandomSongPosition() {
            if (playningAlbum) {
                return mediaProvider.getSongFromAlbum(rand.nextInt(mediaProvider.getAlbumSongCursorSize())).cursorPosition;
            } else {
                return Objects.requireNonNull(mediaProvider.getMediaItemDataAtPosition(
                        rand.nextInt(mediaProvider.getSongCursorSize()),
                        MediaItemDataSource.SONG_DATA_SOURCE)).cursorPosition;
            }
        }

        private int getSongAtPosition(int position) {
            if (playningAlbum) {
                return mediaProvider.getSongFromAlbum(position % mediaProvider.getAlbumSongCursorSize()).cursorPosition;
            } else {
                return Objects.requireNonNull(mediaProvider.getMediaItemDataAtPosition(
                        (position % mediaProvider.getSongCursorSize()),
                        MediaItemDataSource.SONG_DATA_SOURCE)).cursorPosition;
            }
        }

        void resetQueue() {
            if (queue.size() != 0) {
                pointer = -1;
                queue = new ArrayList<>();
            }
        }

        void resetQueueFromShuffle() {
            int cursorPosition = queue.get(pointer);
            resetQueue();
            addItem(cursorPosition);
        }

        void addItem(int cursorPosition) {
            pointer++;
            queue.add(cursorPosition);

        }

    }

}
