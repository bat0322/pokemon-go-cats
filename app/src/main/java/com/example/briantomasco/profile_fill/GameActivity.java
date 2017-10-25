package com.example.briantomasco.profile_fill;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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

/**
 * Created by zacharyjohnson on 10/24/17.
 */

public class GameActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap map;

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

        // Add a marker in Hanover and move the camera
        double x = Double.parseDouble( getResources().getString(R.string.theGreen_x) );
        double y = Double.parseDouble( getResources().getString(R.string.theGreen_y) );

        LatLng hanover = new LatLng( x, y );
        Log.d("Coords", " " + x + " " + y );

        // Add a marker and move the camera
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.addMarker(new MarkerOptions().position(hanover).title("Marker in Hanover"));
        map.moveCamera(CameraUpdateFactory.newLatLng(hanover));
        map.moveCamera(CameraUpdateFactory.zoomTo(18f));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng p0) {
                if( p0 != null ) {
                    Log.d("Map", p0.toString());
                    map.addMarker(new MarkerOptions().position(p0).title(p0.toString()));
                }
            }
        });
    }


}
