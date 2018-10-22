package com.matejvasko.player.fragments.library;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment2 extends Fragment {


    public TabFragment2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_2, container, false);
    }

}
