package com.webswitcherPro.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.webswitcherPro.activity.MeetingActivity;

import java.util.Random;

public class BackgroundService  extends Service {

    public static final String TAG = BackgroundService.class.getSimpleName();

    public static final String ACTION_DEVICE_DISCOVERED = "DEVICE_DISCOVERED_ACTION";
    public static final String ACTION_DEVICE_LOSTED="DEVICE_LOSTED_ACTION";
    public static final String EXTRA_DEVICE = "DeviceExtra";
    public static final String EXTRA_DEVICES_COUNT = "DevicesCountExtra";
    private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";

    private static final String PUBLISHER_MIC = "PUBLISHER_MIC";


    private boolean isRunning; // Flag indicating if service is already running.

    Boolean PUBLISHER_MIC1    = false;

    private int devicesCount; // Total discovered devices count

    public static Intent createIntent(MeetingActivity context) {
        return new Intent(context,BackgroundService.class);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        setbackgroundMode();
        isRunning = false;
    }

    public void setbackgroundMode(){
        PUBLISHER_MIC1 = true;

        Intent intent = new Intent();
        intent.putExtra(PUBLISHER_MIC, PUBLISHER_MIC1);
        sendBroadcast(intent);

        Toast.makeText(this, "Your app run with Background Mode !!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        // Check if service is already active
        if (isRunning) {
            Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();

            return START_STICKY;
        }

//    startInBackground();

        isRunning = true;
        return START_STICKY;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }








}

