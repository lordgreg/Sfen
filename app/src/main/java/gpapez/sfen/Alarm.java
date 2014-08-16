package gpapez.sfen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Gregor on 22.7.2014.
 */
public class Alarm {

    /*
        create variables we need for specific alarms
     */
    private PendingIntent mPendingIntent;
    private AlarmManager mAlarmManager;
    private int mConditionID;
    private int mAlarmID = new Random().nextInt();
    protected String mIntentExtra = "";

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

    public Alarm(Context context, int conditionId) {
        mContext = context;
        mConditionID = conditionId;

    }

    /**
     * get pendingintent or if null, create new one!
     */
    protected PendingIntent getPendingIntent() {
        if (mPendingIntent != null)
            return mPendingIntent;
        else {
            Intent mIntent = new Intent(getClass().getPackage().getName() + ".ALARM_TRIGGER");

            if (!mIntentExtra.equals(""))
                mIntent.putExtra("ALARM_TRIGGER_EXTRA", mIntentExtra);


            return PendingIntent.getBroadcast(
                    //mContext,
                    //Main.getInstance(),
                    BackgroundService.getInstance(),
                    //Receiver.class,
                    mAlarmID,
                    mIntent,
                    //new Intent(getClass().getPackage().getName() +".ALARM_TRIGGER").putExtra("ALARM_TRIGGER_EXTRA", mIntentExtra),
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

        }
    }

    /**
     * create single alarm repeating every X seconds
     */
    protected void CreateAlarmRepeating(Calendar cal, long interval) {
        isRepeating = true;
        //mPendingIntent = getPendingIntent();


        mAlarmManager = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);


        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, getPendingIntent());
        Log.d("sfen", "Starting repeating alarm at: " + cal.getTime().toString()
                + " (repeating every " + interval + " miliseconds)");
    }

    /**
     * create single INEXACT alarm repeating every X
     * https://developer.android.com/training/scheduling/alarms.html
     */
    protected void CreateInexactAlarmRepeating(Calendar cal, long interval) {
        isRepeating = true;
        mPendingIntent = getPendingIntent();


        mAlarmManager = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);


        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, mPendingIntent);
        Log.d("sfen", "Starting Inexact repeating alarm at: " + cal.getTime().toString()
                + " (repeating every " + interval + " miliseconds)");
    }

    /**
     * create one time only alarm
     */
    protected void CreateAlarm(Calendar cal) {
        mPendingIntent = getPendingIntent();

        mAlarmManager = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), mPendingIntent);
        Log.d("sfen", "Starting alarm at: " + cal.getTime().toString());
    }

    /**
     * create one time only alarm in X seconds
     */
    protected void CreateAlarm(int seconds) {
        mPendingIntent = getPendingIntent();

        mAlarmManager = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, seconds * 1000, mPendingIntent);

        Log.d("sfen", "Starting alarm in: " + seconds + " seconds.");
    }

    /**
     * destroying current alarm
     */
    protected void RemoveAlarm() {
        mAlarmManager.cancel(mPendingIntent);
        Log.d("sfen", "Alarm removed.");
    }

    /**
     * retrieve the alarm
     */
    public AlarmManager getAlarm() {
        return mAlarmManager;
    }

    public int getmAlarmID() {
        return mAlarmID;
    }

    public void setmAlarmID(int mAlarmID) {
        this.mAlarmID = mAlarmID;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public int getConditionID() {
        return mConditionID;
    }

    public void setConditionID(int mConditionID) {
        this.mConditionID = mConditionID;
    }
}
