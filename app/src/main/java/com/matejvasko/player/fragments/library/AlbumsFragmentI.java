package com.matejvasko.player.fragments.library;

import android.support.v4.media.MediaBrowserCompat;

public interface AlbumsFragmentI {

    void setMediaBrowser(MediaBrowserCompat mediaBrowser);

    void loadAlbums();
}
