package com.example.briantomasco.profile_fill;

import android.content.SharedPreferences;
import android.location.Criteria;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by zacharyjohnson on 10/24/17.
 */

public class GameActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener {

    private GoogleMap map;
    final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    final String CATLIST_SERVER_ADDRESS = "http://cs65.cs.dartmouth.edu/catlist.pl?";
    private Marker self;
    private LatLng current;
    private boolean zoomedOut = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;


        final SharedPreferences load = getSharedPreferences(CreateAcctActivity.SHARED_PREF, 0);
        String char_name = new String();
        String pw = new String();

        if (load.contains("User Name")) {
            char_name = load.getString("User Name", "");
        }
        if (load.contains("Password")) {
            pw = load.getString("Password", "");
        }

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
                           for (int i =0; i < response.length(); i++) {
                               JSONObject cat = response.getJSONObject(i);
                               if (cat.has("lat") && cat.has("lng")) {
                                   LatLng catPos = new LatLng(cat.getDouble("lat"), cat.getDouble("lng"));
                                   map.addMarker(new MarkerOptions().position(catPos).title(cat.getString("name")));
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



        // Add a marker in Hanover and move the camera
        double x = Double.parseDouble(getResources().getString(R.string.theGreen_x));
        double y = Double.parseDouble(getResources().getString(R.string.theGreen_y));

        LatLng hanover = new LatLng(x, y);
        Log.d("Coords", " " + x + " " + y);

        // Add a marker and move the camera
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.moveCamera(CameraUpdateFactory.newLatLng(hanover));
        map.moveCamera(CameraUpdateFactory.zoomTo(16f));
        allowLocation();
        getLocation();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng p0) {
                if (p0 != null) {
                    //TODO do different stuff
                }
            }
        });
    }

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
                    Log.d("LOCATION", "loc exists.");
                }
                else {
                    Log.d("LOCATION", "loc does not exist");
                }
                locationManager.requestLocationUpdates(provider,0,0,this);
            }
        }
    }

    protected void updateWithNewLocation(Location loc) {
        if (loc != null) {
            Log.d("LOCATION", "Location exists.");
            LatLng loca = new LatLng(loc.getLatitude(),loc.getLongitude());
            current = loca;
            if (self != null) self.remove();
            self = map.addMarker(new MarkerOptions().position(loca).title("Your Location"));
            moveToCurrentLocation(loca);
        }
        else {
            Log.d("LOCATION", "Location does not exist");
        }

    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!bounds.contains(currentLocation) || zoomedOut ){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 1 second.
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
            zoomedOut = false;
        }
    }

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
