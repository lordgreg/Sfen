package gpapez.sfen;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Gregor on 14.7.2014.
 */
public class Receiver extends BroadcastReceiver {

    /**
     * singleton object
     */
    private Receiver sInstance;


    /**
     * intentfilter object
     */
    //private IntentFilter mIntentFilter;

    /**
     * telephony state receiver object
     */
    private TelephonyManager telephonyManager;


    /**
     * LIST OF AVAILABLE BROADCASTS
     */
    protected static ArrayList<String> sBroadcasts = new ArrayList<String>() {{

        // in-app broadcast calls
        add(getClass().getPackage().getName() +".BRIGHTNESS_SET");
        add(getClass().getPackage().getName() +".EVENT_ENABLED");
        add(getClass().getPackage().getName() +".EVENT_DISABLED");
        add(getClass().getPackage().getName() +".GEOFENCE_ENTER");
        add(getClass().getPackage().getName() +".GEOFENCE_EXIT");
        add(getClass().getPackage().getName() +".ALARM_TRIGGER");
        add(getClass().getPackage().getName() +".CELL_LOCATION_CHANGED");
        add(getClass().getPackage().getName() +".ROOT_GRANTED");



    }};

    /**
     * LIST OF AVAILABLE SYSTEM BROADCASTS
     */
    protected static ArrayList<String> sBroadcastsSystem = new ArrayList<String>() {{

        add(Intent.ACTION_SCREEN_ON);                   // screen on
        add(Intent.ACTION_SCREEN_OFF);                  // screen off
        add(LocationManager.MODE_CHANGED_ACTION);       // gps on/off
        add(Intent.ACTION_BATTERY_CHANGED);             // battery status
        add(WifiManager.NETWORK_STATE_CHANGED_ACTION);   // wifi toggle

    }};


    /**
     * list of added system intent filters
     */
    private ArrayList<String> mAllowableFilters = new ArrayList<String>();



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

        /**
         * if current action is in our allow list, lets run it
         * otherwise return the process.
         */
        if (!isActionAllowedToRun(action)) {
            //Log.d("sfen", "Broadcast recieved "+ action +" but not needed.");
            return ;
        }
        else
            Log.i("sfen", "Received: " + action);


        // wakelock object that will eventually trigger on specific broadcasts.
        PowerManager.WakeLock mWakeLock = null;




        /**
         * check if GPS is enabled/disabled
         */
        if (action.equals("android.location.MODE_CHANGED")) {


        }

        /**
         * battery changed
         */
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {


        }

        /**
         * screen off will create alarm that will wake up the phone every now and then to
         * eventfinders
         */
//        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
//
//            /**
//             * create inexact repeating alarm here.
//             */
//            //if (mAlarmWakeLock != null)
//            //    mAlarmWakeLock.RemoveAlarm();
////            if (mAlarmWakeLock == null) {
////                Log.i("sfen", "Adding inexact alarm.");
////                mAlarmWakeLock = new Alarm(context);
////                mAlarmWakeLock.mIntentExtra = "wake-a-lambada!";
////
////                Calendar calendar = Calendar.getInstance();
////                calendar.add(Calendar.MILLISECOND, (int)WAKELOCK_TIMER);
//
//// TODO: TRY EVERYTHING WITHOUT WAKELOCK ON SLEEP!
////                mAlarmWakeLock.CreateInexactAlarmRepeating(calendar,
////                        WAKELOCK_TIMER
////                );
////            }
//
//
//        }

        /**
         * cancel sleepalarm when we turn on screen
         * aka. we wake up the phone
         */
//        if (action.equals(Intent.ACTION_SCREEN_ON)) {
//            //System.out.println("screen on!");
////            if (mAlarmWakeLock != null) {
////                Log.i("sfen", "Removing inexact alarm.");
////                mAlarmWakeLock.RemoveAlarm();
////                mAlarmWakeLock = null;
////            }
//        }



        if (action.equals(getClass().getPackage().getName() +".ALARM_TRIGGER")) {
//
//            // SLEEPY ALARM HERE
//            if (intent.getStringExtra("ALARM_TRIGGER_EXTRA") != null &&
//                    !intent.getStringExtra("ALARM_TRIGGER_EXTRA").equals("")) {
//
//                Log.i("sfen", "Wakelock alarm trigger: "+
//                                intent.getStringExtra("ALARM_TRIGGER_EXTRA")
//                );
//
//            }


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
        if (mCallBroadcast) {
            BackgroundService.getInstance().EventFinder(context, intent);
        }

        // release wakelock if set
        if (mWakeLock != null)
            mWakeLock.release();

        // set mCallBroadcast back to true
        mCallBroadcast = true;
    }


    /**
     *
     * constructor
     *
     */
//    public Receiver() {
//    }


    /**
     *
     * Create Intent Filter
     *
     */
    public IntentFilter createIntentFilter() {

        IntentFilter mIntentFilter = new IntentFilter();

        // add allowable broadcasts
        for (int i = 0; i < sBroadcasts.size(); i++) {
            mIntentFilter.addAction(sBroadcasts.get(i));
        }

        // add system intents
        for (int  i = 0; i < sBroadcastsSystem.size(); i++) {
            mIntentFilter.addAction(sBroadcastsSystem.get(i));
        }


        return mIntentFilter;

    }


    /**
     * return allowed filters array
     */
    private ArrayList<String> returnAllowedFilters() {
        ArrayList<String> result = new ArrayList<String>();

        result.addAll(sBroadcasts);
        result.addAll(mAllowableFilters);

        return result;
    }


    /**
     * is action allowed?
     *
     * just check all filters and return bool
     */
    private boolean isActionAllowedToRun(String action) {

        return returnAllowedFilters().contains(action);

    }


    /**
     * Add filters to allowable list
     */
    protected void addFiltersToAllowable(ArrayList<String> newFilters) {

        /**
         * add only if key still doesn't exist
         */
        for (int i = 0; i < newFilters.size(); i++) {

            if (!mAllowableFilters.contains(newFilters.get(i)))
                mAllowableFilters.add(newFilters.get(i));

        }


    }

}
