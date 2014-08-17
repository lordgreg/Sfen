package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;

public class Logs extends Activity {
    private static Logs sInstance = null;

    // container for our profiles
    private ViewGroup mContainerView;


    private static final int LOGS_MAX_SIZE = 50;

    private static class Log {
        Calendar time;
        String info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        //final LayoutInflater inflater = LayoutInflater.from(context);
        //final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        mContainerView = (ViewGroup) findViewById(R.id.container_logs);

        // set singleton instance
        sInstance = this;


        // refresh view
        refreshView();
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
        if (id == R.id.action_delete) {

            /**
             * logs size > 0?
             */
            if (getLogsFromPreferences().size() == 0) {

                Util.showMessageBox(getString(R.string.logs_nothing_to_delete), false);
                return false;

            }

            /**
             * ask user if he is sure he wants to delete all items
             */
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage(getString(R.string.logs_delete_confirm))
                    .setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                    .setTitle(getString(R.string.error))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            /**
                             * user confirmed, delete logs
                             */
                            deleteFromPreferences();
                            refreshView();


                        }
                    })

                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            //return;
                        }
                    })
                    .show();




            return true;
        }
        if (id == R.id.action_cancel) {
            finish();
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


    private void refreshView() {

        /**
         * import logs into view
         */
        ArrayList<Log> logsFromPreferences = getLogsFromPreferences();


        /**
         * clear empty textview if logs available
         */
        if (logsFromPreferences.size() > 0) {

            findViewById(android.R.id.empty).setVisibility(View.GONE);

        }
        else {

            findViewById(android.R.id.empty).setVisibility(View.VISIBLE);

        }


        /**
         * remove all views from container
         */
        mContainerView.removeAllViews();


        /**
         * start adding logs from preferences
         */
        // fill the events from array
        for (final Log current : logsFromPreferences) {

            final ViewGroup newRow = (ViewGroup) LayoutInflater.from(Main.getInstance()).inflate(
                    R.layout.dialog_simplerow_single, mContainerView, false);



            ((TextView) newRow.findViewById(android.R.id.text1)).setText(current.info);
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                    Util.getDateLong(current.time, sInstance)
            );

            mContainerView.addView(newRow);




        }


    }


    /**
     *
     * STATIC CALLS
     *
     */


    /**
     * get logs from preferences
     */
    public static ArrayList<Log> getLogsFromPreferences() {

        /**
         * retrieve logs from preferences
         */
        ArrayList<Log> logsFromPreferences;
        logsFromPreferences = (new Gson()).fromJson(
                Preferences.getSharedPreferences().getString("LOGS", "")
                ,
                new TypeToken<ArrayList<Log>>() {
                }.getType());

        if (logsFromPreferences == null)
            logsFromPreferences = new ArrayList<Log>();

        return logsFromPreferences;
    }


    /**
     * adds new entry to our log.
     * @param e
     * @param time
     * @param action
     */
    public static void addToLog(Event e, Calendar time, String action) {

        /**
         * get preferences
         */
        ArrayList<Log> logsFromPreferences = getLogsFromPreferences();


        /**
         * add new item first
         */
        String prepareString = e.getName() +" ("+ action +")";

        Log newLog = new Log();
        newLog.info = prepareString;
        newLog.time = time;

        //logsFromPreferences.put()
        logsFromPreferences.add(0, newLog);

        /**
         * if array has >50 entries, remove the last one
         */
        if (logsFromPreferences.size() > LOGS_MAX_SIZE) {
            System.out.println("new size: "+ logsFromPreferences.size() +", max size: "+ LOGS_MAX_SIZE);
            //logsFromPreferences.remove(logsFromPreferences.size()-1);
            logsFromPreferences.subList(LOGS_MAX_SIZE, logsFromPreferences.size()).clear();

        }


        /**
         * save updated array back to preferences
         */
        Preferences.getSharedPreferences().edit().putString(
                "LOGS",
                new Gson().toJson(logsFromPreferences)).apply();

    }

    /**
     * DELETE ALL logs from preferences
     */
    public static void deleteFromPreferences() {

        Preferences.getSharedPreferences().edit().putString(
                "LOGS",
                new Gson().toJson(new ArrayList<Log>())).apply();


    }



}
