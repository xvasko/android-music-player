package com.matejvasko.player;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
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
import com.matejvasko.player.fragments.library.LibraryFragment;
import com.matejvasko.player.fragments.library.TabFragment1I;
import com.matejvasko.player.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private BottomSheetBehavior bottomSheetBehavior;

    BottomNavigationView bottomNav;
    RelativeLayout fragmentContainer;

    View bottomSheetOnClickView;
    RelativeLayout bottomSheet;
    ImageView albumArtImageView;
    TextView songTitleTextView;
    ImageView playPauseImageView;
    Button playPauseButton;
    MediaSeekBar mediaSeekBar;

    private LibraryFragment libraryFragment;
    private FriendsFragment friendsFragment;
    private MapFragment mapFragment;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaControllerCallback mediaControllerCallback;

    public MediaBrowserCompat getMediaBrowser() {
        return mediaBrowser;
    }

    private boolean isPlaying;

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
        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(clickListener);
        mediaSeekBar = findViewById(R.id.media_seek_bar);
        mediaSeekBar.setPadding(0, 16, 0, 16);
        mediaSeekBar.setTextViews(
                (TextView) findViewById(R.id.duration_current),
                (TextView)findViewById(R.id.duration_total)
        );


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
            mediaBrowser = new MediaBrowserCompat(
                            this,
                            new ComponentName(this, MediaPlaybackService.class),
                            new MediaBrowserConnectionCallback(),
                            null);
        }
        mediaBrowser.connect();
        Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaSeekBar.disconnectMediaController();
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
        }
        mediaBrowser.disconnect();

        Log.d(TAG, "onStop:");
    }


    private TabFragment1I listener;

    public void setListener(TabFragment1I listener) {
        this.listener = listener;
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicPlaybackService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            try {
                listener.setMediaBrowser(mediaBrowser);
                listener.loadSongs();
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
            albumArtImageView.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
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
            playPauseImageView.setPressed(isPlaying);

            Log.d(TAG, "onPlaybackStateChanged: MediaControllerCallback + " + state);
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }

}
