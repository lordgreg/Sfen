package gpapez.sfen;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Class for our event which will include
 * name,
 * status (enabled/disabled),
 * array of Conditions
 * array of Actions
 *
 * Created by Gregor on 10.7.2014.
 */
public class Event {
    private String name;
    private boolean enabled;
    private boolean running = false;
    private boolean matchAllConditions = true;
    private boolean forceRun = false;
    private boolean runOnce = false;
    private boolean hasRun = false;
    private int uniqueID = -1;

    //private Profile profile;
    private int profile;


    private ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();
    //private ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();

    private HashMap<String, String> settings;


    public Event() {
        super();
        if (uniqueID == -1) {
            uniqueID = new Random().nextInt(Integer.MAX_VALUE) + 1;
        }
    }


    /***********************************************************************************************
     * Checks Event for all conditions and returns boolean for every condition
     * @param context
     * @param intent
     * @param e
     * @return boolean if event conditions are met.
     ***********************************************************************************************
     */
    protected boolean areEventConditionsMet(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        boolean ret = false;
        final String action = intent.getAction();

        ArrayList<DialogOptions> conditions = e.getConditions();

        // array with booleans for all conditions
        ArrayList<Boolean> conditionResults = new ArrayList<Boolean>();
        Log.d("sfen", "EVENT " + e.getName());



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
                        PowerManager powerManager = (PowerManager)
                                BackgroundService.getInstance().getSystemService(BackgroundService.getInstance().POWER_SERVICE);
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
                        PowerManager powerManager = (PowerManager)
                                BackgroundService.getInstance().getSystemService(BackgroundService.getInstance().POWER_SERVICE);
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
                        if (ssid.indexOf(BackgroundService.getInstance().mLatestSSID) != -1)
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
//
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
                    if (BackgroundService.getInstance().mTriggeredGeofences != null &&
                            BackgroundService.getInstance().mTriggeredGeoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                        for (Geofence single : BackgroundService.getInstance().mTriggeredGeofences) {
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
                    if (BackgroundService.getInstance().mTriggeredGeofences != null &&
                            BackgroundService.getInstance().mTriggeredGeoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                        for (Geofence single : BackgroundService.getInstance().mTriggeredGeofences) {
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
                    for (Event single : BackgroundService.getInstance().events) {

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


                /**
                 * battery status
                 */
                case BATTERY_STATUS:

                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = context.registerReceiver(null, ifilter);

                    // Are we charging / charged?
                    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    //BatteryManager.EX
                    /*boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    // How are we charging?
                    int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                    boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                    */
                    /*

                        public static final int BATTERY_STATUS_CHARGING
                        Constant Value: 2 (0x00000002)

                        public static final int BATTERY_STATUS_DISCHARGING
                        Constant Value: 3 (0x00000003)

                        public static final int BATTERY_STATUS_FULL
                        Constant Value: 5 (0x00000005)

                        public static final int BATTERY_STATUS_NOT_CHARGING
                        Constant Value: 4 (0x00000004)

                        "Charging",
                        "Discharging",
                        "Not Charging",
                        "Full"

                     */
                    if (cond.getSetting("BATTERY_STATUS").equals("Charging") &&
                            status == 2)
                        conditionResults.add(true);

                    else if (cond.getSetting("BATTERY_STATUS").equals("Discharging") &&
                            status == 3)
                        conditionResults.add(true);

                    else if (cond.getSetting("BATTERY_STATUS").equals("Not Charging") &&
                            status == 4)
                        conditionResults.add(true);

                    else if (cond.getSetting("BATTERY_STATUS").equals("Full") &&
                        status == 5)
                        conditionResults.add(true);

                    else
                        conditionResults.add(false);


                    System.out.println("-----------------current battery status: "+ status);

                    break;

                /**
                 * BATTERY PERCENTAGE
                 */
                case BATTERY_LEVEL:

                    ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    batteryStatus = context.registerReceiver(null, ifilter);

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    float batteryPct = level / (float)scale;
//                    System.out.println("current battery level: "+ batteryPct);
//                    System.out.println("level: "+ level +", scale: "+ scale);

                    int saveLevel = Integer.parseInt(cond.getSetting("BATTERY_LEVEL"));

                    /**
                     * return true only in case of same battery level
                     */
                    if (level == saveLevel)
                        conditionResults.add(true);

                    else
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
                BackgroundService.getInstance().isOneStopping = true;
            }
        }

        // if we have to
        Log.d("sfen", "match all? "+ e.isMatchAllConditions() +" (results from conditions: "+
                conditionResults.toString() +"); RETURN RESULT: "+ ret);
        //System.out.println(conditionResults.toString());
        return ret;

    }

    public boolean isMatchAllConditions() {
        return matchAllConditions;
    }

    public boolean isForceRun() {
        return forceRun;
    }

    public void setForceRun(boolean forceRun) {
        this.forceRun = forceRun;
    }

    public void setMatchAllConditions(boolean matchAllConditions) {
        this.matchAllConditions = matchAllConditions;
    }

    public boolean isRunOnce() {
        return runOnce;
    }

    public void setRunOnce(boolean runOnce) {
        this.runOnce = runOnce;
    }

    public boolean isHasRun() {
        return hasRun;
    }

    public void setHasRun(boolean hasRun) {
        this.hasRun = hasRun;
    }


    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<DialogOptions> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<DialogOptions> conditions) {
        this.conditions = conditions;
    }

//    public ArrayList<DialogOptions> getActions() {
//        return actions;
//    }

//    public void setActions(ArrayList<DialogOptions> actions) {
//        this.actions = actions;
//    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        return settings.get(key);
    }

    public void setSettings(HashMap<String, String>settings) {
        this.settings = settings;
    }

    public HashMap<String, String>getSettings() {
        return settings;
    }

    public Profile getProfile() {
        //return profile;
        return Profile.getProfileByUniqueID(profile);

    }

    public int getProfileID() {
        return profile;
    }

    public void setProfileID(int profileID) {
        profile = profileID;
    }

    public void setProfile(Profile profile) {
        //this.profile = profile;
        this.profile = profile.getUniqueID();

    }

    /**
     * returns Event by Unique ID
     */
    public static Event returnEventByUniqueID(int uniqueID) {

        for (Event single : BackgroundService.getInstance().events) {

            if (single.getUniqueID() == uniqueID)
                return single;

        }

        return null;
    }

    /**
     * returns key of events array at which, event with unique id is found
     */
    public static int returnKeyByEventUniqueID(int uniqueID) {

        Event single;

        for (int i = 0; i < BackgroundService.getInstance().events.size(); i++) {

            single = BackgroundService.getInstance().events.get(i);

            if (single.getUniqueID() == uniqueID)
                return i;

        }

        return -1;
    }

    /**
     * resets unique id
     * (used when importing events and profiles)
     */
    public void resetUniqueId() {

        uniqueID = Math.abs(new Random().nextInt());

    }

}
