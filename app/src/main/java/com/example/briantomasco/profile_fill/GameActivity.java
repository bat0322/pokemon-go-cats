package com.example.briantomasco.profile_fill;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.varunmishra.catcameraoverlay.CameraViewActivity;
import com.varunmishra.catcameraoverlay.Config;
import com.varunmishra.catcameraoverlay.OnCatPetListener;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by zacharyjohnson on 10/24/17.
 */

public class GameActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,OnCatPetListener {

    private GoogleMap map;
    final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    final int CAMERA_OVERLAY_CODE = 2;
    final String CATLIST_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/catlist.pl?";
    final String PET_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/pat.pl?";
    final String TRACK_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/track.pl?";
    private Marker self;
    private static LatLng current;
    private boolean zoomedOut = true;
    private String char_name;
    private String pw;
    private int distancePref;
    private double distanceBetween;
    private Button petButton;
    public static Button trackButton;
    private LinearLayout buttonLayout;
    public boolean tracking = false;
    private TextView bannerText;
    private ImageView bannerPic;
    private int selectedId;  // saves selected cat's ID for orientation change
    private static Marker selectedMarker;
    private Bitmap grayCatIcon;
    private Bitmap greenCatIcon;
    private ArrayList<Marker> catMarkers = new ArrayList<>();
    SharedPreferences load;
    ForegroundService thisService = null;
    private boolean isBound = false;
    //private IBinder myBinder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        broadcastReceiver();

        load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);

        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
        }
        if (load.contains("Password")) {
            pw = load.getString("Password", "");
        }
        if (load.contains("Distance")){
            distancePref = load.getInt("Distance",250);
        }

        buttonLayout = findViewById(R.id.button_layout);
        petButton = findViewById(R.id.pet_button);
        trackButton = findViewById(R.id.track_button);
        bannerText = findViewById(R.id.banner_text);
        bannerPic = findViewById(R.id.banner_image);
        grayCatIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_grey);
        greenCatIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_green);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
   /* if (tracking) {

        myBinder = thisService.getMyBinder();
    }
    */

    }


    //callback for when the Google Map is ready to be interacted with
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //googleMap.getUiSettings().setScrollGesturesEnabled(false);
        //googleMap.getUiSettings().setZoomGesturesEnabled(false);
        map = googleMap;

        //get the catlist from the server
        String url = CATLIST_SERVER_ADDRESS + "name=" + char_name + "&password=" + pw;
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest catlist = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            // loop through the response array, add to the marker list, find their distances from current location
                           for (int i =0; i < response.length(); i++) {
                               JSONObject cat = response.getJSONObject(i);
                               if (cat.has("lat") && cat.has("lng")) {
                                   LatLng catPos = new LatLng(cat.getDouble("lat"), cat.getDouble("lng"));
                                   Marker marker = map.addMarker(new MarkerOptions()
                                           .position(catPos)
                                           .icon(BitmapDescriptorFactory.fromBitmap(grayCatIcon)));
                                   marker.setTag(cat);
                                   catMarkers.add(marker);
                                   float[] results = new float[1];
                                   Location.distanceBetween(current.latitude, current.longitude, cat.getDouble("lat"), cat.getDouble("lng"), results);
                                   float diffDist = results[0];


                                   //only make the markers visible if they are within the preselected range
                                   if ((int)diffDist > distancePref) marker.setVisible(false);
                                   if (cat.getInt("catId") == selectedId) markerSelected(marker);
                                   map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                       @Override
                                       public boolean onMarkerClick(Marker marker) {
                                           markerSelected(marker);
                                           return true;
                                       }

                                   });
                               }
                           }

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),
                                    e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );

        queue.add(catlist);

        // choose map type and find the current location
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        allowLocation();
        getLocation();

        //deselect all cats when user clicks on the map
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng p0) {
                if (p0 != null) {
                    selectedId = 0;
                    if (selectedMarker != null) {
                        selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(grayCatIcon));
                        selectedMarker = null;
                    }
                    changeBannerDefault();
                }
            }
        });
    }

    // when a cat is selected, change the banner appropriately
    protected void markerSelected(Marker marker){
        JSONObject cat = (JSONObject) marker.getTag();
        if (cat != null && marker != selectedMarker) {
            try {
                // set old selected icon to gray
                if (selectedMarker != null) selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(grayCatIcon));

                // use Picasso library to load bitmap from url
                // documentation at: square.github.io/picasso/
                String imageUrl = cat.getString("picUrl");
                Picasso.with(getApplicationContext()).load(imageUrl).into(bannerPic);

                // change icon to green
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(greenCatIcon));

                // change text for the cat currently selected
                float[] results = new float[1];
                Location.distanceBetween(current.latitude, current.longitude, cat.getDouble("lat"), cat.getDouble("lng"), results);
                if (results[0] > distancePref) {
                    selectedMarker = null;
                    selectedId = 0;
                    changeBannerDefault();
                    return;
                }
                bannerText.setText(cat.getString("name") + " is " + (int)results[0] + " meters away.");
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        3.0f
                );
                bannerText.setLayoutParams(textParams);

                // expand button layout to make them visible
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                buttonLayout.setLayoutParams(layoutParams);

                // make buttons functional
                trackButton.setClickable(true);
                trackButton.setText("Track");
                trackButton.setBackgroundColor(Color.GREEN);
                trackButton.setTextColor(Color.WHITE);

                // adjust pet button based on whether cat has been petted before
                if (cat.getBoolean("petted")) {
                    petButton.setClickable(false);
                    petButton.setBackgroundColor(Color.GRAY);
                    petButton.setTextColor(Color.BLACK);

                } else {
                    petButton.setClickable(true);
                    petButton.setBackgroundColor(Color.BLUE);
                    petButton.setTextColor(Color.WHITE);
                }

                // save this cat as the selected one
                selectedId = cat.getInt("catId");
                selectedMarker = marker;

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error getting cat info", Toast.LENGTH_SHORT).show();
                Log.d("CAT MARKER", e.getMessage());
            }
        }
    }

    public void updateDistanceText() {

        try {
            if (selectedMarker != null) {
                JSONObject cat = (JSONObject) selectedMarker.getTag();
                // change text for the cat currently selected
                float[] results = new float[1];
                Location.distanceBetween(current.latitude, current.longitude, cat.getDouble("lat"), cat.getDouble("lng"), results);
                if (results[0] > distancePref) {
                    selectedMarker = null;
                    selectedId = 0;
                    changeBannerDefault();
                    return;
                }
                bannerText.setText(cat.getString("name") + " is " + (int) results[0] + " meters away.");
            }
        }
        catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error getting selected marker info", Toast.LENGTH_SHORT).show();
            Log.d("SELECTED", e.getMessage());
        }
    }

    public static float getDistance() {
        try {
            if (selectedMarker != null) {
                JSONObject cat = (JSONObject) selectedMarker.getTag();
                // change text for the cat currently selected
                float[] results = new float[1];
                Location.distanceBetween(current.latitude, current.longitude, cat.getDouble("lat"), cat.getDouble("lng"), results);
                return results[0];
            }
        }
        catch(JSONException e){
            Log.d("SELECTED", e.getMessage());
        }
        return 0;
    }

    // change banner back to default
    protected void changeBannerDefault() {

        // set image back to click
        bannerPic.setImageResource(R.drawable.click_icon);

        // change text back and inflate to full width
        bannerText.setText("Try to click the markers!");
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                4.0f
        );
        bannerText.setLayoutParams(textParams);

        // hide the buttons
        petButton.setClickable(false);
        trackButton.setClickable(false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.0f
        );
        buttonLayout.setLayoutParams(layoutParams);

    }

    //find current location. Borrowed heavily from class notes from LiveLocationUpdates
    protected void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Criteria criteria = getCriteria();
            String provider;
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                provider = locationManager.getBestProvider(criteria, true);
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc != null) {
                    updateWithNewLocation(loc);
                }
                //this line is necessary to make sure you continue to update location after the first request
                locationManager.requestLocationUpdates(provider,0,0,this);
            }
        }
    }

    //container method for finding the new current location
    protected void updateWithNewLocation(Location loc) {
        updateDistanceText();
        if (loc != null) {
            LatLng loca = new LatLng(loc.getLatitude(),loc.getLongitude());
            current = loca;

            //remove marker from previous location
            if (self != null) self.remove();
            moveToCurrentLocation(loca);

            self = map.addMarker(new MarkerOptions().position(loca).title("Your Location"));
            if (selectedMarker!=null) markerSelected(selectedMarker);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(loca, 16f));

            //loop through the cats with each update and set visibility according to distancePref as above
            for (Marker marker : catMarkers){
                JSONObject cat = (JSONObject) marker.getTag();
                float[] results = new float[1];
                try {
                    Location.distanceBetween(current.latitude, current.longitude, cat.getDouble("lat"), cat.getDouble("lng"), results);
                }
                catch (JSONException e){
                    Log.d("CAT MARKER JSON ERROR", e.getMessage());
                }
                float diffDist = results[0];
                //distanceBetween = diffDist;
                if ((int)diffDist > distancePref) marker.setVisible(false);
                else marker.setVisible(true);
            }
        }
    }

    //taken from class notes in LiveLocationUpdate. Set where the camera focus is... to current location
    private void moveToCurrentLocation(LatLng currentLocation)
    {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!bounds.contains(currentLocation) || zoomedOut ){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 1 second.
            map.animateCamera(CameraUpdateFactory.zoomTo(16f), 1000, null);
            zoomedOut = false;
        }
    }

    //set the options for finding the best provider. taken from aforementioned class notes
    protected static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        return criteria;
    }
    //Looked at code from Google Maps API on Github
    protected void allowLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // if not, request it, tell user it is needed
            Toast.makeText(this, "Location permission needed to proceed", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);

        } else if (map != null) {
            map.setMyLocationEnabled(true);
        }
    }
    //check the status of the permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                              String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    // when pet is clicked
    public void onPetClick(View v) {

        JSONObject currentCat = (JSONObject) selectedMarker.getTag();

        try {
            Config.catName = currentCat.getString("name");
            Config.catLatitude = currentCat.getDouble("lat");
            Config.catLongitude = currentCat.getDouble("lng");
            Config.locDistanceRange = 30;
            Config.useLocationFilter = false;
            Config.onCatPetListener = this;
            Intent i = new Intent(this, CameraViewActivity.class);
            startActivityForResult(i, CAMERA_OVERLAY_CODE);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //when the Track button is clicked
    public void onTrackClick(View v) {
        if (tracking) {
            tracking = false;
            Log.d("TRACK", "stopping");
            trackButton.setText("Track");
            trackButton.setBackgroundColor(Color.GREEN);
            trackButton.setTextColor(Color.WHITE);
            Intent stopIntent = new Intent();
            stopIntent.setClass(getApplicationContext(), ForegroundService.class);
            //unbindService(serviceConnection);
            stopService(stopIntent);
        }
        else {
            tracking = true;
            Log.d("TRACK", "tracking");
            trackButton.setText("Stop");
            trackButton.setBackgroundColor(Color.RED);
            trackButton.setTextColor(Color.WHITE);

            // add user info then cat/location info to pet url
            String url = TRACK_SERVER_ADDRESS + "name=" + char_name + "&password=" + pw;
            url += "&catid=" + selectedId + "&lat=" + current.latitude + "&lng=" + current.longitude;

            //send a track request
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                if (status.equals("OK")){

                                    distanceBetween = response.getDouble("distance");
                                    Intent trackIntent = new Intent ();
                                    JSONObject mark = (JSONObject) selectedMarker.getTag();
                                    String name = mark.getString("name");
                                    String pic = mark.getString("picUrl");
                                    trackIntent.putExtra("name", name);
                                    trackIntent.putExtra("distancePref", distanceBetween);
                                    trackIntent.putExtra("pic", pic);
                                    trackIntent.setClass(getApplicationContext(), ForegroundService.class);
                                    startService(trackIntent);
                                    /*Intent bindIntent = new Intent();
                                    bindIntent.setClass(getApplicationContext(), ForegroundService.class);
                                    bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                                    */





                                }
                                else {
                                    String reason = response.getString("reason");
                                    Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "Error checking with server", Toast.LENGTH_SHORT).show();
                                Log.d("TRACK JSON ERROR", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObjReq);
        }

        }

  /*  private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ForegroundService.MyBinder binder = (ForegroundService.MyBinder) service;
            thisService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            thisService = null;
            isBound = false;
        }
    };
    */

    public void onCatPet(String catName){

        // add user info then cat/location info to pet url
        String url = PET_SERVER_ADDRESS + "name=" + char_name + "&password=" + pw;
        url += "&catid=" + selectedId + "&lat=" + current.latitude + "&lng=" + current.longitude;

        //send a pet request
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")){
                                JSONObject cat = (JSONObject) selectedMarker.getTag();
                                cat.put("petted", "true");
                                selectedMarker.setTag(cat);
                                petButton.setClickable(false);
                                petButton.setBackgroundColor(Color.GRAY);
                                petButton.setTextColor(Color.BLACK);
                                Intent successIntent = new Intent("SUCCESS");
                                startActivity(successIntent);
                                finishActivity(CAMERA_OVERLAY_CODE);
                            }
                            else {
                                String reason = response.getString("reason");
                                if (reason.charAt(0) == 'T') {
                                    Toast.makeText(getApplicationContext(), "Too far from cat", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), reason, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error checking with server", Toast.LENGTH_SHORT).show();
                            Log.d("PET JSON ERROR", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjReq);
    }




    //handles configuration changes. saves which marker is selected
    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putInt("selected", selectedId);
        outState.putBoolean("tracking", tracking);
        //outState.putBinder("connection", myBinder);
        super.onSaveInstanceState(outState);
    }

    //finds the selected id and restores onConfiguration change
    @Override
    public void onRestoreInstanceState(Bundle inState){
        selectedId = inState.getInt("selected");
        tracking = inState.getBoolean("tracking");
        //myBinder = inState.getBinder("connection");
        super.onRestoreInstanceState(inState);
    }

    //need to provide the following methods in order to implement LocationListener
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onLocationChanged(Location location) {
        // Called whenever the location is changed.
        updateWithNewLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Called when the provider status changes.
    }

    @Override
    public void onDestroy() {
        //unbindService(serviceConnection);
        super.onDestroy();

    }

    public void broadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals("CHANGE")) {
                            trackButton.setText("Track");
                            trackButton.setBackgroundColor(Color.GREEN);
                            trackButton.setTextColor(Color.WHITE);
                        }
                    }
                }, new IntentFilter("CHANGE")
        );
    }

}
