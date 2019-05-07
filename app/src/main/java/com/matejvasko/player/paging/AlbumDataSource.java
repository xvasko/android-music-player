package com.matejvasko.player.paging;

import android.util.Log;

import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.models.Album;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class AlbumDataSource extends PositionalDataSource<Album> {

    private static final String TAG = "SongDataSource";

    private static int pageSize;
    private MediaProvider mediaProvider = MediaProvider.getInstance();
    private Set<Integer> loadedPages = new HashSet<>();

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Album> callback) {
        Log.d(TAG, "loadInitial");

        pageSize = params.pageSize;
        loadedPages.add(0);

        int size = mediaProvider.getAlbumCursorSize();

        callback.onResult(getAlbumPage(0, pageSize), params.requestedStartPosition, size);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<Album> callback) {
        Log.d(TAG, "loadRange");

        final int pageIndex = getPageIndex(params);
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<Album>());
            return;
        }

        loadedPages.add(pageIndex);
        callback.onResult(getAlbumPage(pageIndex, pageSize));
    }

    private List<Album> getAlbumPage(int page, int pageSize) {

        int cursorSize = mediaProvider.getSongCursorSize();

        int startPosition = page * pageSize;
        if (startPosition + pageSize <= cursorSize) {
            return mediaProvider.getAlbumAtRange(startPosition, startPosition + pageSize);
        } else {
            return mediaProvider.getAlbumAtRange(startPosition, cursorSize);
        }
    }

    private int getPageIndex(LoadRangeParams params) {
        return params.startPosition / pageSize;
    }

}