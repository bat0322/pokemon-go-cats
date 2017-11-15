package com.example.briantomasco.profile_fill;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // create a linear layout to show the cat list
        LinearLayout lin = getView().findViewById(R.id.lin);

        // get the list of cats
        JSONArray cats = TabLayout.getCats();

        // assuming list is there, get information and make a linear layout with each one
        if (cats != null) {
            for (int i = 0; i < cats.length(); i++) {
                try {
                    JSONObject obj = (JSONObject) cats.get(i);
                    String picUrl = obj.getString("picUrl");
                    String name = obj.getString("name");
                    Double lat = obj.getDouble("lat");
                    Double lng = obj.getDouble("lng");
                    Boolean petted = obj.getBoolean("petted");

                    // use layout helper to make panel for each cat
                    HistoryLayoutHelper helper = new HistoryLayoutHelper(getContext(), picUrl, name, lat, lng, petted);
                    LinearLayout layout = helper.createLayout();

                    // add to master linear layout
                    lin.addView(layout);

                } catch (Exception e) {
                    Toast.makeText(getContext(),
                            e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}
