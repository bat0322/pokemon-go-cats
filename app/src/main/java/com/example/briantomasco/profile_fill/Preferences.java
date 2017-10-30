package com.example.briantomasco.profile_fill;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by briantomasco on 10/9/17.
 * Preferences Fragment for tab layout
 * Functionality will be added in later lab
 */

public class Preferences extends Fragment {

    Bitmap bitmap;
    private String path;

    private TextView fn;
    private TextView cn;
    private ImageView imageView;
    private Switch sound;
    private Switch vibrate;
    private Switch pub;
    private EditText distance;
    SharedPreferences load;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.preferences, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // once the view has been created, update the text views and profile picture to match current user
        fn = view.findViewById(R.id.pref_full_name);
        cn = view.findViewById(R.id.pref_char_name);
        imageView = view.findViewById(R.id.pref_prof_pic);
        sound = view.findViewById(R.id.sound_switch);
        vibrate = view.findViewById(R.id.vibrate_switch);
        pub = view.findViewById(R.id.public_switch);
        distance = view.findViewById(R.id.distance_pref);

        // load the shared preferences data and put them in the appropriate fields
        load = getActivity().getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        if (load.contains("Full Name")) {
            fn.setText(load.getString("Full Name", ""));
        }
        if (load.contains("User Name")) {
            cn.setText("@" + load.getString("User Name", ""));
        }
        // found info and base code for saving and loading bitmap at stackoverflow
        // URL: https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
        if (load.contains("filePath")) {
            path = load.getString("filePath", "");

            FileInputStream fis = null;

            try {
                File f = new File(path, "profile.jpg");
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //find the saved settings if they are in sharedPreferences
        if (load.contains("Sound")) sound.setChecked(load.getBoolean("Sound", false));
        if (load.contains("Vibrate")) vibrate.setChecked(load.getBoolean("Vibrate", false));
        if (load.contains("Public")) pub.setChecked(load.getBoolean("Public", false));
        if (load.contains("Distance")) distance.setText(Integer.toString(load.getInt("Distance", 250)));


        distance.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

                //record the inputted distance setting and save it to sharedPreferences
                if (!s.toString().equals("")) {
                    SharedPreferences.Editor editor = load.edit();
                    editor.putInt("Distance", Integer.parseInt(s.toString()));
                    editor.commit();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });
    }



}
