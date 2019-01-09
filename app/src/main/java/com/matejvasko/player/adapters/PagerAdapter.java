package com.matejvasko.player.adapters;

import com.matejvasko.player.fragments.library.AlbumsFragment;
import com.matejvasko.player.fragments.library.SongsFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    List<Fragment> fragmentList = new ArrayList<>();

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList.add(new SongsFragment());
        fragmentList.add(new AlbumsFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
