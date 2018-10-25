package com.matejvasko.player.fragments.library;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.MainActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.adapters.SongListAdapter;
import com.matejvasko.player.viewmodels.SongsViewModel;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment1 extends Fragment implements MyInterface {
    private static final String TAG = "TabFragment1";
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 0;
    private final static int MEDIASTORE_LOADER_ID = 0;

    private RecyclerView recyclerView;
    private SongsViewModel songsViewModel;

    public TabFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity)context).setListener(this);
        Log.d(TAG, "onAttach: TabFragment1");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_tab_1, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        songsViewModel = ViewModelProviders.of(this).get(SongsViewModel.class);

        return view;
    }

    public void loadSongs(MediaBrowserCompat mediaBrowser) {
        System.out.println("load songs");
        final SongListAdapter songListAdapter = new SongListAdapter(getActivity());
        recyclerView.setAdapter(songListAdapter);
        songsViewModel.getSongs(mediaBrowser).observe(this, new Observer<PagedList<Song>>() {
            @Override
            public void onChanged(PagedList<Song> songs) {
                System.out.println("su to oni?" + songs);
                songListAdapter.submitList(songs);
            }
        });
    }

//    private void checkReadExternalStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
//            } else {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    Toast.makeText(getActivity(), "App needs to view thumbnails", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION);
//            }
//        } else {
//            getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case READ_EXTERNAL_STORAGE_PERMISSION:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(getActivity(), "Now have access to views thumbnails", Toast.LENGTH_SHORT).show();
//                    getLoaderManager().initLoader(MEDIASTORE_LOADER_ID, null, this);
//                }
//                break;
//            default:
//                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
//
//    @NonNull
//    @Override
//    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
//        Log.d(TAG, "onCreateLoader:");
//        String[] projection = {
//                MediaStore.Audio.Media.TITLE,
//                MediaStore.Audio.Media.ARTIST,
//                MediaStore.Audio.Media.ALBUM_ID,
//                MediaStore.Audio.Media.DATA,
//                MediaStore.Audio.Media.DURATION
//        };
//
//        return new CursorLoader(
//                getActivity(),
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                null,
//                null,
//                null
//        );
//
//    }
//
//    @Override
//    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
//        Log.d(TAG, "onLoadFinish:");
//        adapter.changeCursor(data);
//    }
//
//    @Override
//    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
//        Log.d(TAG, "onLoaderReset:");
//        adapter.changeCursor(null);
//    }
}
