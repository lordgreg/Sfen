package gpapez.sfen;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Gregor on 10.7.2014.
 *
 * main background process service; this one
 * loads and runs when we press back or home button
 */
public class BackgroundService extends Service {

    final Receiver receiver = new Receiver();
    protected String receiverAction = "";
    private boolean isOneRunning = false;
    private boolean isOneStopping = false;
    protected String mLatestSSID = "";

    protected Preferences mPreferences;
    protected Util mUtil = new Util();

    // alarmmanager
    protected Alarm mAlarm;
    protected ArrayList<Alarm> mActiveAlarms = new ArrayList<Alarm>();

    // geofence init
    private GeoLocation geoLocation;
    protected List<Geofence> mTriggeredGeofences = new ArrayList<Geofence>();
    protected int mTriggeredGeoFenceTransition = -1;

    // telephony
    protected ReceiverPhoneState mPhoneReceiver;
    protected TelephonyManager mPhoneManager;


    // class specific
    private static BackgroundService sInstance = null;
    protected Intent sIntent = null;

    // list of all allowable broadcasts
    private static ArrayList<String> sBroadcasts = new ArrayList<String>() {{
        // system-based broadcast calls
        add(WifiManager.NETWORK_STATE_CHANGED_ACTION);  // wifi disable/enable/connect/disconnect
        add(Intent.ACTION_SCREEN_ON);                   // screen on
        add(Intent.ACTION_SCREEN_OFF);                  // screen off
        //add(Intent.ACTION_AIRPLANE_MODE_CHANGED);     // toggle airplane
        add(LocationManager.MODE_CHANGED_ACTION);

        // in-app broadcast calls
        add(getClass().getPackage().getName() +".EVENT_ENABLED");
        add(getClass().getPackage().getName() +".EVENT_DISABLED");
        add(getClass().getPackage().getName() +".GEOFENCE_ENTER");
        add(getClass().getPackage().getName() +".GEOFENCE_EXIT");
        add(getClass().getPackage().getName() +".ALARM_TRIGGER");
        add(getClass().getPackage().getName() +".CELL_LOCATION_CHANGED");
    }};
    

    @Override
    public void onCreate() {
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // believe it or not, but this notification will take care of our
        // background service!
        Util.showNotification(sInstance, getString(R.string.app_name), "", R.drawable.ic_launcher);

        // set intent
        sIntent = intent;

        // start preferences
        mPreferences = new Preferences(Main.getInstance());


        // get alarm preferences
        /**
         * REMIND ME AGAIN...
         *
         * why would we need to save alarms in preferences? o_O
         */
        /*
        mActiveAlarms = (ArrayList<Alarm>) mPreferences.getPreferences("alarms", Preferences.REQUEST_TYPE.ALARMS);
        if (mActiveAlarms == null)
            mActiveAlarms = new ArrayList<Alarm>();
            */
        mActiveAlarms = new ArrayList<Alarm>();


        // start our receiver
        IntentFilter intentFilter = new IntentFilter();

        // add allowable broadcasts
        for (int i = 0; i < sBroadcasts.size(); i++) {
            //Log.e("adding broad to intent", sBroadcasts.get(i));
            intentFilter.addAction(sBroadcasts.get(i));
        }
        registerReceiver(receiver, intentFilter);


        // done: create new objects of GeoLocation & mPhoneReceiver ONLY if we have at least one event
        // start GeoLocation class
        //geoLocation = new GeoLocation(sInstance);

        // start phone listener
//        mPhoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        mPhoneReceiver = new ReceiverPhoneState();
//
//        mPhoneManager.listen(mPhoneReceiver,
//                //PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
//                PhoneStateListener.LISTEN_CELL_LOCATION
//        );



        // also check for the first time and never again, for the condition triggers
        // refresh condition timers
        if (Main.getInstance().events.size() > 0)
            updateEventConditionTimers(Main.getInstance().events);


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Destroy service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister our receiver
        unregisterReceiver(receiver);

        // cancel phone state listener
        if (mPhoneManager != null)
            mPhoneManager.listen(mPhoneReceiver,PhoneStateListener.LISTEN_NONE);


        // cancel alarms (if up)
        if (mActiveAlarms.size() > 0) {
            ArrayList<Alarm> mAlarmsDelete = new ArrayList<Alarm>();
            for (Alarm single : mActiveAlarms) {
                mAlarmsDelete.add(single);
            }
            //mActiveAlarms.removeAll(mAlarmsDelete);

            if (mAlarmsDelete.size() > 0) {
                //System.out.println("*** deleting "+ mAlarmsDelete.size() +" alarms.");
                for (Alarm single : mAlarmsDelete) {
                    // stop the alarm
                    single.RemoveAlarm();

                    // remove it from array of active alarms
                    mActiveAlarms.remove(single);
                }
            }

        }

        if (mActiveAlarms.size() == 0)
            Log.d("sfen", "All alarms removed.");

        // save new alarms to preferences
        //mPreferences.setPreferences("alarms", mActiveAlarms);

        // destroy all geofences
        if (geoLocation != null)
            geoLocation.RemoveGeofences(geoLocation.getTransitionPendingIntent());
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
     * This function os the reason of our existence! It is this function which is going to check
     * if there is any event that can be triggered by eny broadcast!
     */
    protected void EventFinder(Context context, Intent intent) {
        // loop through all events, check only the enabled ones..
        for (Event e : Main.getInstance().events) {
            if (e.isEnabled() /* & !e.isRunning() */) {
            // NEW IF CONDITION
//            if (e.isEnabled() &&
//                    (( !e.isHasRun() && e.isRunOnce() )
//                    ||
//                    ( !e.isRunOnce()))
//                    ) {
                // if it is still not running, then, we have a candidate to check conditions..


                /**
                 * any broadcast EXCEPT "EVENT_ENABLED" can re-trigger loading actions IF
                 * we told him to runOnce only.
                 */
                // event runs only once? then all broadcasts EXCEPT event_enable can re-run it
                boolean canContinueRunning = false;

                if (receiverAction.equals(getClass().getPackage().getName() +".EVENT_ENABLED")
                        && e.isRunOnce() && e.isHasRun() && !e.isForceRun())
                    canContinueRunning = false;
                else
                    canContinueRunning = true;


                if (areEventConditionsMet(context, intent, e) && canContinueRunning) {

                    isOneRunning = true;

                    // wow. conditions are met! you know what that means?
                    // we trigger actions!
                    runEventActions(context, intent, e);

                    // TODO: store action to log.
                }
                // conditions aren't met; switch event to not running (if maybe they were)
                else {
                    e.setRunning(false);
                    //e.setHasRun(false);
                }
            }
            // maybe it isn't enabled
            // maybe it did run already
            // or something else o_O
            else {
                e.setRunning(false);
                //e.setHasRun(false);
            }
        }

        // if there's no events running OR events stopping, clear notification
        if ((!isOneRunning && isOneStopping) || (!isOneRunning && !isOneStopping)) {
            Log.d("sfen", "no events running.");
            mUtil.showNotification(sInstance, getString(R.string.app_name), "", R.drawable.ic_launcher);
            // what.
        }

        // clear all variables
        isOneRunning = false;
        isOneStopping = false;
        mTriggeredGeoFenceTransition = -1;
        mTriggeredGeofences = null;

        // if we have main activity window open, refresh them
        if (Main.getInstance().isVisible) {
            Main.getInstance().refreshEventsView();
        }

    }

    /**
     * Checks Event for all conditions and returns boolean for every condition
     * @param context
     * @param intent
     * @param e
     * @return boolean if event conditions are met.
     */
    private boolean areEventConditionsMet(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        boolean ret = false;
        final String action = intent.getAction();

        ArrayList<DialogOptions> conditions = e.getConditions();

        // array with booleans for all conditions
        ArrayList<Boolean> conditionResults = new ArrayList<Boolean>();
        Log.d("sfen", "EVENT "+ e.getName());



        for (DialogOptions cond : conditions) {
            Log.d("sfen", "condition "+ cond.getOptionType());

            // single condition variables
            //String hashCode = ""+ e.hashCode() + cond.hashCode();
            String hashCode = e.getUniqueID() +""+ cond.getUniqueID();

            switch (cond.getOptionType()) {
                case SCREEN_ON:
                    // if we're triggering after screen is going on?
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        conditionResults.add(true);
                    }
                    // this wasnt called by broadcast, lets check if screen is on then
                    else {
                        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                        if (powerManager.isScreenOn())
                            conditionResults.add(true);

                        else
                            conditionResults.add(false);
                    }

                    break;

                case SCREEN_OFF:
                    // totally opposite of turning screen on :)
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        conditionResults.add(true);
                    }
                    else {
                        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                        if (!powerManager.isScreenOn())
                            conditionResults.add(true);

                        else
                            conditionResults.add(false);

                    }


                    break;


                case GPS_ENABLED:
                case GPS_DISABLED:

                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                    //            List<String> providers = locationManager.getProviders(true);
                    //            System.out.println("enabled providers: "+ providers.toString());

                    boolean gpsEnabled = false;
                    try{gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
                    //System.out.println("gps enabled: "+ gpsEnabled);

                    if (gpsEnabled) {
                        if (cond.getOptionType() == DialogOptions.type.GPS_ENABLED)
                            conditionResults.add(true);
                        else
                            conditionResults.add(false);
                    }
                    else {
                        if (cond.getOptionType() == DialogOptions.type.GPS_ENABLED)
                            conditionResults.add(false);
                        else
                            conditionResults.add(true);

                    }


                    break;


                case DAYSOFWEEK:

                    if (cond.getSetting("selectedDays") == null) return false;

                    gson = new Gson();
                    final ArrayList<Integer> days = gson.fromJson(cond.getSetting("selectedDays"),
                            new TypeToken<List<Integer>>(){}.getType());

                    // 0=monday, 1=tuesday ... 6=sunday
                    //String[] days = cond.getSetting("selectedDays");
                    Calendar calendar = Calendar.getInstance();
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK); // start from 0

                    //System.out.println("current day: "+ currentDay +"; saved days: "+ days.toString());

                    currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                    //System.out.println("mon, tue, sat, sun: "+ Calendar.MONDAY +", "+ Calendar.TUESDAY +", "+ Calendar.SATURDAY +", "+ Calendar.SUNDAY);

                    // is current day in array of selected days?
                    // its not, so break the loop and return false
                    // java Calendar.DAY =  mon, tue, sat, sun: 2, 3, 7, 1
                    // our array            mon, tue, sat, sun: 0, 1, 5, 6

                    // because of my logic explained above, i have to change currentDay retrieved
                    // from Calendar;
                    //currentDay = currentDay+5;
                    //if (currentDay>=7) currentDay = currentDay - 7;
                    currentDay = (currentDay+5 >= 7) ? currentDay-2 : currentDay+5;

                    //System.out.println("current day new: "+ currentDay);

                    if (days.indexOf(currentDay) == -1) {
                        conditionResults.add(false);
                    }

                    // it is, so fix return value for the next condition;
                    else
                        conditionResults.add(true);


                    break;

                case WIFI_CONNECT:


                    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    // first thing if condition WIFI_CONNECT, are we connected to wifi?
                    if (networkInfo.isConnected()) {
                        //System.out.println("yes, we are connected. "+ networkInfo.getTypeName());

                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                        String currentSsid = wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);

                        //System.out.println(wifiInfo.toString());
                        final ArrayList<String> ssid = gson.fromJson(cond.getSetting("selectedWifi"),
                                new TypeToken<List<String>>(){}.getType());

                        // what ssid we connected on?
                        //System.out.println("ssid "+ wifiInfo.getSSID());
                        //System.out.println(wifiInfo.toString());
                        //System.out.println("is current ssid in condition array? "+ ssid.indexOf(currentSsid));

                        // if connected ssid = the one from array of conditioned, return true.
                        if (ssid.indexOf(currentSsid) != -1) {
                            conditionResults.add(true);
                        }
                        else
                            conditionResults.add(false);



                    }
                    // wifi connect case should return false otherwise, since we're not connected
                    // anyways
                    else {
                        conditionResults.add(false);
                    }


                    break;


                case WIFI_DISCONNECT:

                    ConnectivityManager dconnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo dcnetworkInfo = dconnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    // first thing if condition WIFI_DISCONNECT, did we disconnected?
                    if (!dcnetworkInfo.isConnected()) {
                        //System.out.println("we aren't connected to any wifi. latest remembered ssid was "+ mLatestSSID);

                        // get ssid from settings
                        final ArrayList<String> ssid = gson.fromJson(cond.getSetting("selectedWifi"),
                                new TypeToken<List<String>>(){}.getType());

                        //for (String single : ssid) {
                        if (ssid.indexOf(mLatestSSID) != -1)
                            conditionResults.add(true);
                        else
                            conditionResults.add(false);
                        //}
                    }
                    // if we connected, just return false
                    else {
                        conditionResults.add(false);
                    }



                    break;


                case TIMERANGE:
                    Calendar current = Calendar.getInstance();

                    // add 1 second to current if we got a trigger just on the same second as start
                    // time
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, 1);

                    Calendar cStart = Calendar.getInstance();
                    Calendar cEnd = Calendar.getInstance();

                    cStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(cond.getSetting("fromHour")));
                    cStart.set(Calendar.MINUTE, Integer.parseInt(cond.getSetting("fromMinute")));

                    cEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(cond.getSetting("toHour")));
                    cEnd.set(Calendar.MINUTE, Integer.parseInt(cond.getSetting("toMinute")));

                    // if end time is small than start time, usually means end date is after midnight
                    if (cEnd.before(cStart)) {
                        cEnd.add(Calendar.DATE, 1);
                        //cal.add(Calendar.DATE, 1);
                    }

                    // also, check if current date is lower than start date
                    // AND start & end times aren't in the same day
//                    if (cal.before(cStart) &&
//                            cStart.get(Calendar.DAY_OF_YEAR) != cEnd.get(Calendar.DAY_OF_YEAR)
//                            ) {
//                        //cal.add(Calendar.DATE, 1);
//                        cStart.add(Calendar.DATE, -1);
//                    }

//                    Log.e("test", "current date: "+ cal.getTime().toString());
//                    Log.e("test", "start date: "+ cStart.getTime().toString());
//                    Log.e("test", "end date: "+ cEnd.getTime().toString());


                    //Date current = cal.getTime();
                    if (cal.after(cStart) && cal.before((cEnd))) {
                        conditionResults.add(true);
                    }

                    else {
                        conditionResults.add(false);
                    }


                    break;

                case TIME:

                    // compare current time against triggered time
                    Calendar mCurrentTime = Calendar.getInstance();
                    Calendar mSavedTime = Calendar.getInstance();

                    mSavedTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(cond.getSetting("hour")));
                    mSavedTime.set(Calendar.MINUTE, Integer.parseInt(cond.getSetting("minute")));

                    /**
                     * comparison with range included
                     *
                     * current--3min <= saved-3min &&
                     * current+3min >= saved+3min
                     */
                    long range = 3 * 60 * 1000; // 3 minute
                    long difference = Math.abs(mCurrentTime.getTimeInMillis() - mSavedTime.getTimeInMillis());

                    //System.out.println("*** difference between two times (ms): "+ difference);
                    //System.out.println("*** diff: "+ difference +", range: "+ range);


                    /**
                     * compare EXACT hour and minute of saved vs current time.
                     */
                    if (mCurrentTime.get(Calendar.HOUR_OF_DAY) == mSavedTime.get(Calendar.HOUR_OF_DAY) &&
                            mCurrentTime.get(Calendar.MINUTE) == mSavedTime.get(Calendar.MINUTE)
                            ) {
                        Log.d("sfen", "Current time is the same as saved time.");
                        conditionResults.add(true);

                    }

                    /**
                     * compare if current time is IN RANGE of saved time.
                     * if so, return condition as TRUE.
                     */
                    if (difference <= range) {
                        Log.d("sfen", "Current time is in range of saved time.");
                        conditionResults.add(true);
                    }


                    else
                        conditionResults.add(false);




                    break;

                case LOCATION_ENTER:
                    //System.out.println("entering location checker");

                    // do we have triggered geofences AND is triggered transition ENTER?
                    if (mTriggeredGeofences != null && mTriggeredGeoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                        for (Geofence single : mTriggeredGeofences) {
                            Log.d("sfen", "req id from triggered geofence: "+ single.getRequestId() +"; current event/condition hashcode = "+ hashCode);

                            // if the triggered geoface has the same hashid than current event, we have a match!
                            if (single.getRequestId().equals(hashCode)) {
                                conditionResults.add(true);
                            }
                        }
                    }
                    // if triggered geofences are empty, result is false
                    else {
                        // not yet...
                        // result will be false ONLY if distance between CURRENTLOC LatLng
                        // and SAVED LatLng is greater than radius set.
                        AndroidLocation loc;
                        loc = new AndroidLocation(context);

                        if (loc.isError())
                            conditionResults.add(false);
                        else {

                            // is current > saved?
                            float[] results = new float[1];
                            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                    Double.parseDouble(cond.getSetting("latitude")),
                                    Double.parseDouble(cond.getSetting("longitude")), results);

                            System.out.println("distance between current location and "+ cond.getDescription() +": "+ results[0] +" in meters.");
                            if (results[0] < Float.parseFloat(cond.getSetting("radius"))) {
                                conditionResults.add(true);
                            }
                            else {
                                conditionResults.add(false);
                            }

                        }
                    }

                    break;


                case LOCATION_LEAVE:
                    // do we have triggered geofences AND is triggered transition ENTER?
                    if (mTriggeredGeofences != null && mTriggeredGeoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                        for (Geofence single : mTriggeredGeofences) {
                            Log.d("sfen", "req id from triggered geofence: "+ single.getRequestId() +"; current event/condition hashcode = "+ hashCode);

                            // if the triggered geoface has the same hashid than current event, we have a match!
                            if (single.getRequestId().equals(hashCode)) {
                                conditionResults.add(true);
                            }
                        }
                    }
                    // if triggered geofences are empty, result is false
                    else {
                        // not yet...
                        // result will be false ONLY if distance between CURRENTLOC LatLng
                        // and SAVED LatLng is greater than radius set.
                        AndroidLocation loc;
                        loc = new AndroidLocation(context);

                        if (loc.isError())
                            conditionResults.add(false);
                        else {

                            // is current > saved?
                            float[] results = new float[1];
                            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                    Double.parseDouble(cond.getSetting("latitude")),
                                    Double.parseDouble(cond.getSetting("longitude")), results);

                            Log.i("sfen", "distance between current location and "+ cond.getDescription() +": "+ results[0] +" in meters.");
                            if (results[0] > Float.parseFloat(cond.getSetting("radius"))) {
                                conditionResults.add(true);
                            }
                            else {
                                conditionResults.add(false);
                            }

                        }
                    }

                    break;


                case CELL_IN:
                case CELL_OUT:

                    // condition with CELL was called, lets check current CELL ID and saved CELLS
                    // if current matches any of saved, we return true.
                    CellConnectionInfo cellInfo = new CellConnectionInfo(Main.getInstance());

                    if (cellInfo.isError()) {
                        Log.d("sfen", cellInfo.getError());

                        // TODO: think what you want to do in later development stage
                        // if cellinfo gets error, are we returning false?
                        // or false_positive?
                        conditionResults.add(false);
                    }
                    else {
                        // we received cellinfo, find a match now

                        // parse to ArrayList first
                        ArrayList<String> mCells = gson.fromJson(cond.getSetting("selectedcell"),
                                new TypeToken<List<String>>(){}.getType());

                        // current cell is in saved cells, we have a match.
                        // depending if we need CELL_IN or CELL_OUT
                        // current cell is stored
                        if (mCells.contains(cellInfo.getCellId())) {
                            // CELL IN returns true
                            if (cond.getOptionType() == DialogOptions.type.CELL_IN)
                                conditionResults.add(true);
                            // CELL OUT returns false
                            else
                                conditionResults.add(false);
                        }
                        // current cell is NOT stored
                        else {
                            // CELL IN returns FALSE
                            if (cond.getOptionType() == DialogOptions.type.CELL_IN)
                                conditionResults.add(false);
                            // CELL OUT returns TRUE
                            else
                                conditionResults.add(true);
                        }

                    }

                    break;


                case EVENT_RUNNING:
                case EVENT_NOTRUNNING:

                    // check if specified event has boolean status of running set to true/false

                    // retrieve event ID's from settings
                    ArrayList<Integer> mEventsToCheck = gson.fromJson(cond.getSetting("selectevents"),
                            new TypeToken<List<Integer>>(){}.getType());


                    // loop through all events,
                    // find the one that is included in settings,
                    // return true/false
                    //int mCurrentID = cond.getUniqueID();
                    boolean mFound = false;
                    for (Event single : Main.getInstance().events) {

                        // match was found
                        if (mEventsToCheck.contains(single.getUniqueID())) {
                            mFound = true;

                            // EVENT_RUNNING
                            if (cond.getOptionType() == DialogOptions.type.EVENT_RUNNING) {
                                if (single.isRunning())
                                    conditionResults.add(true);
                                else
                                    conditionResults.add(false);
                            }

                            // EVENT_NOTRUNNING
                            else if (cond.getOptionType() == DialogOptions.type.EVENT_NOTRUNNING) {
                                if (!single.isRunning())
                                    conditionResults.add(true);
                                else
                                    conditionResults.add(false);
                            }

                            // since we found one, we can break the loop
                            break;


                        }
                    }

                    // if we didn't find anything, we return false
                    if (!mFound)
                        conditionResults.add(false);


                    break;

                default:
                    Log.e("sfen", "No case match ("+ cond.getOptionType() +" in areEventConditionsMet). Returning false.");

                    conditionResults.add(false);

                    break;

            }

        }

        // if we matching all conditions, find if any returned false
        if (e.isMatchAllConditions()) {
            // nope, one returned false, so return false.
            if (conditionResults.indexOf(false) != -1) {
                ret = false;
            }
            else
                ret = true;
        }
        // we need to match at least one condition
        else {
            // got at least one true result?
            if (conditionResults.indexOf(true) != -1) {
                ret = true;
            }
            else
                ret = false;
        }

        // if all conditions are false, that means the Event is NOT running!
        // switch event running boolean to false.
        if (conditionResults.indexOf(true) == -1) {
            if (e.isRunning()) {
                Log.d("sfen", "Turning off " + e.getName());
                e.setRunning(false);

                // we have updated even though we are returning false
                isOneStopping = true;
            }
        }

        // if we have to
        Log.d("sfen", "match all? "+ e.isMatchAllConditions() +" (results from conditions: "+
                conditionResults.toString() +"); RETURN RESULT: "+ ret);
        //System.out.println(conditionResults.toString());
        return ret;

    }

    /**
     * RUN ACTIONS
     * @param context
     * @param intent
     * @param e
     */
    private void runEventActions(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        WifiManager wifiManager;
        ConnectivityManager conMan;

        // if event is already running
        // don't re-run actions
        if (
                (e.isRunning() && !e.isForceRun()) ||
                (e.isRunning() && e.isForceRun()) ||
                (!e.isEnabled() && !e.isForceRun())
                ) {
            Log.e("sfen", e.getName() +" is already running. Skipping actions.");
            return ;
        }

        ArrayList<DialogOptions> actions = e.getActions();
        // loop through all actions and run them
        for (DialogOptions act : actions) {
            Log.i("sfen", "action: "+ act.getTitle() +"("+ act.getOptionType() +")");

            switch (act.getOptionType()) {

                // popup notification!
                case ACT_NOTIFICATION:
                    mUtil.showNotification(sInstance, "Sfen - "+ e.getName(), e.getName(), R.drawable.ic_launcher);

                    break;

                // enable or disable wifi connection
                case ACT_WIFIENABLE:

                    wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

                    if (!wifiManager.isWifiEnabled())
                        wifiManager.setWifiEnabled(true);

                    break;

                case ACT_WIFIDISABLE:

                    wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

                    if (wifiManager.isWifiEnabled())
                        wifiManager.setWifiEnabled(false);

                    break;

                // enable or disable data connection
                // could result in exceptions if user doesn't have root privileges
                case ACT_MOBILEENABLE:

                    conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    //NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();

                    // if not connected yet, meaning enable it now!
                    //if (conMan.getNetworkInfo(0).getState() != NetworkInfo.State.CONNECTED) {

                        // continue only if we have root
                        mUtil.callRootCommand("svc data enable");

                    //}


                    break;

                case ACT_MOBILEDISABLE:

                    // opposite of ACT_MOBILEENABLE
                    conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    //NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
//Util.showMessageBox("mobile state: "+ conMan.getNetworkInfo(0).getState(), false);
                    // if connected, disable data.
                    //if (conMan.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED) {
                        // TODO: ADD root options! (mobile data toggle!) su -c "svc data disable"
                        // continue only if we have root
                        mUtil.callRootCommand("svc data disable");

                    //}

                    break;

                case ACT_VIBRATE:

                    Vibrator v = (Vibrator) sInstance.getSystemService(Context.VIBRATOR_SERVICE);

                    // if our device doesn't have a vibrator, why continue?
                    if (!v.hasVibrator()) {
                        Log.e("sfen", "Phone doesn't have a vibrator.");
                        break;
                    }


                    // retrieve vibration type
                    String vibType = act.getSetting("vibrationtype");

                    // create pattern for vibration depending on vibration type
                    // parameters for vibPattern
                    // delay on start,
                    // milisec vibrate
                    // sleep for
                    long[] vibPattern = {0, 100, 100};
                    if (vibType.equals("Short")) {
                        vibPattern = new long[]{0, 100, 100};
                    }
                    else if (vibType.equals("Medium")) {
                        vibPattern = new long[]{0, 300, 100};
                    }
                    else if (vibType.equals("Long")) {
                        vibPattern = new long[]{0, 500, 100};
                    }


                    // The '-1' here means to vibrate once
                    // '0' would make the pattern vibrate indefinitely
                    v.vibrate(vibPattern, -1);


                    break;

                case ACT_DIALOGWITHTEXT:

                    // get text from settings
                    String mText = act.getSetting("text");

                    // replace occurences of strings with real parameters
                    mText = mUtil.replaceTextPatterns(mText);

                    final WindowManager manager =
                            (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                    layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    //layoutParams.flags |= WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
                    //layoutParams.screenBrightness = 0.9f;

                    final View newView = View.inflate(context.getApplicationContext(), R.layout.dialog_windowmanager, null);

                    Button okButton = (Button) newView.findViewById(R.id.wm_button);
                    TextView info = (TextView) newView.findViewById(R.id.wm_text);
                    info.setText(mText);

                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager.removeView(newView);
                        }
                    });

                    manager.addView(newView, layoutParams);

                    // show dialog since we have text and what not
                    /*
                    final AlertDialog.Builder builder = new AlertDialog.Builder(Main.getInstance());

                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setTitle("Sfen!");
                    builder.setMessage(mText);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    builder.show();
                    */


                    break;

                case ACT_OPENAPPLICATION:

                    String packageName = act.getSetting("packagename");

                    PackageManager pm = context.getPackageManager();
                    // open app
                    //Intent appIntent = new Intent(Intent.ACTION_MAIN);
                    //appIntent.setClassName("com.android.settings", "com.android.settings.Settings");
                    Intent appIntent = pm.getLaunchIntentForPackage(packageName);
                    if (appIntent != null)
                        context.startActivity(appIntent);


                    break;

                case ACT_LOCKSCREENDISABLE:

                    Main.getInstance().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                    break;

                case ACT_PLAYSFEN:

                    MediaPlayer mp = MediaPlayer.create(context, R.raw.sfen_sound);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }

                    });
                    mp.start();

                    break;

                case ACT_LOCKSCREENENABLE:

                    //KeyguardManager keyguardManager = (KeyguardManager)getSystemService(sInstance.KEYGUARD_SERVICE);
                    //KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
                    //lock.reenableKeyguard();

                    //DevicePolicyManager mDPM = new DevicePolicyManager();
                    //mDPM.lockNow();

                    //Main.getInstance().getWindow().addFlags(WindowManager.LayoutParams.KEY);

                    break;

                default:

                    break;

            }
        }

        // first time actions are run. now set event to running.
        e.setRunning(true);

        // set hasrun boolean to true also
        e.setHasRun(true);

        // disable force run
        e.setForceRun(false);

    }

    /**
     * Function will create event condition timers
     * or geofaces if needed by specific condition. Easy as pie.
     *
     * @param events Array Of Events (can be only one too)
     */
    protected void updateEventConditionTimers(ArrayList<Event> events) {

        // set internal variables so after we fill arrays of any kind, we start the triggers
        // Geofence ID's are the same as EVENT unique ID + condition unique ID
        List<String> mGeoIds = new ArrayList<String>();
        ArrayList<Geofence> mGeofences = new ArrayList<Geofence>();

        // alarmmanager array, one when adding, one when removing
        ArrayList<Alarm> mAlarmsCreate = new ArrayList<Alarm>();
        ArrayList<Alarm> mAlarmsDelete = new ArrayList<Alarm>();

        for (Event e : events) {

            for (DialogOptions single : e.getConditions()) {

                /**
                 * start specific libraries if condition uses them
                 */
                // GEOFENCES library
                if ((single.getOptionType() == DialogOptions.type.LOCATION_ENTER ||
                        single.getOptionType() == DialogOptions.type.LOCATION_LEAVE) &&
                        geoLocation == null
                        ) {
                    // start GeoLocation class
                    geoLocation = new GeoLocation(sInstance);
                    Log.i("sfen", "Enabling GeoLocation lib. Needed for "+ single.getTitle() +" in "+ e.getName() +"");
                }

                // TELEPHONYMANAGER library
                if ((single.getOptionType() == DialogOptions.type.CELL_IN ||
                        single.getOptionType() == DialogOptions.type.CELL_OUT) &&
                        mPhoneManager == null && mPhoneReceiver == null
                        ) {
                    // start phone listener
                    mPhoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    mPhoneReceiver = new ReceiverPhoneState();

                    mPhoneManager.listen(mPhoneReceiver,
                            //PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                            PhoneStateListener.LISTEN_CELL_LOCATION
                    );
                    Log.i("sfen", "Enabling TelephonyManager lib. Needed for "+ single.getTitle() +" in "+ e.getName() +"");
                }


                // generate hashcode
                String hashCode = e.getUniqueID() +""+ single.getUniqueID();

                switch (single.getOptionType()) {
                    case LOCATION_ENTER:
                    case LOCATION_LEAVE:

                        //Geofence.GEOFENCE_TRANSITION_ENTER    = 1
                        //Geofence.GEOFENCE_TRANSITION_EXIT     = 2
                        //Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT = 3
                        //transition type should ALWAYS be enter & exit!

                        // IF EVENT ENABLED, add geofences
                        if (e.isEnabled()) {
                            // THIS IS CALLED WHEN ADDING/UPDATING
                            Geofence fence = new Geofence.Builder()
                                    .setRequestId(hashCode)
                                            // when entering this geofence
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .setCircularRegion(
                                            Double.parseDouble(single.getSetting("latitude")),
                                            Double.parseDouble(single.getSetting("longitude")),
                                            Float.parseFloat(single.getSetting("radius")) // radius in meters
                                    )
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .build();

                            mGeofences.add(fence);
                            fence = null;

                        }

                        // IF EVENT DISABLED, remove geofences
                        else {
                            // THIS IS CALLED WHEN REMOVING
                            mGeoIds.add(hashCode);

                        }

                        hashCode = "";

                        break;

                    /**
                     * timerange adds alarms to specific condition time sets
                     * one at start and one on end+1min
                     */
                    case TIMERANGE:
                        //System.out.println("*** TIMERANGE");

                        //System.out.println("showing current alarms: "+ single.getAlarms().toString());
                        //System.out.println("showing all active alarms: "+ mActiveAlarms.toString());

                        // check if we're enabling or disabling event.
                        if (e.isEnabled()) {
                            Log.d("sfen", "Creating alarms (2) for condition: "+ single.getTitle() +" of "+ e.getName());

                            // interval for both created alarms will be 24 hours
                            long interval = AlarmManager.INTERVAL_DAY;


                            /*
                            Create starting time and start it on specific time.

                            We are going to repeat this alarm every day on the same time to check
                            if all event conditions are met.
                            */
                            //System.out.println("start time");
                            Calendar timeStart = Calendar.getInstance();
                            timeStart.setTimeInMillis(System.currentTimeMillis());
                            timeStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(single.getSetting("fromHour")));
                            timeStart.set(Calendar.MINUTE, Integer.parseInt(single.getSetting("fromMinute")));
                            timeStart.set(Calendar.SECOND, 0);


                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeStart, interval);
                            mActiveAlarms.add(mAlarm);


                            /*
                            Create ending time. It will trigger the same as starting, but, we will
                            add 1 minute to it, so it triggers when timerange is over.
                             */
                            //System.out.println("end time");
                            Calendar timeEnd = Calendar.getInstance();
                            timeEnd.setTimeInMillis(System.currentTimeMillis());
                            timeEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(single.getSetting("toHour")));
                            timeEnd.set(Calendar.MINUTE, Integer.parseInt(single.getSetting("toMinute")));
                            //timeEnd.add(Calendar.MINUTE, 1);
                            timeEnd.set(Calendar.SECOND, 0);


                            // TIMES CHECK.
                            // if endTime is lower than startTime, it usually means endTime has to
                            // be tomorrow
                            if (timeEnd.before(timeStart))
                                timeEnd.add(Calendar.DATE, 1);



                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeEnd, interval);
                            mActiveAlarms.add(mAlarm);

                        }
                        // disabling event, stop all timers
                        else {
                            Log.d("sfen", "Disabling alarm(s) for condition: " + single.getTitle() +" of "+ e.getName());

                            // remove every single one
                            for (Alarm singleAlarm : mActiveAlarms) {
                                if (singleAlarm.getConditionID() == single.getUniqueID()) {
                                    // we found a match in active alarms
                                    //mActiveAlarms.remove(singleAlarm);
                                    mAlarmsDelete.add(singleAlarm);
                                }

                            }
                        }

                        break;

                    /**
                     * TIME
                     */
                    case TIME:

                        // if enabling event, start single alarm
                        if (e.isEnabled()) {
                            Log.d("sfen", "Creating alarms for condition: "+ single.getTitle() +" of "+ e.getName());

                            // interval for timer
                            long interval = AlarmManager.INTERVAL_DAY;

                            Calendar timeStart = Calendar.getInstance();
                            timeStart.setTimeInMillis(System.currentTimeMillis());
                            timeStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(single.getSetting("hour")));
                            timeStart.set(Calendar.MINUTE, Integer.parseInt(single.getSetting("minute")));
                            timeStart.set(Calendar.SECOND, 0);

                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeStart, interval);
                            mActiveAlarms.add(mAlarm);

                            // second alarm should trigger 10 minutes after timestart to recheck
                            // why? because, it is possible alarm wont start on correct minute
                            // that's why i've implemented
                            timeStart.add(Calendar.MINUTE, 10);

                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeStart, interval);
                            mActiveAlarms.add(mAlarm);

                        }

                        // if disabling event, delete single alarm
                        else {

                            // remove alarm
                            for (Alarm singleAlarm : mActiveAlarms) {
                                if (singleAlarm.getConditionID() == single.getUniqueID()) {
                                    // we found a match in active alarms
                                    //mActiveAlarms.remove(singleAlarm);
                                    mAlarmsDelete.add(singleAlarm);
                                }

                            }

                        }


                        break;

                    /**
                     * DAYSOFWEEK
                     */
                    case DAYSOFWEEK:

                        // if enabling event, start single alarm
                        if (e.isEnabled()) {
                            Log.d("sfen", "Creating alarms for condition: "+ single.getTitle() +" of "+ e.getName());

                            //System.out.println("*** SETTINGS for condition are:");
                            //System.out.println(single.getSettings().toString());

                            // no matter what days we picked, we only need 1 recurring alarm and repeat
                            // it every day, one second after midnight.
                            if (single.getSetting("selectedDays") != null) {

                                long interval = AlarmManager.INTERVAL_DAY;

                                Calendar timeStart = Calendar.getInstance();
                                timeStart.setTimeInMillis(System.currentTimeMillis());
                                timeStart.set(Calendar.HOUR_OF_DAY, 0);
                                timeStart.set(Calendar.MINUTE, 1);
                                timeStart.set(Calendar.SECOND, 0);

                                mAlarm = new Alarm(sInstance, single.getUniqueID());
                                mAlarm.CreateAlarmRepeating(timeStart, interval);
                                mActiveAlarms.add(mAlarm);


                            }


                        }
                        // if disabling event, delete single alarm
                        else {

                            // remove alarm
                            for (Alarm singleAlarm : mActiveAlarms) {
                                if (singleAlarm.getConditionID() == single.getUniqueID()) {
                                    // we found a match in active alarms
                                    //mActiveAlarms.remove(singleAlarm);
                                    mAlarmsDelete.add(singleAlarm);
                                }

                            }

                        }


                        break;

                    default:
                        Log.d("sfen", "No case match ("+ single.getOptionType() +" in updateEventConditionTimers).");

                        break;

                }
            }

            /**
             * checking if we need extra prerequisites for ACTIONS
             */
            for (DialogOptions single : e.getConditions()) {

                // ROOT?
                // ACT_MOBILEENABLE, ACT_MOBILEDISABLE
                if ((single.getOptionType() == DialogOptions.type.ACT_MOBILEENABLE ||
                        single.getOptionType() == DialogOptions.type.ACT_MOBILEDISABLE) &&
                        geoLocation == null
                        ) {
                    // start GeoLocation class
                    geoLocation = new GeoLocation(sInstance);
                    Log.i("sfen", "Enabling Root mode. Needed for "+ single.getTitle() +" in "+ e.getName() +"");

                    mUtil.callRootCommand("");
                }


            }



            // disable event and set it to running=off
            e.setForceRun(false);
        }

        // after checked all conditions of events, run triggers

        // TRIGGERS FOR ALL EVENTS

        // REMOVING FENCES
        if (mGeoIds.size() > 0) {
            // destroy specific ID's
            geoLocation.RemoveGeofencesById(mGeoIds);
        }

        // ADDING FENCES
        else if (mGeofences.size() > 0) {
            geoLocation.AddGeofences(mGeofences);
        }

        // TIMERS
        if (mAlarmsDelete.size() > 0) {
            //System.out.println("*** deleting "+ mAlarmsDelete.size() +" alarms.");
            for (Alarm single : mAlarmsDelete) {
                Log.d("sfen", "Deleting alarm from condition "+ single.getConditionID());
                // stop the alarm
                single.RemoveAlarm();

                // remove it from array of active alarms
                mActiveAlarms.remove(single);

            }

            mAlarmsDelete = null;

            // save new alarms to preferences
            //mPreferences.setPreferences("alarms", mActiveAlarms);
        }


    }






}
