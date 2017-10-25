package com.example.briantomasco.profile_fill;

/**
 * Created by briantomasco on 10/10/17.
 * Borrowed from in-class tab layout example.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.briantomasco.profile_fill.view.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TabLayout extends AppCompatActivity {
    //Changes
    private SlidingTabLayout slidingTabLayout;
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragments;
    private TabViewPagerAdapter mViewPagerAdapter;

    // server address for sign in and cat list
    final String CATLIST_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/catlist.pl?";
    protected int length;



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

        final SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        String char_name = new String();
        String pw = new String();

        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
        }
        if (load.contains("Password")) {
            pw = load.getString("Password", "");
        }

        String url = CATLIST_SERVER_ADDRESS + "name=" + char_name + "&password=" + pw;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest catlist = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            length = response.length();
                            final SharedPreferences.Editor editor = load.edit();
                            editor.putInt("Cat List Length", length);
                            editor.commit();

                            //TODO: will probably want to save the JSONArray to do something with it
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );

        queue.add(catlist);
    }

    // when sign out is clicked, clear local data and return to sign in activity
    protected void onSignOutClick(View v){
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        load.edit().clear().commit();
        Intent signOut = new Intent("SIGN");
        startActivity(signOut);
        Toast.makeText(getApplicationContext(), "You have signed out", Toast.LENGTH_SHORT).show();
    }

    // on clicking switches, save current state to local data
    protected void onSoundClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevSound = load.getBoolean("Sound", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Sound", !prevSound).commit();

        // tell user new setting
        if (!prevSound) Toast.makeText(getApplicationContext(), "Sound turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Sound turned off", Toast.LENGTH_SHORT).show();
    }

    protected void onVibrateClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevVibrate = load.getBoolean("Vibrate", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Vibrate", !prevVibrate).commit();

        // tell user new setting
        if (!prevVibrate) Toast.makeText(getApplicationContext(), "Vibrate turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Vibrate turned off", Toast.LENGTH_SHORT).show();
    }

    protected void onPublicClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevPublic = load.getBoolean("Public", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Public", !prevPublic).commit();

        // tell user new setting
        if (!prevPublic) Toast.makeText(getApplicationContext(), "Public score turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Public score turned off", Toast.LENGTH_SHORT).show();
    }

    protected void onPlayClick(View v) {
        Intent play = new Intent("GAME");
        startActivity(play);
    }

}
