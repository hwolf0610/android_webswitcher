package com.webswitcherPro.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.tokbox.android.tutorials.webswitcherpro.R;
import com.webswitcherPro.Utils.Constant;
import com.webswitcherPro.activity.MainActivity;
import com.webswitcherPro.activity.MeetingActivity;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "Webswitcher Pro";
    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

//        Intent i = new Intent(MyBackgroundService.this, MeetingActivity.class);
//        PUBLISHER_MIC = true;
//        i.putExtra("PUBLISHER_MIC", PUBLISHER_MIC );

        Toast.makeText(this, "Your app run with Foreground Mode !!!", Toast.LENGTH_LONG).show();
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constant.FLAG = true;

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MeetingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Webswitcher Pro")
                .setContentText("Webswitcher Pro Foreground service")
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        //do heavy work on a background thread
        //stopSelf();

        return START_NOT_STICKY;


//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Invoke Foreground service onDestroy method.", Toast.LENGTH_LONG).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Webswitcher Pro Foreground Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
