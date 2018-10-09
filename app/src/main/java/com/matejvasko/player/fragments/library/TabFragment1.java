package com.matejvasko.player.fragments.library;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.R;
import com.matejvasko.player.adapters.SongListAdapter;

import java.util.LinkedList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment1 extends Fragment {

    private final LinkedList<String> songList = new LinkedList<>();
    private RecyclerView recyclerView;
    private SongListAdapter adapter;

    public TabFragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_tab_1, container, false);

        for (int i = 0; i < 20; i++) {
            songList.addLast("Song " + i);
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new SongListAdapter(getActivity(), songList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

}
