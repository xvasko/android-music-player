package com.matejvasko.player.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.matejvasko.player.App;
import com.matejvasko.player.LocationService;
import com.matejvasko.player.MediaPlaybackService;
import com.matejvasko.player.MediaSeekBar;
import com.matejvasko.player.R;
import com.matejvasko.player.fragments.library.AlbumsFragmentI;
import com.matejvasko.player.fragments.library.SongsFragmentI;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.utils.SharedPref;
import com.matejvasko.player.utils.Utils;
import com.matejvasko.player.viewmodels.MainActivityViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;
    private View navHostFragment;

    public MainActivityViewModel viewModel;

    View bottomSheetOnClickView;
    RelativeLayout bottomSheet;
    ImageView albumArtImageView;
    TextView songTitleTextView;
    ImageView playPauseImageView;
    ImageView playPauseBigImageView;
    ImageView shuffleImageView;
    ImageView collapseImageView;
    Button playPauseButton;
    Button playPauseButtonBig;
    Button skipNextButton;
    Button skipPreviousButton;
    Button shuffleButton;
    Button collapseButton;
    MediaSeekBar mediaSeekBarIndicator;
    MediaSeekBar mediaSeekBar;
    View backgroundDimmer;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaControllerCallback mediaControllerCallback;

    private boolean isPlaying;
    private boolean isShuffle;

    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = SharedPref.getInstance();
        isShuffle = sharedPref.isShuffle();
        ClickListener clickListener = new ClickListener();

        // setup bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
//        BottomNavViewHelper.removeShiftMode(bottomNav);
        navHostFragment = findViewById(R.id.nav_host_fragment);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);

        backgroundDimmer = findViewById(R.id.background_dimmer);
        backgroundDimmer.setOnClickListener(clickListener);

        bottomSheetOnClickView = findViewById(R.id.bottom_sheet_on_click);
        bottomSheetOnClickView.setOnClickListener(clickListener);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        albumArtImageView = findViewById(R.id.album_art_image_view);
        songTitleTextView = findViewById(R.id.song_title_text_view);
        songTitleTextView.setSelected(true);
        playPauseImageView = findViewById(R.id.play_pause_image_view);
        playPauseBigImageView = findViewById(R.id.play_pause_image_view_big);
        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(clickListener);
        playPauseButtonBig = findViewById(R.id.play_pause_button_big);
        playPauseButtonBig.setOnClickListener(clickListener);
        collapseImageView = findViewById(R.id.collapse_image_view);
        collapseButton = findViewById(R.id.collapse_button);
        collapseButton.setOnClickListener(clickListener);
        skipNextButton = findViewById(R.id.skip_next_button);
        skipNextButton.setOnClickListener(clickListener);
        skipPreviousButton = findViewById(R.id.skip_previous_button);
        skipPreviousButton.setOnClickListener(clickListener);
        shuffleButton = findViewById(R.id.shuffle_button);
        shuffleButton.setOnClickListener(clickListener);
        shuffleImageView = findViewById(R.id.shuffle_image_view);
        shuffleImageView.setImageResource(isShuffle ? R.drawable.ic_shuffle_primary_24dp : R.drawable.ic_shuffle_black_24dp);
        mediaSeekBarIndicator = findViewById(R.id.media_seek_bar_indicator);
        mediaSeekBarIndicator.setPadding(0, 0, 0, 0);
        mediaSeekBarIndicator.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mediaSeekBarIndicator.setThumb(null);
        mediaSeekBar = findViewById(R.id.media_seek_bar);
        mediaSeekBar.setTextViews(
                (TextView) findViewById(R.id.duration_current),
                (TextView) findViewById(R.id.duration_total)
        );

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mediaControllerCallback = new MediaControllerCallback();
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        sharedPref.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
                        playPauseButton.setEnabled(true);
                        collapseButton.setEnabled(false);
                        Log.d(TAG, "onStateChanged: STATE_COLLAPSED");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        sharedPref.setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        playPauseButton.setEnabled(false);
                        collapseButton.setEnabled(true);
                        Log.d(TAG, "onStateChanged: STATE_EXPANDED");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d(TAG, "onStateChanged: STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d(TAG, "onStateChanged: STATE_SETTLING");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d(TAG, "onStateChanged: STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        Log.d(TAG, "onStateChanged: STATE_HALF_EXPANDED");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (v == 0.0) {
                    backgroundDimmer.setVisibility(View.INVISIBLE);
                    mediaSeekBarIndicator.setVisibility(View.VISIBLE);
                } else if (backgroundDimmer.getVisibility() != View.VISIBLE && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                    backgroundDimmer.setVisibility(View.VISIBLE);
                } else {
                    mediaSeekBarIndicator.setVisibility(View.INVISIBLE);
                }

                backgroundDimmer.setAlpha(v);

                if (v > 0.5) {
                    collapseImageView.setAlpha((v - 0.5f) * 2);
                    playPauseImageView.setAlpha(0f);
                } else {
                    playPauseImageView.setAlpha(1 - 2 * v);
                    collapseImageView.setAlpha(0f);
                }
            }
        });

        Log.d(TAG, "onCreate: ");
    }

    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.play_pause_button:
                    if (isPlaying) {
                        mediaController.getTransportControls().pause();
                    } else {
                        mediaController.getTransportControls().play();
                    }
                    break;
                case R.id.skip_previous_button:
                    mediaController.getTransportControls().skipToPrevious();
                    break;
                case R.id.play_pause_button_big:
                    if (isPlaying) {
                        mediaController.getTransportControls().pause();
                    } else {
                        mediaController.getTransportControls().play();
                    }
                    break;
                case R.id.skip_next_button:
                    mediaController.getTransportControls().skipToNext();
                    break;
                case R.id.shuffle_button:
                    if (isShuffle) {
                        mediaController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                        isShuffle = false;
                    } else {
                        mediaController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                        isShuffle = true;
                    }
                    shuffleImageView.setImageResource(isShuffle ? R.drawable.ic_shuffle_primary_24dp : R.drawable.ic_shuffle_black_24dp);
                    break;
                case R.id.bottom_sheet_on_click:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    break;
                case R.id.background_dimmer:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
                case R.id.collapse_button:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }

    public void playSong(Song song) {
        sharedPref.setSong(song);
        handleBottomSheetBehaviour();
        songTitleTextView.setSelected(true);
        mediaController.getTransportControls().sendCustomAction("play", null);
    }

    private void handleBottomSheetBehaviour() {
        if (sharedPref.getSong() == null) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            bottomSheetBehavior.setHideable(false);
            ((ViewGroup.MarginLayoutParams) navHostFragment.getLayoutParams()).bottomMargin = Utils.densityPixelToPixel(56);
            if (sharedPref.getBottomSheetState() == BottomSheetBehavior.STATE_EXPANDED) {
                playPauseImageView.setAlpha(0f);
                collapseImageView.setAlpha(1f);
                backgroundDimmer.setVisibility(View.VISIBLE);
                playPauseButton.setEnabled(false);
                collapseButton.setEnabled(true);
                mediaSeekBarIndicator.setVisibility(View.INVISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else if (sharedPref.getBottomSheetState() == BottomSheetBehavior.STATE_COLLAPSED) {
                playPauseImageView.setAlpha(1f);
                collapseImageView.setAlpha(0f);
                backgroundDimmer.setVisibility(View.INVISIBLE);
                playPauseButton.setEnabled(true);
                collapseButton.setEnabled(false);
                mediaSeekBarIndicator.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaBrowser == null) {
            if (isStoragePermissionGranted()) {
                createMediaBrowser();
            }
        }

        handleBottomSheetBehaviour();

        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaSeekBar.disconnectMediaController();
        mediaSeekBarIndicator.disconnectMediaController();
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaController = null;
        }
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
            mediaBrowser = null;
        }

        Log.d(TAG, "onStop: ");
    }

    private void createMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaPlaybackService.class),
                new MediaBrowserConnectionCallback(),
                null);
        mediaBrowser.connect();
        Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
    }

    private SongsFragmentI listener1;
    private AlbumsFragmentI listener2;

    public void setListener1(SongsFragmentI listener) {
        this.listener1 = listener;
    }

    public void setListener2(AlbumsFragmentI listener) {
        this.listener2 = listener;
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicPlaybackService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            try {
                listener1.loadSongs();
                listener2.loadAlbums();
                mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                mediaSeekBar.setMediaController(mediaController);
                mediaSeekBarIndicator.setMediaController(mediaController);
                mediaController.registerCallback(mediaControllerCallback);

                if (sharedPref.getSong() != null) {
                    mediaController.getTransportControls().prepare(); // set Metadata
                }
                // media metadata is retrieved from shared preferences inside MediaPlaybackService.java
                mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
                Log.d(TAG, "onConnected: mediaController.getPlaybackState(): mediaController.getPlaybackState()");

                // enables handling of media buttons
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "onConnected: MediaBrowserConnectionCallback");
        }
    }

    // Receives callbacks from the MediaController and updates the UI state,
    // i.e.: Which is the current item, whether it's playing or paused, etc.
    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            if (metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART) == null) {
                albumArtImageView.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.ic_audiotrack_black_24dp));
            } else {
                albumArtImageView.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            }

            String title = metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE).toString();
            String artist = metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST).toString();

            songTitleTextView.setText(
                    String.format("%s   %s   %s",
                            title,
                            String.valueOf(Html.fromHtml("&#8226;")),
                            artist
                    ));

            Log.d(TAG, "onMetadataChanged: MediaControllerCallback");
        }

        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
            isPlaying = state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING;
            playPauseImageView.setImageDrawable(isPlaying ? getResources().getDrawable(R.drawable.ic_pause_black_24dp) : getResources().getDrawable(R.drawable.ic_play_arrow_black_24dp));
            playPauseBigImageView.setImageResource(isPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
            Log.d(TAG, "onPlaybackStateChanged: MediaControllerCallback + " + state);
        }

        // This might happen if the MusicService is killed while the Activity is in the
        // foreground and onStart() has been called (but not onStop()).
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();

            Log.d(TAG, "onSessionDestroyed: MediaControllerCallback: ");
        }

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);
                    createMediaBrowser();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        stopService(new Intent(App.getAppContext(), LocationService.class));
        super.onDestroy();
    }
}
