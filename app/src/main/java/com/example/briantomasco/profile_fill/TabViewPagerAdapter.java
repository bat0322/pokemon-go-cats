package com.example.briantomasco.profile_fill;

/**
 * Created by briantomasco on 10/10/17.
 * Borrowed from in-class tab layout example.
 */

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
                return "Play";

            case 2:
                return "History";
            default:
                break;
        }
        return null;

    }
}
