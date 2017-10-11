package com.example.briantomasco.profile_fill;

/**
 * Created by briantomasco on 10/10/17.
 * Borrowed from in-class tab layout example.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.briantomasco.profile_fill.view.SlidingTabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class TabLayout extends AppCompatActivity {
    //Changes
    private SlidingTabLayout slidingTabLayout;
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragments;
    private TabViewPagerAdapter mViewPagerAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();
        fragments.add(new Preferences());
        fragments.add(new Ranking());
        fragments.add(new Play());
        fragments.add(new History());

        mViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(),fragments);

        mViewPager.setAdapter(mViewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);

    }
    protected void onSignOutClick(View v){
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        load.edit().clear().commit();
        Intent signOut = new Intent("SIGN");
        startActivity(signOut);
        Toast.makeText(getApplicationContext(), "You have signed out", Toast.LENGTH_SHORT).show();
    }

}
