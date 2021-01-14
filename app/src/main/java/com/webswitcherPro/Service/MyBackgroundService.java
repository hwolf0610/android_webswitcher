package com.webswitcherPro.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.webswitcherPro.Utils.Constant;
import com.webswitcherPro.activity.MeetingActivity;

public class MyBackgroundService extends Service {


    public MyBackgroundService() {
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

        Toast.makeText(this, "Your app run with Background Mode !!!", Toast.LENGTH_LONG).show();
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Constant.FLAG = true;
        Toast.makeText(this, "Invoke background service onStartCommand method.", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Invoke background service onDestroy method.", Toast.LENGTH_LONG).show();
    }
}
