package com.example.briantomasco.profile_fill;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.briantomasco.profile_fill.CreateAcctActivity.SHARED_PREF;

/**
 * Created by briantomasco on 10/4/17.
 *
 */

public class SignInActivity extends AppCompatActivity {

    // EditTexts for entering character name and password
    EditText cn = null;
    EditText pw = null;

    // server address for sign in and cat list
    final String PROFILE_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/profile.pl";



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        cn = findViewById(R.id.sign_in_character);
        pw = findViewById(R.id.sign_in_password);

        // check if a user is already logged in
        SharedPreferences load = getSharedPreferences(SHARED_PREF, 0);
        if (load.contains("Logged In")) {
            // if so, go directly to tab layout
            if (load.getBoolean("Logged In", false)) {
                Intent goToTab = new Intent("TAB");
                startActivity(goToTab);
                finish();
            }
        }
    }

    // sign in to account with given credentials
    protected void onSignInClick(View v){

        Toast.makeText(getApplicationContext(),
                "Validating...",
                Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = PROFILE_SERVER_ADDRESS + "?name=" + cn.getText() + "&password=" + pw.getText();

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
                            } else {

                                // get info and settings tied to account
                                String char_name = response.getString("name");
                                String pass = response.getString("password");
                                String full_name = response.getString("full_name");
                                boolean sound = response.getBoolean("sound");
                                boolean vibrate = response.getBoolean("vibrate");
                                boolean pub = response.getBoolean("public");
                                int distance = response.getInt("distance");
                                int notiDistance = response.getInt("noti_distance");

                                SharedPreferences save = getSharedPreferences(SHARED_PREF, 0);
                                final SharedPreferences.Editor editor = save.edit();


                                editor.putString("Full Name", full_name);
                                editor.putString("User Name", char_name);
                                editor.putString("Password", pass);

                                // booleans to handle editing profile info screen
                                editor.putBoolean("Match", true);
                                editor.putBoolean("Available", true);


                                // save boolean saying a profile is logged in
                                editor.putBoolean("Logged In", true);

                                // save default booleans for Settings
                                editor.putBoolean("Sound", sound);
                                editor.putBoolean("Vibrate", vibrate);
                                editor.putBoolean("Public", pub);
                                editor.putInt("Distance", distance);
                                editor.putInt("Notification distance", notiDistance);

                                editor.commit();

                                Intent signIn = new Intent("TAB");
                                startActivity(signIn);
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
                        if (error.networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                Toast.makeText(getApplicationContext(), "Timeout error!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (error.getClass().equals(ServerError.class)) {
                            Toast.makeText(getApplicationContext(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                        }

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
        jsObjReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //add request to Volley queue for execution
        queue.add(jsObjReq);
    }

    // clear EditTexts if clear is clicked
    protected void signInOnClearClick(View v){
        cn.setText("");
        pw.setText("");

    }

    //  if create an account is clicked, go to the create account activity
    protected void onCreateClick(View v){
        Intent createIntent = new Intent("CREATE");
        startActivity(createIntent);
    }
}
