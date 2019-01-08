package com.matejvasko.player.paging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.widget.Toast;

import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.paging.PositionalDataSource;

public class MediaItemDataSource extends PositionalDataSource<MediaItemData> {

    private static final String TAG = "MediaItemDataSource";
    private static int pageSize;

    public static final int SONG_DATA_SOURCE = 0;
    public static final int ALBUM_DATA_SOURCE = 1;

    private MediaProvider mediaProvider = MediaProvider.getInstance();
//
//    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 0;
//
//    private void checkReadExternalStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                listener1.loadSongs();
//                listener2.loadAlbums();
//            } else {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this, "App needs to load music", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
//            }
//        } else {
//            listener1.loadSongs();
//            listener2.loadAlbums();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case READ_EXTERNAL_STORAGE_PERMISSION:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Now have access to views thumbnails", Toast.LENGTH_SHORT).show();
//                    listener1.loadSongs();
//                    listener2.loadAlbums();
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//
//    }

    private final int flag;
    private Set<Integer> loadedPages = new HashSet<>();

    MediaItemDataSource(int flag) {
        this.flag = flag;
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params, @NonNull final LoadInitialCallback<MediaItemData> callback) {
        Log.d(TAG, "loadInitial");

        pageSize = params.pageSize;
        loadedPages.add(0);

        int size;
        if (this.flag == SONG_DATA_SOURCE) {
            size = mediaProvider.getSongCursorSize();
        } else {
            size = mediaProvider.getAlbumCursorSize();
        }

        callback.onResult(getMediaItemPage(0, pageSize), params.requestedStartPosition, size);
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull final LoadRangeCallback<MediaItemData> callback) {
        Log.d(TAG, "loadRange " + flag);

        final int pageIndex = getPageIndex(params);
        if (loadedPages.contains(pageIndex)) {
            callback.onResult(new ArrayList<MediaItemData>());
            return;
        }

        loadedPages.add(pageIndex);
        callback.onResult(getMediaItemPage(pageIndex, pageSize));
    }

    private List<MediaItemData> getMediaItemPage(int page, int pageSize) {

        int cursorSize;

        if (flag == MediaItemDataSource.SONG_DATA_SOURCE) {
            cursorSize = mediaProvider.getSongCursorSize();
        } else {
            cursorSize = mediaProvider.getAlbumCursorSize();
        }

        int startPosition = page * pageSize;
        if (startPosition + pageSize <= cursorSize) {
            return mediaProvider.getMediaItemDataAtRange(startPosition, startPosition + pageSize, flag);
        }else {
            return mediaProvider.getMediaItemDataAtRange(startPosition, cursorSize, flag);
        }
    }

    private int getPageIndex(LoadRangeParams params) {
        return params.startPosition / pageSize;
    }

}
