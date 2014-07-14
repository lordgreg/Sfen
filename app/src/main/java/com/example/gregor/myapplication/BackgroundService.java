package com.example.gregor.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * Created by Gregor on 10.7.2014.
 *
 * main background process service; this one
 * loads and runs when we press back or home button
 */
public class BackgroundService extends Service {
    final Receiver receiver = new Receiver();

    private static BackgroundService sInstance = null;

    @Override
    public void onCreate() {
        //Log.d("service", "service running");
        // set singleton instance
        sInstance = this;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Service created.", Toast.LENGTH_LONG).show();
        //Main.getInstance().setNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);
        Util.showNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);

        // start our receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(receiver, intentFilter);

        // run Events Checker every X seconds to see, if any of our events is ready to be run
        //AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, );
        // Start every 30 seconds
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);

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

    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static BackgroundService getInstance() {
        if (sInstance == null) {
            return new BackgroundService();
        }
        else
            return sInstance;
    }

    /**
     * EVENT CHECKER!
     *
     * This function os the reason of our existance! It is this function which is going to check
     * if there is any event that can be triggered by eny broadcast!
     */
    protected void EventFinder(Intent intent) {

    }
}
