package com.example.briantomasco.profile_fill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by zacharyjohnson on 10/29/17.
 */

public class EditProfileActivity extends AppCompatActivity {

    TextView oldFull;
    TextView cn;
    TextView oldPass;

    EditText fn;
    EditText pw;
    EditText con_pw;
    ImageView profPic;

    boolean fnEmpty = true;
    boolean pwEmpty = true;

    String pass;
    String char_name;
    String path;
    Bitmap bitmap;

    final String PASS_ADDRESS = "http://cs65.cs.dartmouth.edu/changepass.pl?";
    final String PROFILE_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/profile.pl";
    JSONObject profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        oldFull = findViewById(R.id.old_full_name);
        cn = findViewById(R.id.edit_char_name);
        oldPass = findViewById(R.id.old_pass);

        fn = findViewById(R.id.edit_full_name);
        pw = findViewById(R.id.edit_new_pass);
        con_pw = findViewById(R.id.edit_confirm_pass);
        profPic = findViewById(R.id.edit_prof_pic);

        //when creating the view, load any information stored in the shared preferences
        SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        if (load.contains("Full Name")) {
            oldFull.setText("Full name: " + load.getString("Full Name", ""));
        }
        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
            cn.setText("Character name: " + char_name);
        }
        if (load.contains("Password")) {
            pass = load.getString("Password", "");
            oldPass.setText("Password: " + pass);
        }
        if (load.contains("filePath")) {
            path = load.getString("filePath", "");

            FileInputStream fis = null;

            try {
                File f = new File(path, "profile.jpg");
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
                profPic.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        pw.addTextChangedListener(new TextWatcher() {

            //check if the password entered is empty
            @Override
            public void afterTextChanged(Editable editable) {
                if (!pw.getText().equals("")) pwEmpty = false;
                else pwEmpty = true;
            }
            //with this listener, have to include beforeTextChanged and onTextChanged, even if not used
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

        });

        fn.addTextChangedListener(new TextWatcher() {

            //check if full name is empty
            @Override
            public void afterTextChanged(Editable s) {
            if (fn.getText().equals("")) fnEmpty = false;
            else fnEmpty = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }


        });
    }

    protected void onEditSaveClick(View v){

        //saving new password locally as well as sending an update password request to the server
        SharedPreferences save = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final SharedPreferences.Editor editor = save.edit();
        if (!pwEmpty) {
            if (!pw.getText().toString().equals(con_pw.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
            else {
                String url = PASS_ADDRESS + "name=" + char_name + "&password=" + pass + "&newpass=" + pw.getText().toString();
                JsonObjectRequest changePass = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getString("status").equals("OK")) {
                                        pass = pw.getText().toString();
                                        Toast.makeText(getApplicationContext(), "New password is " + pass, Toast.LENGTH_SHORT).show();
                                        editor.putString("Password", pass);
                                        editor.commit();
                                    }
                                }
                                catch (JSONException e) {
                                    Toast.makeText(getApplicationContext(), "Error changing password", Toast.LENGTH_SHORT).show();
                                    Log.d("CHANGE PASS ERROR", e.getMessage());
                                }
                            }
                        },
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
                );
                changePass.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(changePass);
            }
        }

        //repeat process with full name and a getProfile request instead
        if (!fnEmpty){
            String url = PROFILE_SERVER_ADDRESS + "?name=" + char_name + "&password=" + pass;
            JsonObjectRequest getProfile = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if (response.getString("status").equals("OK")){
                                    profile = response;
                                    profile.put("full_name", fn.getText().toString());
                                }
                            }
                            catch (JSONException e){
                                Log.d("ERROR GETTING PROF", e.getMessage());
                            }
                        }
                    },
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
            );
            getProfile.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(getProfile);
            url = PROFILE_SERVER_ADDRESS;
            JsonObjectRequest saveProfile = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    profile,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try{
                                if (response.getString("status").equals("OK")){
                                    editor.putString("Full Name", fn.getText().toString());
                                    Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_LONG).show();
                                    oldFull.setText(fn.getText().toString());
                                    editor.commit();
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
            );
            saveProfile.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(saveProfile);
        }
        //check to make sure that password and password re-entry match
        if (!pw.getText().toString().equals(con_pw.getText().toString())) {
            Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
        else {
            Intent returnIntent = new Intent("TAB");
            startActivity(returnIntent);
        }
    }



}
