package com.example.briantomasco.profile_fill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by briantomasco on 10/4/17.
 */

public class SignInActivity extends AppCompatActivity {

    // EditTexts for entering character name and password
    EditText cn = null;
    EditText pw = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        cn = findViewById(R.id.sign_in_character);
        pw = findViewById(R.id.sign_in_password);

        // check if a user is already logged in
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        if (load.contains("Logged In")) {
            // if so, go directly to tab layout
            if (load.getBoolean("Logged In", false)) {
                Intent goToTab = new Intent("TAB");
                startActivity(goToTab);
            }
        }
    }

    // clear EditTexts if clear is clicked
    protected void signInOnClearClick(View v){
        cn.setText("");
        pw.setText("");

    }

    // sign in functionality to be added later, for now just go to tab layout on click
    protected void onSignInCLick(View v) {
        Intent signInIntent = new Intent("TAB");
        startActivity(signInIntent);
    }

    //  if create an account is clicked, go to the create account activity
    protected void onCreateClick(View v){
        Intent createIntent = new Intent("CREATE");
        startActivity(createIntent);
    }
}
