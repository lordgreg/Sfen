package com.example.gregor.myapplication;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


/**
 * Created by Gregor on 10.7.2014.
 *
 * main background process service; this one
 * loads and runs when we press back or home button
 */
public class BackgroundService extends Service
        implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    final Receiver receiver = new Receiver();
    protected String receiverAction = "";
    private boolean isOneRunning = false;
    private boolean isOneStopping = false;
    protected String mLatestSSID = "";

    // geofence init
    private GeoLocation geoLocation = new GeoLocation();
    private ArrayList<Geofence> geoFences = new ArrayList<Geofence>();
    private LocationClient mLocationClient;
    public enum REQUEST_TYPE {ADD, REMOVE};
    private REQUEST_TYPE mRequestType;
    private PendingIntent mGeoPendingIntent = null;
    private PendingIntent mCurrentIntent = null;
    private boolean mInProgress;


    private static BackgroundService sInstance = null;

    // list of all allowable broadcasts
    private static ArrayList<String> sBroadcasts = new ArrayList<String>() {{
        add(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        add(getClass().getPackage().getName() +".EVENT_ENABLED");
        add(getClass().getPackage().getName() +".EVENT_DISABLED");
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


        // start our receiver
        IntentFilter intentFilter = new IntentFilter();

        // add allowable broadcasts
        for (int i = 0; i < sBroadcasts.size(); i++) {
            //Log.e("adding broad to intent", sBroadcasts.get(i));
            intentFilter.addAction(sBroadcasts.get(i));
        }


        registerReceiver(receiver, intentFilter);

        // check, for the first time of our app history, if we have a candidate..
        EventFinder(this, intent);



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

        // unregister our receiver
        unregisterReceiver(receiver);
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
                // if it is still not running, then, we have a candidate to check conditions..
                if (areEventConditionsMet(context, intent, e)) {

                    isOneRunning = true;

                    // wow. conditions are met! you know what that means?
                    // we trigger actions!
                    runEventActions(context, intent, e);

                    // TODO: store action to log.
                }
                // conditions aren't met; switch event to not running (if maybe they were)
                else {
                    e.setRunning(false);
                }
            }
        }

        // if there's no events running OR events stopping, clear notification
        if (!isOneRunning || isOneStopping) {
            System.out.println("no events running.");
            Util.showNotification(sInstance, getString(R.string.app_name), "", R.drawable.ic_launcher);
        }

        isOneRunning = false;
        isOneStopping = false;

        // if we have main activity window open, refresh them
        // TODO: only refresh if we noticed a change
        if (Main.getInstance().isVisible) {
            Main.getInstance().refreshEventsView();
        }

    }

    private boolean areEventConditionsMet(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        boolean ret = false;
        final String action = intent.getAction();

        ArrayList<DialogOptions> conditions = e.getConditions();

        // array with booleans for all conditions
        ArrayList<Boolean> conditionResults = new ArrayList<Boolean>();
System.out.println("EVENT "+ e.getName());
        for (DialogOptions cond : conditions) {
System.out.println("  '--- checking condition "+ cond.getTitle());

            switch (cond.getOptionType()) {
                case DAYSOFWEEK:

                    if (cond.getSetting("selectedDays") == null) return false;

                    gson = new Gson();
                    final ArrayList<Integer> days = gson.fromJson(cond.getSetting("selectedDays"),
                            new TypeToken<List<Integer>>(){}.getType());

                    // 0=monday, 1=tuesday ... 6=sunday
                    //String[] days = cond.getSetting("selectedDays");
                    Calendar calendar = Calendar.getInstance();
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2; // start from 0

                    // is current day in array of selected days?
                    // its not, so break the loop and return false
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
                        System.out.println("we aren't connected to any wifi. latest remembered ssid was "+ mLatestSSID);

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
                    Calendar cal = Calendar.getInstance();
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

                    //Log.e("test", "current date: "+ current.toString());
                    //Log.e("test", "start date: "+ cStart.getTime().toString());
                    //Log.e("test", "end date: "+ cEnd.getTime().toString());


                    //Date current = cal.getTime();
                    if (cal.after(cStart) && cal.before((cEnd))) {
                        conditionResults.add(true);
                    }

                    else {
                        conditionResults.add(false);
                    }


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
                System.out.println("Turning off " + e.getName());
                e.setRunning(false);

                // we have updated even though we are returning false
                isOneStopping = true;
            }
        }

        // if we have to
        System.out.println("match all? "+ e.isMatchAllConditions() +" (results from conditions: "+
                conditionResults.toString() +"); RETURN RESULT: "+ ret);
        //System.out.println(conditionResults.toString());
        return ret;

    }


    private void runEventActions(Context context, Intent intent, Event e) {
        Gson gson = new Gson();

        // if even is already running and this isn't first run of app,
        // don't re-run actions
        if (e.isRunning() && !e.isForceRun()) {
            System.out.println(e.getName() +" is already running. Skipping actions.");
            return ;
        }

        ArrayList<DialogOptions> actions = e.getActions();
        // loop through all actions and run them
        for (DialogOptions act : actions) {
            System.out.println("action: "+ act.getTitle() +"("+ act.getOptionType() +")");

            switch (act.getOptionType()) {

                // popup notification!
                case ACT_NOTIFICATION:
                    Util.showNotification(sInstance, "Sfen - "+ e.getName(), e.getName(), R.drawable.ic_launcher);

                    break;

                default:

                    break;

            }
        }

        // first time actions are run. now set event to running.
        e.setRunning(true);

        // disable force run
        e.setForceRun(false);

    }

    /**
     * Function will create event condition timers
     * or geofaces if needed by specific condition. Easy as pie.
     *
     * @param e Single Event
     */
    protected void updateEventConditionTimers(Event e) {


        for (DialogOptions single : e.getConditions()) {
            System.out.println(e.getName() +" >>> "+ single.getTitle());

            switch (single.getOptionType()) {
                    case LOCATION_ENTER:
                    case LOCATION_LEAVE:
                        System.out.println(single.getSettings().toString());

                        // check if we have google play services installed
                        if (!Util.hasGooglePlayServices()) {
                            break;
                        }

                        // in progress? not yet.
                        //mInProgress = false;

                        // request type? add.
                        mRequestType = REQUEST_TYPE.ADD;

                        // create/update geofaces!
                        Geofence fence = new Geofence.Builder()
                                .setRequestId(e.getName())
                                // when entering this geofence
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                .setCircularRegion(
                                        Double.parseDouble(single.getSetting("latitude")),
                                        Double.parseDouble(single.getSetting("longitude")),
                                        (float)500 // raidus in meters
                                )
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .build();

                        System.out.println("Current size of all running fences: "+ geoFences.size());

                        // if there is one already, remove it from array
                        if (geoFences.indexOf(fence) != -1) {
                            // also check if fence is up, if so, remove it
                            mRequestType = REQUEST_TYPE.REMOVE;
                            mCurrentIntent = getRequestPendingIntent();
                            geoFences.remove(geoFences.indexOf(fence));
                            //mLocationClient = new LocationClient(sInstance, sInstance, sInstance);
                            //mLocationClient.connect();
                            //mLocationClient.removeGeofences(mCurrentIntent, this);
                            System.out.println("*** Preparing to remove intent");
                        }

                        // add fence to array
                        else {
                            geoFences.add(fence);
                        }


                        // starting new geo
                        mLocationClient = new LocationClient(sInstance, sInstance, sInstance);
                        mLocationClient.connect();

                        break;



            }
        }
    }


    /**
     * GEOFENCES NEEDED FUNCTIONS
     */
    @Override
    public void onConnected(Bundle bundle) {

        System.out.println("*** CONNECTED");

        if (mRequestType == REQUEST_TYPE.ADD) {
            System.out.println("*** ADDING NEW GEOFENCE");
            mGeoPendingIntent = getTransitionPendingIntent();

            mLocationClient.addGeofences(geoFences, mGeoPendingIntent, this);
        }

        else {
            System.out.println("*** REMOVING EXISTING GEOFENCE");
            //mLocationClient.removeGeofences(mCurrentIntent, this);
            mLocationClient.removeGeofences(mCurrentIntent, this);
            //mLocationClient.remove

        }



    }

    @Override
    public void onDisconnected() {
        System.out.println("*** DISCONNECTED");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("*** Connection failed.");
    }

    /*
     * Provide the implementation of
     * OnAddGeofencesResultListener.onAddGeofencesResult.
     * Handle the result of adding the geofences
     *
     */
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        System.out.println("*** RESULT: "+ statusCode);
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            System.out.println("new geofence added.");
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        }

        // location access unavailable?
        else if (statusCode == LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            Util.showMessageBox("Geofence service is not available now. Typically this is because " +
                    "the user turned off location access in settings > location access ("+
                    statusCode +")", true);

        }

        // other random error
        else {
            // If adding the geofences failed
            Util.showMessageBox("Error creating new Location listener ("+ statusCode +").", false);

            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        //mInProgress = false;
        mLocationClient.disconnect();
    }

    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    private PendingIntent getTransitionPendingIntent() {
        if (mGeoPendingIntent != null) {
            return mGeoPendingIntent;
        }

        else {

            // Create an explicit Intent
            Intent intent = new Intent(this,
                    ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
            return PendingIntent.getService(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public PendingIntent getRequestPendingIntent() {
        return getTransitionPendingIntent();
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode,
                                                       PendingIntent requestIntent) {
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            System.out.println("*** Removal of fence successful.");
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            System.out.println("*** Error when trying to remove fence.");
            // If adding the geocodes failed

        }
        /*
         * Disconnect the location client regardless of the
         * request status, and indicate that a request is no
         * longer in progress
         */
        //mInProgress = false;
        mLocationClient.disconnect();
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(
            int statusCode, String[] geofenceRequestIds) {
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            System.out.println("*** removing done");
            /*
             * Handle successful removal of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
            // If removing the geofences failed
            System.out.println("*** removing failed.");
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Indicate that a request is no longer in progress
        //mInProgress = false;
        // Disconnect the location client
        mLocationClient.disconnect();
    }




}
