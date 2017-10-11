package com.example.briantomasco.profile_fill;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class TabViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    public TabViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;

    }

    @Override
    public Fragment getItem(int position) {


        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "Settings";
            case 1:
                return "Ranking";
            case 2:
                return "History";

            case 3:
                return "Play";
            default:
                break;
        }
        return null;

    }
}
