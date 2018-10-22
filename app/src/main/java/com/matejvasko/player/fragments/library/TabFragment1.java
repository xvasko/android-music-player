package com.matejvasko.player.fragments.library;


import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.matejvasko.player.R;
import com.matejvasko.player.adapters.SongListAdapter;

import java.util.LinkedList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment1 extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "TabFragment1";
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;

    private final LinkedList<String> songList = new LinkedList<>();
    private RecyclerView recyclerView;
    private SongListAdapter adapter;

    public TabFragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_tab_1, container, false);

        checkReadExternalStoragePermission();

        for (int i = 0; i < 20; i++) {
            songList.addLast("Song " + i);
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new SongListAdapter(getActivity(), songList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void checkReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(getActivity(), "App needs to view thumbnails", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
            getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Now have access to views thumbnails", Toast.LENGTH_SHORT).show();
                    getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "onCreateLoader:");
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
//                MediaStore.Audio.Albums._ID,
//                MediaStore.Audio.Albums.ALBUM,
//                MediaStore.Audio.Albums.ALBUM_ART
        };
        //String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        return new CursorLoader(
                getActivity(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

//        String[] projection = {
//                MediaStore.Files.FileColumns._ID,
//                MediaStore.Files.FileColumns.DATE_ADDED,
//                MediaStore.Files.FileColumns.MEDIA_TYPE
//        };
//        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//        return new CursorLoader(
//                getActivity(),
//                MediaStore.Files.getContentUri("external"),
//                projection,
//                selection,
//                null,
//                MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
//        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinish:");
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset:");
        adapter.changeCursor(null);
    }
}
