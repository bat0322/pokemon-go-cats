package com.example.briantomasco.profile_fill;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by zacharyjohnson on 10/29/17.
 */

public class SuccessActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
    }

    //when you click the "More Cats!" button, go back to the game
    protected void onCatsClick(View v) {
        Intent backIntent = new Intent("GAME");
        startActivity(backIntent);
    }

    //when you click the "Main Menu" button, go back to the settings page
    protected void onMenuClick(View v) {
        Intent menuIntent = new Intent("TAB");
        startActivity(menuIntent);
    }
}
