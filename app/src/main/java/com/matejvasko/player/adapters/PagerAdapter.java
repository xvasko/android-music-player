package com.matejvasko.player.adapters;

import com.matejvasko.player.fragments.library.TabFragment1;
import com.matejvasko.player.fragments.library.TabFragment2;
import com.matejvasko.player.fragments.library.TabFragment3;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {

    List<Fragment> fragmentList = new ArrayList<>();

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList.add(new TabFragment1());
        fragmentList.add(new TabFragment2());
        fragmentList.add(new TabFragment3());
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
