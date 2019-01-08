package com.matejvasko.player;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.matejvasko.player.fragments.FriendsFragment;
import com.matejvasko.player.fragments.MapFragment;
import com.matejvasko.player.fragments.library.AlbumsFragmentI;
import com.matejvasko.player.fragments.library.LibraryFragment;
import com.matejvasko.player.fragments.library.SongsFragmentI;
import com.matejvasko.player.viewmodels.MainActivityViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;

    public MainActivityViewModel viewModel;

    BottomNavigationView bottomNav;
    RelativeLayout fragmentContainer;

    View bottomSheetOnClickView;
    RelativeLayout bottomSheet;
    ImageView albumArtImageView;
    TextView songTitleTextView;
    ImageView playPauseImageView;
    ImageView playPauseBigImageView;
    ImageView shuffleImageView;
    Button playPauseButton;
    Button playPauseButtonBig;
    Button skipNextButton;
    Button skipPreviousButton;
    Button shuffleButton;
    MediaSeekBar mediaSeekBar;

    private LibraryFragment libraryFragment;
    private FriendsFragment friendsFragment;
    private MapFragment mapFragment;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaControllerCallback mediaControllerCallback;

    private boolean isPlaying;
    private boolean isShuffle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ClickListener clickListener = new ClickListener();

        bottomNav = findViewById(R.id.bottom_nav);
        fragmentContainer = findViewById(R.id.fragment_container);

        bottomSheetOnClickView = findViewById(R.id.bottom_sheet_on_click);
        bottomSheetOnClickView.setOnClickListener(clickListener);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        albumArtImageView = findViewById(R.id.album_art_image_view);
        songTitleTextView = findViewById(R.id.song_title_text_view);
        playPauseImageView = findViewById(R.id.play_pause_image_view);
        playPauseBigImageView = findViewById(R.id.play_pause_image_view_big);
        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(clickListener);
        playPauseButtonBig = findViewById(R.id.play_pause_button_big);
        playPauseButtonBig.setOnClickListener(clickListener);
        skipNextButton = findViewById(R.id.skip_next_button);
        skipNextButton.setOnClickListener(clickListener);
        skipPreviousButton = findViewById(R.id.skip_previous_button);
        skipPreviousButton.setOnClickListener(clickListener);
        shuffleButton = findViewById(R.id.shuffle_button);
        shuffleButton.setOnClickListener(clickListener);
        shuffleImageView = findViewById(R.id.shuffle_image_view);
        mediaSeekBar = findViewById(R.id.media_seek_bar);
        mediaSeekBar.setPadding(0, 16, 0, 16);
        mediaSeekBar.setTextViews(
                (TextView) findViewById(R.id.duration_current),
                (TextView)findViewById(R.id.duration_total)
        );

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        mediaControllerCallback = new MediaControllerCallback();

        libraryFragment = new LibraryFragment();
        friendsFragment = new FriendsFragment();
        mapFragment = new MapFragment();

        setFragment(libraryFragment);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_library:
                        setFragment(libraryFragment);
                        return true;
                    case R.id.bottom_nav_friends:
                        setFragment(friendsFragment);
                        return true;
                    case R.id.bottom_nav_map:
                        setFragment(mapFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        playPauseImageView.setVisibility(View.VISIBLE);
                        playPauseButton.setEnabled(true);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        playPauseImageView.setVisibility(View.INVISIBLE);
                        playPauseButton.setEnabled(false);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    public void customAction(String action, MediaItemData mediaItemData) {
        Bundle bundle = new Bundle();
        bundle.putInt("cursor_position", mediaItemData.cursorPosition);
        mediaController.getTransportControls().sendCustomAction(action, bundle);
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

        Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
    }

    private void createMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MediaPlaybackService.class),
                new MediaBrowserConnectionCallback(),
                null);
        mediaBrowser.connect();
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: " + permissions[0] + " was " + grantResults[0]);
            createMediaBrowser();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaSeekBar.disconnectMediaController();
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaController = null;
        }
        if (mediaBrowser != null && mediaBrowser.isConnected()) {
            mediaBrowser.disconnect();
            mediaBrowser = null;
        }

        Log.d(TAG, "onStop:");
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
//                listener2.loadAlbums();
                mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                mediaSeekBar.setMediaController(mediaController);
                mediaController.registerCallback(mediaControllerCallback);

                // enables handling of media buttons
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                // Display the initial state
                MediaMetadataCompat metadata = mediaController.getMetadata();
                PlaybackStateCompat plabackState = mediaController.getPlaybackState();

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

            songTitleTextView.setText(
                    String.format("%s   %s   %s",
                    metadata.getText(MediaMetadataCompat.METADATA_KEY_TITLE),
                    String.valueOf(Html.fromHtml("&#8226;")),
                    metadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST)
            ));

            Log.d(TAG, "onMetadataChanged: MediaControllerCallback + " + metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
        }

        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
            isPlaying = state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING;
            playPauseImageView.setImageResource(isPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);
            playPauseBigImageView.setImageResource(isPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp);

            Log.d(TAG, "onPlaybackStateChanged: MediaControllerCallback + " + state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);

            Log.d(TAG, "onQueueChanged: ");
        }

        // This might happen if the MusicService is killed while the Activity is in the
        // foreground and onStart() has been called (but not onStop()).
        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();

            Log.d(TAG, "onSessionDestroyed: MediaControllerCallback");
        }

    }

    private void setFragment(Fragment fragment) {

        if (!(fragment instanceof LibraryFragment)) {
            Fragment albumsFragment = getSupportFragmentManager().findFragmentByTag("albumsFragment");
            Fragment albumFragment  = getSupportFragmentManager().findFragmentByTag("albumFragment");

            if (albumsFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(albumsFragment).commit();
            }

            if (albumFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(albumFragment).commit();
                getSupportFragmentManager().popBackStack();
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
}
