package com.example.gregor.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

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

        //intentFilter.addAction(getClass().getPackage().getName() +".EVENT_ENABLED");
        //intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        registerReceiver(receiver, intentFilter);

        // check, for the first time of our app history, if we have a candidate..
        EventFinder(this, intent);



        // run Events Checker every X seconds to see, if any of our events is ready to be run
        //AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, );
        // Start every 30 seconds
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);

        // output every 3 seconds
        /*
        try {
            for (int i = 0; i < 50; i++) {
                System.out.println("output: "+ i);
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */


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
            }
        }

        // if we have main activity window open, refresh them
        // TODO: only refresh if we noticed a change
        if (Main.getInstance().isVisible) {
            Main.getInstance().refreshEventsView();
        }

        // if there's no events running OR events stopping, clear notification
        if (!isOneRunning || isOneStopping) {
            System.out.println("no events running.");
            Util.showNotification(sInstance, getString(R.string.app_name), "", R.drawable.ic_launcher);
            isOneRunning = false;
            isOneStopping = false;
        }

    }

    private boolean areEventConditionsMet(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        boolean ret = false;
        final String action = intent.getAction();

        ArrayList<DialogOptions> conditions = e.getConditions();

        // array with booleans for all conditions
        ArrayList<Boolean> conditionResults = new ArrayList<Boolean>();

        for (DialogOptions cond : conditions) {
System.out.println("Event "+ e.getName() +": checking condition "+ cond.getTitle());

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

}
