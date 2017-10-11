package com.example.briantomasco.profile_fill;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by briantomasco on 10/10/17.
 * History Fragment for tab layout
 * Functionality will be added in a later lab
 */

public class History extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.history, container, false);
    }

}
