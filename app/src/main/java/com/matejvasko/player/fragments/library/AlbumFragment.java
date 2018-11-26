package com.matejvasko.player.fragments.library;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matejvasko.player.MediaItemData;
import com.matejvasko.player.MediaProvider;
import com.matejvasko.player.R;
import com.matejvasko.player.adapters.AlbumSongsListAdapter;
import com.matejvasko.player.adapters.MediaItemDataListAdapter;
import com.matejvasko.player.paging.MediaItemDataSource;
import com.matejvasko.player.utils.Utils;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {

    MediaItemData mediaItemData;

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        Bundle bundle = getArguments();

        MediaProvider mediaProvider = MediaProvider.getInstance();
        List<MediaItemData> songs = mediaProvider.getAlbumSongs(bundle.getString("album_id"));
        AlbumSongsListAdapter albumSongsListAdapter = new AlbumSongsListAdapter(songs);
        RecyclerView recyclerView = view.findViewById(R.id.albums_songs_list);
        recyclerView.setAdapter(albumSongsListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

}
