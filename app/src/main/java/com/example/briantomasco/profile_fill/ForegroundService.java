package com.example.briantomasco.profile_fill;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

/**
 * Created by zacharyjohnson on 11/10/17.
 */

public class ForegroundService extends Service {

    String name;
    String pic;
    Float distance;

    //used code example from http://www.tutorialsface.com/2015/09/simple-android-foreground-service-example/
    public boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SERVICE", "Received Start Foreground Intent ");
        name = intent.getStringExtra("name");
        pic = intent.getStringExtra("pic");
        distance = intent.getFloatExtra("distance", 250);
        showNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {

        String id = "my_channel_1";
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle("Tracking" + name)
                .setContentText("You are " + distance + "meters away.")
                .setSmallIcon(R.drawable.banner)
                .setOngoing(true);
        Notification notification = builder.build();
        startForeground(1, notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case if services are bound (Bound Services).
        return null;
    }
}
