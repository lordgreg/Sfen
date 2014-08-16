package gpapez.sfen;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

public class Logs extends Activity {
    private static Logs sInstance = null;

    private static final int LOGS_MAX_SIZE = 50;

    private static class Log {
        Long time;
        String info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // set singleton instance
        sInstance = this;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static Logs getInstance() {
        if (sInstance == null) {
            return new Logs();
        }
        else
            return sInstance;
    }


    /**
     *
     * STATIC CALLS
     *
     */

    /**
     * adds new entry to our log.
     * @param e
     * @param time
     */
    public static void addToLog(Event e, Calendar time) {

        /**
         * retrieve logs from preferences
         *
         * Hashmap<timeinmiliseconds, string>
         */
        ArrayList<Log> logsFromPreferences = new ArrayList<Log>();
        logsFromPreferences = (new Gson()).fromJson(
                Preferences.getSharedPreferences().getString("LOGS", "")
                ,
                new TypeToken<ArrayList<Log>>() {
                }.getType());


        /**
         * add new item first
         */
        String prepareString = e.getName() +" with "+ e.getActions().size() +" actions and "+
                ((e.getProfile() != null) ?
                "profile "+ e.getProfile().getName() :
                "no profile") +
                " successfully ran.";

        Log newLog = new Log();
        newLog.info = prepareString;
        newLog.time = time.getTimeInMillis();

        //logsFromPreferences.put()
        logsFromPreferences.add(0, newLog);

        /**
         * if array has >50 entries, remove the last one
         */
        if (logsFromPreferences.size() > LOGS_MAX_SIZE) {

            logsFromPreferences.remove(logsFromPreferences.size());

        }


        /**
         * save updated array back to preferences
         */
        Preferences.getSharedPreferences().edit().putString(
                "LOGS",
                new Gson().toJson(logsFromPreferences)).apply();



    }



}
