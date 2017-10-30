package com.example.briantomasco.profile_fill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by briantomasco on 10/10/17.
 * Play Fragment for tab layout
 * Functionality will be added in a later lab
 */

public class Play extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.play, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // once the view has been created, update the text views and profile picture to match current user
        TextView cn = view.findViewById(R.id.play_char_name);
        TextView status = view.findViewById(R.id.status);

        // load the shared preferences data and put them in the appropriate fields
        SharedPreferences load = getActivity().getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        if (load.contains("User Name")) {
            cn.setText("Welcome " + load.getString("User Name", ""));
        }

        //find the length of the cat list and display it on the splash screen
        if (load.contains("Cat List Length")) {
            int length = load.getInt("Cat List Length", 0);
            status.setText("You have " + length + " cats!");
            Log.d("LOAD", "Length exists");
        }
        else Log.d("LOAD", "No length");



    }
}
