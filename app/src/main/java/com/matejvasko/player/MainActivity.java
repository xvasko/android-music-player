package com.matejvasko.player;

import android.content.ComponentName;
import android.media.AudioManager;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.matejvasko.player.fragments.FriendsFragment;
import com.matejvasko.player.fragments.library.LibraryFragment;
import com.matejvasko.player.fragments.MapFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.bottom_nav) BottomNavigationView bottomNav;
    @BindView(R.id.fragment_container)
    RelativeLayout fragmentContainer;

    private LibraryFragment libraryFragment;
    private FriendsFragment friendsFragment;
    private MapFragment mapFragment;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private MediaControllerCallback mediaControllerCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowserCompat(
                            this,
                            new ComponentName(this, MediaPlaybackService.class),
                            new MediaBrowserConnectionCallback(),
                            null);
            mediaBrowser.connect();
        }
        Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaController != null) {
            mediaController.unregisterCallback(mediaControllerCallback);
        }
        mediaBrowser.disconnect();

        Log.d(TAG, "onStop:");
    }

    // Receives callbacks from the MediaBrowser when it has successfully connected to the
    // MediaBrowserService (MusicPlaybackService).
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        // Happens as a result of onStart().
        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                mediaController.registerCallback(mediaControllerCallback);

                // enables handling of media buttons
                MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                ImageView playPause = findViewById(R.id.play_pause_button);
                playPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pbState = mediaController.getPlaybackState().getState();
                        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                            mediaController.getTransportControls().pause();
                        } else {
                            mediaController.getTransportControls().play();
                        }
                    }
                });

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
            Log.d(TAG, "onMetadataChanged: MediaControllerCallback");
        }

        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
            Log.d(TAG, "onPlaybackStateChanged: MediaControllerCallback + " + state);
        }

        // This might happen if the MusicService is killed while the Activity is in the
        // foreground and onStart() has been called (but not onStop()).
        @Override
        public void onSessionDestroyed() {
            Log.d(TAG, "onSessionDestroyed: MediaControllerCallback");
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commit();
    }

}
