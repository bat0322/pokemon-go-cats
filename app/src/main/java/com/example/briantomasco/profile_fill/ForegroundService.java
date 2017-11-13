package com.example.briantomasco.profile_fill;

import android.*;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.renderscript.RenderScript;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zacharyjohnson on 11/10/17.
 */

public class ForegroundService extends Service implements LocationListener {

    String name;
    String pic;
    double distance;

    //used code example from http://www.tutorialsface.com/2015/09/simple-android-foreground-service-example/
    public boolean running = false;
    private IBinder myBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SERVICE", "Received Start Foreground Intent ");
        name = intent.getStringExtra("name");
        pic = intent.getStringExtra("pic");
        distance = intent.getDoubleExtra("distancePref", 0);
        showNotification();
        getLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    //find current location. Borrowed heavily from class notes from LiveLocationUpdates
    protected void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Criteria criteria = GameActivity.getCriteria();
            String provider;
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager != null) {
                provider = locationManager.getBestProvider(criteria, true);
                Location loc = locationManager.getLastKnownLocation(provider);

                //this line is necessary to make sure you continue to update location after the first request
                locationManager.requestLocationUpdates(provider,0,0,this);
            }
        }
    }

    private void showNotification() {

        String id = "my_channel_1";
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Tracking " + name)
                .setContentText("You are " + (int)distance + " meters away.")
                .setSmallIcon(R.drawable.banner)
                .setOngoing(true);
        Notification notification = builder.build();
        startForeground(1, notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICE", "onBind done");
        return myBinder;

    }

    public class MyBinder extends Binder {
        ForegroundService getService() {
            return ForegroundService.this;
        }
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
        Log.d("CHANGE", "Location changed");
        distance = (int)GameActivity.getDistance();
        String id = "my_channel_1";
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Tracking " + name)
                .setContentText("You are " + (int)distance + " meters away.")
                .setSmallIcon(R.drawable.banner)
                .setOngoing(true);
        Notification notification = builder.build();
        startForeground(2, notification);

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Called when the provider status changes.
    }

}
