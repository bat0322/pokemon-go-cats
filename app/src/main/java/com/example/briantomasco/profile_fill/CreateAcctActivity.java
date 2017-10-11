package com.example.briantomasco.profile_fill;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateAcctActivity extends AppCompatActivity {

    final int REQUEST_IMAGE_CAPTURE = 1;
    final int CONFIRM = 2;
    final String NAME_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/nametest.pl";
    final String PROFILE_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/profile.pl";

    // UI elements which need to be tracked or changed
    private ImageView imageView;
    private TextView available;
    private Uri profilePicUri;
    private Button accClear;
    private Button confirmButton;
    private EditText fn;
    private EditText cn;
    private EditText pw;

    // keep track of which fields are empty (useful for clear button)
    private boolean pwEmpty = true;
    private boolean fnEmpty = true;
    private boolean cnEmpty = true;

    // booleans for matching passwords and available character name
    private boolean match = false;
    private boolean avail = false;

    Bitmap bitmap;
    private String path;
    public static String SHARED_PREF = "my_shared_pref";
    Handler d1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("CYCLE", "create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // create new handler for server connection
        d1 = new Handler();

        //get access to the individual elements from the xml preferences file
        fn = findViewById(R.id.full_name);
        cn = findViewById(R.id.char_name);
        pw = findViewById(R.id.passwd);
        imageView = findViewById(R.id.profilePic);
        accClear = findViewById(R.id.account_clear);
        confirmButton = findViewById(R.id.confirm_button);
        available = findViewById(R.id.available);

        //when creating the view, load any information stored in the shared preferences
        SharedPreferences load = getSharedPreferences(SHARED_PREF, 0);
        if (load.contains("Full Name")) {
            fn.setText(load.getString("Full Name", ""));
            fnEmpty = false;
        }
        if (load.contains("User Name")) {
            cn.setText(load.getString("User Name", ""));
            cnEmpty = false;
        }
        if (load.contains("Password")) {
            pw.setText(load.getString("Password", ""));
            pwEmpty = false;
        }
        if (load.contains("Match")){

            //check to see if the user confirmed the pw
            match = load.getBoolean("Match", false);

            //if the user confirmed, display a green button indication of that success
            if (match){
                confirmButton.setText("Confirmed");
                confirmButton.setTextColor(Color.GREEN);
            }
        }
        if (load.contains("Avail")){

            // if the username was available, show it as available
            avail = load.getBoolean("Avail", false);
            if (avail){
                available.setText("Available");
                available.setTextColor(Color.GREEN);
            }
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
       //check if the form is empty. If not, change the top button to clear
        if (!fnEmpty || !pwEmpty || !cnEmpty || bitmap != null) {
            accClear.setText("Clear");
            accClear.setClickable(true);
         }

        //onFocusChangeListeners track when a user has moved on from any given field
        pw.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean gotFocus) {
                //After the user enters a pw, send an intent to bring up the confirmation dialog
                if (!gotFocus && !match && !pwEmpty) {
                    Intent pswdIntent = new Intent("CONFIRM");
                    pswdIntent.putExtra("passwd1", pw.getText().toString());
                    startActivityForResult(pswdIntent, CONFIRM);
                }
            }
        });
        //Check whether text has been entered
        pw.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                accClear.setText("Clear"); // if so, change the top button to "Clear"
                accClear.setClickable(true);
                match = false;
                confirmButton.setText("Not Confirmed"); //button telling the user if pw has been confirmed
                confirmButton.setTextColor(Color.RED);
                if (pw.getText().toString().equals("")) pwEmpty = true;
                else pwEmpty = false;

                //if every field is empty, top button should read: "I already have an account."
                if (pwEmpty && fnEmpty && cnEmpty && bitmap == null) {
                    accClear.setText("I already have an account");
                }

            }
            //with this listener, have to include beforeTextChanged and onTextChanged, even if not used
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

        });


        // full name entry box
        final EditText fn = findViewById(R.id.full_name);
        //repeat the same process as above with the full name field
        fn.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                accClear.setText("Clear");
                accClear.setClickable(true);
                if (fn.getText().toString().equals("")) fnEmpty = true;
                else fnEmpty = false;

                if (pwEmpty && fnEmpty && cnEmpty && bitmap == null) {
                    accClear.setText("I already have an account");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });


        // Character name entry box
        final EditText cn = findViewById(R.id.char_name);
        //repeat above process with character name field
        cn.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                accClear.setText("Clear");
                accClear.setClickable(true);
                if (cn.getText().toString().equals("")) cnEmpty = true;
                else cnEmpty = false;

                if (pwEmpty && fnEmpty && cnEmpty && profilePicUri == null) {
                    accClear.setText("I already have an account");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });

        // after a character name is entered, check with server for availability
        cn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean gotFocus) {

                if (!gotFocus) {
                    // set up for Volley
                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = NAME_SERVER_ADDRESS + "?name=" + cn.getText();

                    // send request for name availabilty
                    JsonObjectRequest jsObjReq = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    // change text according to availability
                                    avail = Boolean.parseBoolean(response.getString("avail"));
                                    Log.d("JSON", "avail");
                                    if (!avail) {
                                        available.setTextColor(Color.RED);
                                        available.setText("Not available");
                                    } else {
                                        available.setTextColor(Color.GREEN);
                                        available.setText("Available");
                                    }
                                } catch (Exception e) {
                                    Log.d("JSON AVAIL", e.getMessage());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Network connection error: could not check availability", Toast.LENGTH_SHORT).show();
                            }
                        }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            // otherwise causes status 500 server error
                            params.put("Accept", "application/json");
                            return params;
                        }
                    };
                    queue.add(jsObjReq);
                }
            }
        });


    }
    //when photo is clicked...
    protected void onPhotoClick(View v){
        //start up the camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values  = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        //store the image via a URI
        profilePicUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, profilePicUri);
        cameraIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); //grant permission!

        //make final image request
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
        }


    }
    //when returning from a different activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //when a photo has been taken, begin the crop
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            beginCrop(profilePicUri);
        }
        else if (requestCode==Crop.REQUEST_CROP){
            handleCrop(resultCode, data);
        }
        //when a password was entered and needs to be confirmed
        else if (requestCode == CONFIRM){
            match = data.getBooleanExtra("match", false); //check if there's a pw match
            if (match){
                confirmButton.setText("Confirmed");
                confirmButton.setTextColor(Color.GREEN);
            }
        }
    }
    //used format from third party library crop tool
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        //set the uri from the crop as the profile picture
        if (resultCode == RESULT_OK) {
            try {
                // get uri from crop activity, use it to create a bitmap and set imageView as bitmap
                profilePicUri = Crop.getOutput(result);
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), profilePicUri);
                imageView.setImageBitmap(bitmap);

                accClear.setText("Clear");
                accClear.setClickable(true);
            }
            catch (IOException e){
                e.printStackTrace();
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //when the clear button is clicked...
    protected void onAccClearClick(View v){
        //set all fields to empty
        if (accClear.getText().equals("Clear")) {
            fn.setText("");
            cn.setText("");
            pw.setText("");
            bitmap = null;
            imageView.setImageBitmap(null);
            imageView.setImageResource(R.drawable.default_profile);

            accClear.setText("I already have an account");
        }
        else if (accClear.getText().equals("I already have an account")){
            Intent alreadyHave = new Intent("SIGN");
            startActivity(alreadyHave);
        }
    }
    //when user clicks on confirm button, it calls dialog
    protected void onConfirmClick(View v){
        //send an intent to create the dialog box
        Intent pswdIntent = new Intent("CONFIRM");

        //send along the inputted password so that comparison can occur
        pswdIntent.putExtra("passwd1", pw.getText().toString());
        startActivityForResult(pswdIntent, CONFIRM);
    }

    protected void onSaveClick(View v){

        //Check to make sure all required fields are full and there is a match
        if (fnEmpty) {
            Toast.makeText(this, "Please enter a valid full name", Toast.LENGTH_SHORT).show();
        }
        else if (pwEmpty) {
            Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
        }
        else if (cnEmpty) {
            Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
        }
        else if (!match) {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
            Intent pswdIntent = new Intent("CONFIRM");
            pswdIntent.putExtra("passwd1", pw.getText().toString());
            startActivityForResult(pswdIntent, CONFIRM);
        }
        else if (!avail) {
            Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
        }
        //if inputs are valid, save data in a shared preference
        else {
            SharedPreferences save = getSharedPreferences(SHARED_PREF, 0);
            final SharedPreferences.Editor editor = save.edit();


            editor.putString("Full Name", fn.getText().toString());
            editor.putString("User Name", cn.getText().toString());
            editor.putString("Password", pw.getText().toString());

            // found info and base code for saving and loading bitmap at stackoverflow
            // URL: https://stackoverflow.com/questions/17674634/saving-and-reading-bitmaps-images-from-internal-memory-in-android
            if (bitmap != null) {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                File mypath=new File(directory,"profile.jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                path = directory.getAbsolutePath();
                editor.putString("filePath", path);
            }
            editor.putBoolean("Match", match);
            editor.putBoolean("Avail", avail);
            editor.putBoolean("Logged In", true);
            Toast.makeText(this, "Data is being uploaded", Toast.LENGTH_SHORT).show();

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = PROFILE_SERVER_ADDRESS;
            Log.d("URL", url);
            try {
                JSONObject profile = new JSONObject();
                profile.put("name", cn.getText());
                profile.put("password", pw.getText());
                profile.put("full_name", fn.getText());
                if (bitmap != null) profile.put("image_path", path);
                Log.d("JSON OBJ", "put");

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                    url,
                    profile,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.get("status").equals("OK")) {
                                    editor.commit();
                                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                                    Intent saved = new Intent ("TAB");
                                    startActivity(saved);
                                }
                                else
                                    Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                                Log.d("SAVE ERROR", response.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                            Log.d("SAVE ERROR", error.toString());
                        }
                    }
                ) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Accept", "application/json");
                        return params;
                    }
                };
                queue.add(jsonObjReq);
            }
            catch (Exception e){
                Log.d("JSON SAVE ERROR", e.getMessage());
            }


        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putBoolean("match", match);
        if (profilePicUri == null) Log.d("PPU", "null");
        if (profilePicUri != null) outState.putString("uri", profilePicUri.toString());
        Log.d("SAVE", String.valueOf(match));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
        match = inState.getBoolean("match");
        if (inState.containsKey("uri")) profilePicUri =Uri.parse(inState.getString("uri"));
        if (profilePicUri == null) Log.d("PPU", "null");
        Log.d("RESTORE", String.valueOf(match));

        if (match){
            confirmButton.setTextColor(Color.GREEN);
            confirmButton.setText("Confirmed");
        }

        if(profilePicUri != null){
            bitmap = null;
            imageView.setImageBitmap(null);
            imageView.setImageURI(profilePicUri);
        }
    }
}
