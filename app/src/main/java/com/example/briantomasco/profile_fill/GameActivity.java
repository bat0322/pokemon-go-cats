package com.example.briantomasco.profile_fill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.os.Bundle;
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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
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

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by zacharyjohnson on 10/24/17.
 */

public class GameActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private GoogleMap map;
    final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    final String CATLIST_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/catlist.pl?";
    final String PET_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/pat.pl?";
    private Marker self;
    private LatLng current;
    private boolean zoomedOut = true;
    private String char_name;
    private String pw;
    private int distance;
    private Button petButton;
    private TextView bannerText;
    private ImageView bannerPic;
    private int selectedId;  // saves selected cat's ID for orientation change
    private Marker selectedMarker;
    private Bitmap grayCatIcon;
    private Bitmap greenCatIcon;
    private ArrayList<Marker> catMarkers = new ArrayList<>();
    SharedPreferences load;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);

        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
        }
        if (load.contains("Password")) {
            pw = load.getString("Password", "");
        }
        if (load.contains("Distance")){
            distance = load.getInt("Distance",250);
            Log.d("DISTANCE", Integer.toString(load.getInt("Distance", 2)));
        }

        petButton = findViewById(R.id.pet_button);
        bannerText = findViewById(R.id.banner_text);
        bannerPic = findViewById(R.id.banner_image);
        grayCatIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_grey);
        greenCatIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker_green);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //callback for when the Google Map is ready to be interacted with
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
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
                                   Log.d("DIFF", Integer.toString((int)diffDist));


                                   //only make the markers visible if they are within the preselected range
                                   if ((int)diffDist > distance) marker.setVisible(false);
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
        if (cat != null) {
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
                if (results[0] > distance) {
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

                // show button as gray or blue depending on if it's been petted
                petButton.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                petButton.setLayoutParams(buttonParams);
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

        // hide the pet button
        petButton.setVisibility(View.INVISIBLE);
        petButton.setClickable(false);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.0f
        );


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
        if (loc != null) {
            LatLng loca = new LatLng(loc.getLatitude(),loc.getLongitude());
            current = loca;

            //remove marker from previous location
            if (self != null) self.remove();
            moveToCurrentLocation(loca);

            self = map.addMarker(new MarkerOptions().position(loca).title("Your Location"));
            if (selectedMarker!=null) markerSelected(selectedMarker);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(loca, 18f));

            //loop through the cats with each update and set visibility according to distance as above
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
                if ((int)diffDist > distance) marker.setVisible(false);
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
            map.animateCamera(CameraUpdateFactory.zoomTo(18f), 1000, null);
            zoomedOut = false;
        }
    }

    //set the options for finding the best provider. taken from aforementioned class notes
    protected Criteria getCriteria() {
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
        super.onSaveInstanceState(outState);
    }

    //finds the selected id and restores onConfiguration change
    @Override
    public void onRestoreInstanceState(Bundle inState){
        super.onRestoreInstanceState(inState);
        selectedId = inState.getInt("selected");
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
}
