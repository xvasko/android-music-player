package com.matejvasko.player.fragments.library;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.MainActivity;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.SongListAdapter;
import com.matejvasko.player.models.Song;
import com.matejvasko.player.viewmodels.MainActivityViewModel;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsFragment extends Fragment implements SongsFragmentI {

    private static final String TAG = "SongsFragment";

    private RecyclerView recyclerView;
    private SongListAdapter songListAdapter;
    private MainActivityViewModel mainActivityViewModel;


    public SongsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity)context).setListener1(this);
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
        View view =  inflater.inflate(R.layout.fragment_tab_1, container, false);

        mainActivityViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        songListAdapter = new SongListAdapter(getActivity());

        recyclerView = view.findViewById(R.id.songs_recycler_view);
        recyclerView.setAdapter(songListAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (((MainActivity) getActivity()).isStoragePermissionGranted()) {
            loadSongs();
        }

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

    // as a result of MainActivity.MediaControllerCallback.onConnected()
    public void loadSongs() {
        mainActivityViewModel.getSongs().observe(this, new Observer<PagedList<Song>>() {
            @Override
            public void onChanged(PagedList<Song> songs) {
                songListAdapter.submitList(songs);
            }
        });
    }

}
