package com.matejvasko.player;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;

import com.matejvasko.player.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class SongsDataSource extends PositionalDataSource<MediaItemData> {

    private static final String TAG = "SongsDataSource";
    private static int pageSize;

    private final MediaBrowserCompat mediaBrowser;
    private String rootId;
    private Set<Integer> loadedPages = new HashSet<>();

    SongsDataSource(MediaBrowserCompat mediaBrowser) {
        this.mediaBrowser = mediaBrowser;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<MediaItemData> callback) {
        Log.d(TAG, "loadInitial");
        String parentId = getParentId(params.requestedStartPosition);
        Bundle extra = getInitialPageBundle(params);
        pageSize = params.pageSize;
        mediaBrowser.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                Log.d(TAG, "loadInitial: onChildrenLoaded" + options.toString());
                super.onChildrenLoaded(parentId, children);
                loadedPages.add(0);
                List<MediaItemData> songs = Utils.mapToMediaItemData(children);
                callback.onResult(songs, params.requestedStartPosition, options.getInt("songs_count"));
            }
        });
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<MediaItemData> callback) {
        Log.d(TAG, "loadRange");
        final int pageIndex = getPageIndex(params);
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<MediaItemData>());
            return;
        }

        String parentId = getParentId(params.startPosition);
        Bundle extra = getRangeBundle(params);
        mediaBrowser.subscribe(parentId, extra, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                Log.d(TAG, "loadRange: onChildrenLoaded");
                super.onChildrenLoaded(parentId, children);
                loadedPages.add(pageIndex);
                List<MediaItemData> songs = Utils.mapToMediaItemData(children);
                callback.onResult(songs);
            }
        });
    }

    @NonNull
    private Bundle getInitialPageBundle(@NonNull LoadInitialParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, 0);
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, params.pageSize);
        return extra;
    }

    private Bundle getRangeBundle(LoadRangeParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params));
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE, pageSize);
        return extra;
    }

    private int getPageIndex(LoadRangeParams params) {
        return params.startPosition / pageSize;
    }

    private String getParentId(int requestedStartPosition) {
        if (rootId == null)
            rootId = mediaBrowser.getRoot();
        return rootId + requestedStartPosition;
    }

}
