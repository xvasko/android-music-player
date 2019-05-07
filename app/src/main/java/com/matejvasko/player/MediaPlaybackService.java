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

import com.matejvasko.player.firebase.FirebaseDatabaseManager;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.utils.SharedPref;
import com.matejvasko.player.workmanager.UploadWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String TAG = MediaPlaybackService.class.getSimpleName();

    private int state;

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
        int progress = sharedPref.getSong() != null ? (int) sharedPref.getSong().progress : 0;
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .setState(PlaybackStateCompat.STATE_PAUSED, progress, 1.0f);
        metadataBuilder = new MediaMetadataCompat.Builder();
        mediaSession.setPlaybackState(stateBuilder.build());

        setSessionToken(mediaSession.getSessionToken());
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
    Song song;

    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        QueueManager queueManager = new QueueManager();

        /**
         * On explicit item click.
         */
        @Override
        public void onCustomAction(String action, Bundle extras) {
            song = sharedPref.getSong();
            setMediaSessionMetadata(song);

            if (sharedPref.isShuffle()) {
                queueManager.resetQueue();
                queueManager.addItem(song.cursorPosition);
            }

            onPlay();
            Log.d(TAG, "onCustomAction:");
        }

        @Override
        public void onPrepare() {
            song = sharedPref.getSong();
            if (song != null) {
                setMediaSessionMetadata(song);
            }
            Log.d(TAG, "onPrepare: ");
        }

        @Override
        public void onPlay() {
            mediaSession.setActive(true);

            // absolutely necessary to reinitialize media player every time play is called due to
            // scenario: play song, go to another app, pause from notification, swipe to stop service, open app, continue playing
            releaseMediaPlayer();
            initializeMediaPlayer();

            try {
                mediaPlayer.setDataSource(song.fileName);
                mediaPlayer.prepare();
                mediaPlayer.seekTo((int) song.progress);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);

            // delay upload of current song to DB
            uploadCurrentSong();
            FirebaseDatabaseManager.setUserOnline(true);

            Log.d(TAG, "onPlay: MediaSessionCallback");
        }

        private void uploadCurrentSong() {
            WorkManager.getInstance().cancelAllWorkByTag("upload");
            Data songData = new Data.Builder()
                    .putString("name", song.title)
                    .putString("artist", song.artist)
                    .build();
            OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .setInputData(songData)
                    .addTag("upload")
                    .build();
            WorkManager.getInstance().enqueue(uploadWorkRequest);
        }

        @Override
        public void onPause() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Song song1 = sharedPref.getSong();
                song1.progress = mediaSession.getController().getPlaybackState().getPosition();
                sharedPref.setSong(song1);
                song = sharedPref.getSong();
                setNewState(PlaybackStateCompat.STATE_PAUSED);
            }

            FirebaseDatabaseManager.setUserOnline(false);

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
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo((int) pos);
                }
                setNewState(state);
            } else {
                song.progress = pos;
            }
        }

        @Override
        public void onSkipToNext() {

            Song nextSong;

            if (sharedPref.isShuffle()) {
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

            if (sharedPref.isShuffle()) {
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
            boolean isShuffle = shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL;
            sharedPref.setShuffle(isShuffle);
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

        reportPosition = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : sharedPref.getSong().progress;

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
