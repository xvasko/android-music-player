package com.matejvasko.player.paging;

import android.util.Log;

import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.models.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.paging.PositionalDataSource;

public class SongDataSource extends PositionalDataSource<Song> {

    private static final String TAG = "SongDataSource";
    private static int pageSize;
    private MediaProvider mediaProvider = MediaProvider.getInstance();
    private Set<Integer> loadedPages = new HashSet<>();

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<Song> callback) {
        Log.d(TAG, "loadInitial");

        pageSize = params.pageSize;
        loadedPages.add(0);

        int size = mediaProvider.getSongCursorSize();

        callback.onResult(getSongPage(0, pageSize), params.requestedStartPosition, size);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<Song> callback) {
        Log.d(TAG, "loadRange");

        final int pageIndex = getPageIndex(params);
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<Song>());
            return;
        }

        loadedPages.add(pageIndex);
        callback.onResult(getSongPage(pageIndex, pageSize));
    }

    private List<Song> getSongPage(int page, int pageSize) {

        int cursorSize = mediaProvider.getSongCursorSize();

        int startPosition = page * pageSize;
        if (startPosition + pageSize <= cursorSize) {
            return mediaProvider.getSongAtRange(startPosition, startPosition + pageSize);
        }else {
            return mediaProvider.getSongAtRange(startPosition, cursorSize);
        }
    }

    private int getPageIndex(LoadRangeParams params) {
        return params.startPosition / pageSize;
    }

}
