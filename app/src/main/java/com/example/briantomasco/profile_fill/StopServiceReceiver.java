package com.example.briantomasco.profile_fill;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zacharyjohnson on 11/14/17.
 */

public class StopServiceReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 333;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ForegroundService.class);
        context.stopService(service);
    }
}
