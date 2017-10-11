package com.example.briantomasco.profile_fill;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        // load the shared preferences data and put them in the appropriate fields
        SharedPreferences load = getActivity().getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
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
    }



}
