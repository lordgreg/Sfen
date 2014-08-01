package gpapez.sfen;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Gregor on 14.7.2014.
 */
public class Receiver extends BroadcastReceiver {

    // every X seconds, wakelock will wake up our device;
    // set time here (miliseconds!)
    //final long WAKELOCK_TIMER = 5 * 60 * 1000;
    final long WAKELOCK_TIMER = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

    // wakelock object which will store our alarm status
    Alarm mAlarmWakeLock = null;

    boolean mCallBroadcast = true;



    /**
     * RECEIVER FUNCTION
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BackgroundService.getInstance().receiverAction = action;

        // wakelock object that will eventually trigger on specific broadcasts.
        PowerManager.WakeLock mWakeLock = null;

        Log.i("sfen", "received: " + action);


        /**
         * check if GPS is enabled/disabled
         */
        if (action.equals("android.location.MODE_CHANGED")) {

            // we wont call broadcast in any other case than in GPS enabled or disabled!
            //mCallBroadcast = false;

//            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//
////            List<String> providers = locationManager.getProviders(true);
////            System.out.println("enabled providers: "+ providers.toString());
//
//            boolean gpsEnabled = false;
//            try{gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
//            System.out.println("gps enabled: "+ gpsEnabled);


        }

        /**
         * screen off will create alarm that will wake up the phone every now and then to
         * eventfinders
         */
        if (action.equals(Intent.ACTION_SCREEN_OFF)) {

            /**
             * create inexact repeating alarm here.
             */
            //if (mAlarmWakeLock != null)
            //    mAlarmWakeLock.RemoveAlarm();
            if (mAlarmWakeLock == null) {
                Log.i("sfen", "Adding inexact alarm.");
                mAlarmWakeLock = new Alarm(context);
                mAlarmWakeLock.mIntentExtra = "wake-a-lambada!";

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MILLISECOND, (int)WAKELOCK_TIMER);

                mAlarmWakeLock.CreateInexactAlarmRepeating(calendar,
                        WAKELOCK_TIMER
                );
            }


        }

        /**
         * cancel sleepalarm when we turn on screen
         * aka. we wake up the phone
         */
        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            //System.out.println("screen on!");
            if (mAlarmWakeLock != null) {
                Log.i("sfen", "Removing inexact alarm.");
                mAlarmWakeLock.RemoveAlarm();
            }
        }



        if (action.equals(getClass().getPackage().getName() +".ALARM_TRIGGER")) {

            // SLEEPY ALARM HERE
            if (intent.getStringExtra("ALARM_TRIGGER_EXTRA") != null &&
                    !intent.getStringExtra("ALARM_TRIGGER_EXTRA").equals("")) {

                Log.i("sfen", "Wakelock alarm trigger: "+
                                intent.getStringExtra("ALARM_TRIGGER_EXTRA")
                );

            }


            // when triggering for alarm, we have to use wakelock so it wakes
            // up the device and calls eventfinder
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sfen");
            mWakeLock.acquire();

        }

        //if (action.equals(WifiManager.EXTRA_SUPPLICANT_CONNECTED)) {
        //if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();

            //System.out.println("*** WIFI STATE: "+ state);

            if (state == NetworkInfo.State.CONNECTING ||
                    state == NetworkInfo.State.DISCONNECTING ||
                    state == NetworkInfo.State.SUSPENDED ||
                    state == NetworkInfo.State.UNKNOWN
                    ) {
                mCallBroadcast = false;
            }

            if (state == NetworkInfo.State.CONNECTED) {
                // save latest connected SSID
                BackgroundService.getInstance().mLatestSSID =
                        manager.getConnectionInfo().getSSID().replace("\"", "");
            }

        }


        // RUN OUR FUNCTION
        if (mCallBroadcast)
            BackgroundService.getInstance().EventFinder(context, intent);

        // release wakelock if set
        if (mWakeLock != null)
            mWakeLock.release();

        // set mCallBroadcast back to true
        mCallBroadcast = true;
    }

}
