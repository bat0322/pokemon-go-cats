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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.briantomasco.profile_fill.view.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.briantomasco.profile_fill.CreateAcctActivity.SHARED_PREF;

public class TabLayout extends AppCompatActivity {
    //Changes
    private SlidingTabLayout slidingTabLayout;
    private ViewPager mViewPager;
    private ArrayList<Fragment> fragments;
    private TabViewPagerAdapter mViewPagerAdapter;

    private String char_name;
    private String pw;

    // server address for reset and cat list
    final String CATLIST_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/catlist.pl?";
    final String RESET_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/resetlist.pl?";
    final String PROFILE_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/profile.pl";
    protected int length;
    JSONObject profile;
    SharedPreferences load;

    private EditText distance;
    static JSONArray catsJSON;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        fragments = new ArrayList<Fragment>();
        fragments.add(new Preferences());
        fragments.add(new Play());
        fragments.add(new History());

        mViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager(),fragments);

        mViewPager.setAdapter(mViewPagerAdapter);

        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mViewPager);

        distance = findViewById(R.id.distance_pref);

        load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);

        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
        }
        if (load.contains("Password")) {
            pw = load.getString("Password", "");
        }
        getCatList();
    }

    //acquire the catList from the server. Finds the length of the list and stores it locally
    protected void getCatList() {
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
                            catsJSON = response;
                            length = response.length();
                            final SharedPreferences.Editor editor = load.edit();
                            editor.putInt("Cat List Length", length);
                            editor.commit();

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

    //return the cat list
    public static JSONArray getCats() {
        return catsJSON;
    }

    //when you click on your information, go to the Edit Profile Activity
    public void onProfInfoClick(View v) {
        Intent editInfo = new Intent("EDIT PROF");
        startActivity(editInfo);
    }

    // when sign out is clicked, clear local data and return to sign in activity
    public void onSignOutClick(View v) {
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        load.edit().clear().commit();
        Intent signOut = new Intent("SIGN");
        startActivity(signOut);
        Toast.makeText(getApplicationContext(), "You have signed out", Toast.LENGTH_SHORT).show();
    }

    // on clicking switches, save current state to local data
    public void onSoundClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevSound = load.getBoolean("Sound", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Sound", !prevSound).commit();

        // tell user new setting
        if (!prevSound) Toast.makeText(getApplicationContext(), "Sound turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Sound turned off", Toast.LENGTH_SHORT).show();
    }

    public void onVibrateClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevVibrate = load.getBoolean("Vibrate", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Vibrate", !prevVibrate).commit();

        // tell user new setting
        if (!prevVibrate) Toast.makeText(getApplicationContext(), "Vibrate turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Vibrate turned off", Toast.LENGTH_SHORT).show();
    }

    public void onPublicClick(View v){

        // get the previous stored setting
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        boolean prevPublic = load.getBoolean("Public", false);

        // save the opposite of the previous setting
        load.edit().putBoolean("Public", !prevPublic).commit();

        // tell user new setting
        if (!prevPublic) Toast.makeText(getApplicationContext(), "Public score turned on", Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Public score turned off", Toast.LENGTH_SHORT).show();
    }

    //Sends a request to the server to update the catlist
    public void onResetClick(View v) {
        String url = RESET_SERVER_ADDRESS + "name=" + char_name + "&password=" + pw;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                Toast.makeText(getApplicationContext(), "List reset", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), response.getString("error"), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error checking with server", Toast.LENGTH_SHORT).show();
                            Log.d("PET JSON ERROR", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        queue.add(jsonObjReq);
        getCatList();
    }

    //when you click the play button, open the game activity
    public void onPlayClick(View v) {
        Intent play = new Intent("GAME");
        startActivity(play);
    }

    //saves the settings locally and sends a request to the server to update. Get request to find the settings and Post to update
    public void onPrefSaveClick(View v) {

        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = PROFILE_SERVER_ADDRESS + "?name=" + char_name + "&password=" + pw;

        JsonObjectRequest jsObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("error")) {
                                Toast.makeText(getApplicationContext(),
                                        response.get("error").toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                profile = response;
                                try {
                                    profile.put("sound", load.getBoolean("Sound", false));
                                    profile.put("vibrate", load.getBoolean("Vibrate", false));
                                    profile.put("public", load.getBoolean("Public", false));
                                    profile.put("distance", load.getInt("Distance", 250));
                                }
                                catch (JSONException e) {
                                    Log.d("JSON EDIT ERROR", e.getMessage());
                                }
                                String url = PROFILE_SERVER_ADDRESS;
                                JsonObjectRequest saveProfile = new JsonObjectRequest(
                                        Request.Method.POST,
                                        url,
                                        profile,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try{
                                                    if (response.getString("status").equals("OK")){
                                                        Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_LONG).show();
                                                    }
                                                    else{
                                                        Toast.makeText(getApplicationContext(), "Error saving changes, try again", Toast.LENGTH_LONG).show();
                                                        Log.d("ERROR SAVING NEW PROF", response.get("data").toString());
                                                    }
                                                }
                                                catch (JSONException e){
                                                    Toast.makeText(getApplicationContext(), "Error saving changes", Toast.LENGTH_LONG).show();
                                                    Log.d("ERROR SAVING NEW PROF", e.getMessage());
                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError e) {
                                                Toast.makeText(getApplicationContext(), "Error saving changes", Toast.LENGTH_LONG).show();
                                                Log.d("ERROR SAVING NEW PROF", e.getMessage());
                                            }
                                        }
                                );
                                queue.add(saveProfile);
                            }
                        } catch (Exception e) {
                            Log.d("JSON AVAIL", e.getMessage());
                        }
                    }
                },
                // tell user if connection failed
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                "Could not get profile: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            // change http header, borrowed from example code
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Accept", "application/json");
                return params;
            }
        };
        //add request to Volley queue for execution
        queue.add(jsObjReq);
    }
}
