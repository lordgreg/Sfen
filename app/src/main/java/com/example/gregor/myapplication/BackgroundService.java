package com.example.gregor.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Gregor on 10.7.2014.
 *
 * main background process service; this one
 * loads and runs when we press back or home button
 */
public class BackgroundService extends Service {
    @Override
    public void onCreate() {
        //Log.d("service", "service running");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Service created.", Toast.LENGTH_LONG).show();
        //Main.getInstance().setNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);
        Util.showNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
