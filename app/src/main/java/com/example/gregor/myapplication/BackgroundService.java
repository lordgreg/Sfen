package com.example.gregor.myapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Gregor on 10.7.2014.
 *
 * main background process service; this one
 * loads and runs when we press back or home button
 */
public class BackgroundService extends Service {
    final Receiver receiver = new Receiver();

    private static BackgroundService sInstance = null;

    // list of all allowable broadcasts
    private static ArrayList<String> sBroadcasts = new ArrayList<String>() {{
        add("WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION");
        add(getClass().getPackage().getName() +".EVENT_ENABLED");
    }};


    @Override
    public void onCreate() {
        //Log.d("service", "service running");
        // set singleton instance
        sInstance = this;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "Service created.", Toast.LENGTH_LONG).show();
        //Main.getInstance().setNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);
        Util.showNotification(getString(R.string.app_name), "", R.drawable.ic_launcher);

        // start our receiver
        IntentFilter intentFilter = new IntentFilter();

        // add allowable broadcasts
        for (int i = 0; i < sBroadcasts.size(); i++) {
            //Log.e("adding broad to intent", sBroadcasts.get(i));
            intentFilter.addAction(sBroadcasts.get(i));
        }
        //intentFilter.addAction(getPackageName() +".EVENT_ENABLED");
        //intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
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

        // unregister our reciever
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
     * This function os the reason of our existance! It is this function which is going to check
     * if there is any event that can be triggered by eny broadcast!
     */
    protected void EventFinder(Context context, Intent intent) {
        // loop through all events, check only the enabled ones..
        for (Event e : Main.getInstance().events) {
            if (e.isEnabled() & !e.isRunning()) {
                // if it is still not running, then, we have a candidate to check conditions..
                Log.e("finder", "Conditions met? "+ areEventConditionsMet(context, intent, e));
            }
        }
    }

    private boolean areEventConditionsMet(Context context, Intent intent, Event e) {
        Gson gson = new Gson();
        boolean ret = false;

        ArrayList<DialogOptions> conditions = e.getConditions();
Log.e("event", e.getName() +", condition number: "+ e.getConditions().size());
        for (DialogOptions cond : conditions) {
Log.e("cond", "current condition "+ cond.getTitle());
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
                    if (days.indexOf(currentDay) == -1)
                        return false;

                    // it is, so fix return value for the next condition;
                    else {
                        ret = true;
                    }

                    break;

                case WIFI_CONNECT:

                    // are we even connected?
                    if (!intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true)) {
                        return false;
                    }

                    // ok, we are connected, but is current SSID the one if array of desired?
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //String ssid = wifiInfo.toString();
                    String currentSsid = wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);

                    final ArrayList<String> ssid = gson.fromJson(cond.getSetting("selectedWifi"),
                            new TypeToken<List<String>>(){}.getType());

                    // is it the correct one? is it????
                    if (ssid.indexOf(currentSsid) == -1)
                        return false;
                    else
                        ret = true;


                    //Log.e("test", ssid.toString());

                    break;

                case TIMERANGE:
                    Calendar cal = Calendar.getInstance();
                    Calendar cStart = Calendar.getInstance();
                    Calendar cEnd = Calendar.getInstance();

                    int fromHour = Integer.parseInt(cond.getSetting("fromHour"));
                    int fromMinute = Integer.parseInt(cond.getSetting("fromMinute"));
                    int toHour = Integer.parseInt(cond.getSetting("toHour"));
                    int toMinute = Integer.parseInt(cond.getSetting("toMinute"));
                    int currHour = cal.get(Calendar.HOUR_OF_DAY);
                    int currMinute = cal.get(Calendar.MINUTE);


                    cStart.set(Calendar.HOUR_OF_DAY, fromHour);
                    cStart.set(Calendar.MINUTE, fromMinute);

                    cEnd.set(Calendar.HOUR_OF_DAY, toHour);
                    cEnd.set(Calendar.MINUTE, toMinute);
                    cEnd.add(Calendar.DATE, 1);

                    //cal.add(Calendar.DATE, 1);

                    Date current = cal.getTime();

                    /*
                    try {
                        Date start = new SimpleDateFormat("HH:mm").parse(cond.getSetting("fromHour") +":"+ cond.getSetting("fromMinute"));
                        Date end = new SimpleDateFormat("HH:mm").parse(cond.getSetting("toHour") +":"+ cond.getSetting("toMinute"));
                        Date tmp = new SimpleDateFormat("HH:mm:ss").parse(cal.get(Calendar.HOUR_OF_DAY) +":"+ cal.get(Calendar.MINUTE));

                        cal.setTime(tmp);
                        cal.add(Calendar.DATE, 1);
                        cStart.setTime(start);
                        cEnd.add(Calendar.DATE, 1);
                        cEnd.setTime(end);
                        System.out.println("-------- test2");


                    }
                    catch (ParseException exception) {
                        exception.getStackTrace();
                    }
*/


                    Log.e("test", "current date: "+ current.toString());
                    Log.e("test", "start date: "+ cStart.getTime().toString());
                    Log.e("test", "end date: "+ cEnd.getTime().toString());


                    if (current.after(cStart.getTime()) && current.before(cEnd.getTime())) {
                        System.out.println("--------"+ true);
                        ret = true;
                    }

                    else
                        return false;


/*
                    Log.e("test", "Current time: "+ currHour +":"+ currMinute);
                    Log.e("test", "From "+ fromHour +":"+ fromMinute +" to "+ toHour +":"+ toMinute);
                    // if current time in between selected times?
                    if (
                            // current time has to be >= start time
                            (currHour >= fromHour && currMinute >= fromMinute) &&
                            (currHour <= toHour && currMinute <= toMinute)
                            ) {
                        Log.e("time", "Time is in between two times.");
                        ret = true;
                    }
                    else
                        return false;
*/

                    break;
            }

        }



        return ret;
    }
}
