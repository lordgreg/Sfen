package gpapez.sfen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.example.gregor.myapplication.R;
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

    // alarmmanager
    //protected AlarmManager mAlarmManager;
    protected Alarm mAlarm;
    protected ArrayList<Alarm> mActiveAlarms = new ArrayList<Alarm>();

    // geofence init
    private GeoLocation geoLocation;
    protected List<Geofence> mTriggeredGeofences = new ArrayList<Geofence>();
    protected int mTriggeredGeoFenceTransition = -1;


    private static BackgroundService sInstance = null;
    protected Intent sIntent = null;

    // list of all allowable broadcasts
    private static ArrayList<String> sBroadcasts = new ArrayList<String>() {{
        add(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        add(getClass().getPackage().getName() +".EVENT_ENABLED");
        add(getClass().getPackage().getName() +".EVENT_DISABLED");
        add(getClass().getPackage().getName() +".GEOFENCE_ENTER");
        add(getClass().getPackage().getName() +".GEOFENCE_EXIT");
        add(getClass().getPackage().getName() +".ALARM_TRIGGER");

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
        mActiveAlarms = (ArrayList<Alarm>) mPreferences.getPreferences("alarms", Preferences.REQUEST_TYPE.ALARMS);
        if (mActiveAlarms == null)
            mActiveAlarms = new ArrayList<Alarm>();


        // start our receiver
        IntentFilter intentFilter = new IntentFilter();

        // add allowable broadcasts
        for (int i = 0; i < sBroadcasts.size(); i++) {
            //Log.e("adding broad to intent", sBroadcasts.get(i));
            intentFilter.addAction(sBroadcasts.get(i));
        }
        //intentFilter.addCategory("com.example.gregor.myapplication.CATEGORY_LOCATION_SERVICES");
        registerReceiver(receiver, intentFilter);

        // start GeoLocation class
        geoLocation = new GeoLocation(sInstance);

        // create alarm object
//        mAlarm = new Alarm(sInstance);
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(System.currentTimeMillis());
//        //cal.set(Calendar.HOUR_OF_DAY, 18);
//        //cal.set(Calendar.MINUTE, 32);
//
//        // add 5 seconds to current time and we get when when next trigger happens. yep, in 5seconds o_O
//        cal.add(Calendar.SECOND, 5);
//        //System.out.println("current time to start trigger: "+ cal.toString());
//        mAlarm.CreateAlarm(cal); // this one starts in
//        mActiveAlarms.add(mAlarm);
//
//        // start this one with X seconds from now
//        mAlarm = new Alarm(sInstance);
//        mAlarm.CreateAlarm(5);
//        mActiveAlarms.add(mAlarm);


        // start this one repeating
        //mAlarm = new Alarm(sInstance);
        //mAlarm.CreateAlarmRepeating(cal, 8);

        //PendingIntent mAlarmPi = PendingIntent.getBroadcast(this, 0, new Intent("com.example.gregor.myapplication.ALARM_TRIGGER"), 0);
        //mAlarmManager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
        //mAlarmManager.set(AlarmManager.RTC_WAKEUP, 5000, mAlarmPi);

        // check, for the first time of our app history, if we have a candidate..
        //EventFinder(this, intent);

        // also check for the first time and never again, for the condition triggers
        // refresh condition timers
        if (Main.getInstance().events.size() > 0)
            updateEventConditionTimers(Main.getInstance().events);

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

        // cancel alarm manager
        //mAlarmManager.cancel();

        // destroy all geofences
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
            Log.d("sfen", "no events running.");
            Util.showNotification(sInstance, getString(R.string.app_name), "", R.drawable.ic_launcher);
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

                    Log.e("test", "current date: "+ cal.getTime().toString());
                    Log.e("test", "start date: "+ cStart.getTime().toString());
                    Log.e("test", "end date: "+ cEnd.getTime().toString());


                    //Date current = cal.getTime();
                    if (cal.after(cStart) && cal.before((cEnd))) {
                        conditionResults.add(true);
                    }

                    else {
                        conditionResults.add(false);
                    }


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


                default:
                    Log.e("sfen", "No case match ("+ cond.getOptionType() +"). Returning false.");

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


    private void runEventActions(Context context, Intent intent, Event e) {
        Gson gson = new Gson();

        // if even is already running and this isn't first run of app,
        // don't re-run actions
        if (e.isRunning() && !e.isForceRun()) {
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
                //System.out.println(e.getName() + " >>> " + single.getTitle());

                // generate hashcode
                String hashCode = e.getUniqueID() +""+ single.getUniqueID();

                switch (single.getOptionType()) {
                    case LOCATION_ENTER:
                    case LOCATION_LEAVE:
                        //System.out.println("hash: "+ hashCode +"\n"+ single.getSettings().toString());
                        //System.out.println("HASHCODE: "+ hashCode);

                        //Geofence.GEOFENCE_TRANSITION_ENTER    = 1
                        //Geofence.GEOFENCE_TRANSITION_EXIT     = 2
                        //Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT = 3
                        //transition type should ALWAYS be enter & exit!
//                        int mTransitionType = ((single.getOptionType()== DialogOptions.type.LOCATION_ENTER) ? 1 :
//                                ((single.getOptionType()== DialogOptions.type.LOCATION_LEAVE) ? 2 :
//                                        ((single.getOptionType()== DialogOptions.type.LOCATION_ENTERLEAVE) ? 3 : 0)));
                        //System.out.println("*** transition type: "+ mTransitionType +" cond: "+ single.getOptionType() +", event: "+ e.getName());

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
                        System.out.println("*** TIMERANGE");

                        //System.out.println("showing current alarms: "+ single.getAlarms().toString());
                        //System.out.println("showing all active alarms: "+ mActiveAlarms.toString());

                        // check if we're enabling or disabling event.
                        if (e.isEnabled()) {
                            Log.d("sfen", "Creating alarm for condition: "+ single.getTitle());

                            // interval for both created alarms will be 24 hours
                            int intervalSeconds = 24*60*60;


                            /*
                            Create starting time and start it on specific time.

                            We are going to repeat this alarm every day on the same time to check
                            if all event conditions are met.
                            */
                            System.out.println("start time");
                            Calendar timeStart = Calendar.getInstance();
                            timeStart.setTimeInMillis(System.currentTimeMillis());
                            timeStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(single.getSetting("fromHour")));
                            timeStart.set(Calendar.MINUTE, Integer.parseInt(single.getSetting("fromMinute")));
                            timeStart.set(Calendar.SECOND, 0);

                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeStart, intervalSeconds);
                            mActiveAlarms.add(mAlarm);


                            /*
                            Create ending time. It will trigger the same as starting, but, we will
                            add 1 minute to it, so it triggers when timerange is over.
                             */
                            System.out.println("end time");
                            Calendar timeEnd = Calendar.getInstance();
                            timeEnd.setTimeInMillis(System.currentTimeMillis());
                            timeEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(single.getSetting("toHour")));
                            timeEnd.set(Calendar.MINUTE, Integer.parseInt(single.getSetting("toMinute")));
                            //timeEnd.add(Calendar.MINUTE, 1);
                            timeEnd.set(Calendar.SECOND, 0);

                            mAlarm = new Alarm(sInstance, single.getUniqueID());
                            mAlarm.CreateAlarmRepeating(timeEnd, intervalSeconds);
                            mActiveAlarms.add(mAlarm);

                        }
                        // disabling event, stop all timers
                        else {
                            Log.d("sfen", "Disabling alarm(s) for condition: " + single.getTitle());

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

                    default:
                        Log.d("sfen", "No case match ("+ single.getOptionType() +").");

                        break;

                }
            }

            // disable event and set it to running=off
            //e.setRunning(false);
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
            System.out.println("*** deleting "+ mAlarmsDelete.size() +" alarms.");
            for (Alarm single : mAlarmsDelete) {
                System.out.println("Deleting alarm from condition "+ single.getConditionID());
                // stop the alarm
                single.RemoveAlarm();

                // remove it from array of active alarms
                mActiveAlarms.remove(single);

            }

            mAlarmsDelete = null;

            // save new alarms to preferences
            mPreferences.setPreferences("alarms", mActiveAlarms);
        }


    }






}
