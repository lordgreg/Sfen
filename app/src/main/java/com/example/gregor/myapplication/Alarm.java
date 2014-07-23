package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Gregor on 22.7.2014.
 */
public class Alarm {

    /*
        create variables we need for specific alarms
     */
    private PendingIntent mPendingIntent;
    private AlarmManager mAlarmManager;

    // use transient with gson 1.7.1
    private transient Context mContext;

    private boolean isRepeating = false;


    /**
     * constructor
     */
    public Alarm(Context context/*, Intent intent*/) {
        mContext = context;
        //mIntent = intent;
    }

    /**
     * destroying current alarm
     */
    protected void RemoveAlarm() {
        mAlarmManager.cancel(mPendingIntent);
    }

    /**
     * get pendingintent or if null, create new one!
     */
    protected PendingIntent getPendingIntent() {
        if (mPendingIntent != null)
            return mPendingIntent;
        else {

            return PendingIntent.getBroadcast(
                    mContext,
                    0,
                    new Intent(getClass().getPackage().getName() +".ALARM_TRIGGER"),
                    0
            );

        }
    }

    /**
     * create array of alarms
     */
    protected void CreateAlarm(ArrayList<Alarm> alarmList) {



    }

    /**
     * create single alarm repeating every X seconds
     */
    protected void CreateAlarmRepeating(Calendar cal, int intervalSeconds) {
        isRepeating = true;
        mPendingIntent = getPendingIntent();

        mAlarmManager = (AlarmManager)mContext.getSystemService(Activity.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), intervalSeconds*1000, getPendingIntent());
        System.out.println("*** starting at: "+ cal.getTime().toString());
    }

    /**
     * create one time only alarm
     */
    protected void CreateAlarm(Calendar cal) {
        mPendingIntent = getPendingIntent();

        mAlarmManager = (AlarmManager)mContext.getSystemService(Activity.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), getPendingIntent());
        System.out.println("*** starting at: "+ cal.getTime().toString());
    }

    /**
     * create one time only alarm in X seconds
     */
    protected void CreateAlarm(int seconds) {
        mPendingIntent = getPendingIntent();

        mAlarmManager = (AlarmManager)mContext.getSystemService(Activity.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, seconds*1000, getPendingIntent());

    }


    /**
     * retrieve the alarm
     */
    public AlarmManager getAlarm() {
        return mAlarmManager;
    }

    public boolean isRepeating() {
        return isRepeating;
    }
}
