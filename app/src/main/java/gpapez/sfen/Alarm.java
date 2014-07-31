package gpapez.sfen;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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
            Intent mIntent = new Intent(getClass().getPackage().getName() +".ALARM_TRIGGER");

            if (!mIntentExtra.equals(""))
                mIntent.putExtra("ALARM_TRIGGER_EXTRA", mIntentExtra);

            return PendingIntent.getBroadcast(
                    mContext,
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
        mPendingIntent = getPendingIntent();


        mAlarmManager = (AlarmManager)mContext.getSystemService(Activity.ALARM_SERVICE);


        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, getPendingIntent());
        System.out.println("*** Starting repeating alarm at: "+ cal.getTime().toString()
                +" (repeating every "+ interval +" miliseconds)");
    }

    /**
     * create single INEXACT alarm repeating every X
     * https://developer.android.com/training/scheduling/alarms.html
     */
    protected void CreateInexactAlarmRepeating(Calendar cal, long interval) {
        isRepeating = true;
        mPendingIntent = getPendingIntent();


        mAlarmManager = (AlarmManager)mContext.getSystemService(Activity.ALARM_SERVICE);


        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, getPendingIntent());
        System.out.println("*** Starting Inexact repeating alarm at: "+ cal.getTime().toString()
                +" (repeating every "+ interval +" miliseconds)");
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
     * destroying current alarm
     */
    protected void RemoveAlarm() {
        mAlarmManager.cancel(mPendingIntent);
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

    public int getConditionID() {
        return mConditionID;
    }

    public void setConditionID(int mConditionID) {
        this.mConditionID = mConditionID;
    }
}
