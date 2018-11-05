package com.matejvasko.player.fragments.library;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.Album;
import com.matejvasko.player.MainActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.Song;
import com.matejvasko.player.adapters.AlbumListAdapter;
import com.matejvasko.player.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment2 extends Fragment {

    private static final String TAG = "TabFragment2";

    private RecyclerView recyclerView;
    AlbumListAdapter albumListAdapter;

    private MediaBrowserCompat mediaBrowser;

    public TabFragment2() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view =  inflater.inflate(R.layout.fragment_tab_2, container, false);
        recyclerView = view.findViewById(R.id.albums_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<Album> albums = new ArrayList<>();

        ArrayList<Song> moderatAlbum = new ArrayList<>();
        moderatAlbum.add(new Song.Builder(1).setTitle("Song 1").build());
        moderatAlbum.add(new Song.Builder(2).setTitle("Song 2").build());
        moderatAlbum.add(new Song.Builder(3).setTitle("Song 3").build());
        moderatAlbum.add(new Song.Builder(4).setTitle("Song 4").build());
        moderatAlbum.add(new Song.Builder(5).setTitle("Song 5").build());
        moderatAlbum.add(new Song.Builder(6).setTitle("Song 6").build());

        Album moderat = new Album(1, "MODERAT", moderatAlbum);
        albums.add(moderat);

        ArrayList<Song> neutralAlbum = new ArrayList<>();
        neutralAlbum.add(new Song.Builder(4).setTitle("Song 1").build());
        neutralAlbum.add(new Song.Builder(5).setTitle("Song 2").build());
        neutralAlbum.add(new Song.Builder(6).setTitle("Song 3").build());

        Album neutral = new Album(2, "NEUTRAL", neutralAlbum);
        albums.add(neutral);

        AlbumListAdapter adapter = new AlbumListAdapter(albums);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

}
