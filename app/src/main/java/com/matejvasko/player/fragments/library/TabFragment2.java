package com.matejvasko.player.fragments.library;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matejvasko.player.R;

import java.lang.reflect.Field;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabFragment2 extends Fragment {

    private static final String TAG = "TabFragment2";

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

    FragmentManager fm;
    Fragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view =  inflater.inflate(R.layout.fragment_tab_2, container, false);

          fm = getActivity().getSupportFragmentManager();
//        fragment = fm.findFragmentByTag("albumsFragment");
//        if (fragment == null) {
            fm.beginTransaction()
                    .add(R.id.album_fragment_container, new AlbumsFragment(), "albumsFragment")
                    .commit();
//            System.out.println("fragment isnull");
//        }


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
//        Fragment fragment = fm.findFragmentByTag("albumsFragment");
//        if (fragment != null) {
//            fm.beginTransaction().remove(fragment).commit();
//        }
        Log.d(TAG, "onDetach");
    }

}
