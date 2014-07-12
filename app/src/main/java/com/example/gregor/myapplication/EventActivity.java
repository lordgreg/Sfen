package com.example.gregor.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EventActivity extends Activity {
    private static EventActivity sInstance = null;
    protected ViewGroup mContainerCondition, mContainerAction;
    private TextView eventName;

    // placeholder for current Event
    private Event event;

    // arrays for conditions and actions
    protected ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();
    protected ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();


    // list of possible Conditions in Options
    static final ArrayList<DialogOptions> optConditions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions("Entering Location", "Entering location", R.drawable.ic_map, DialogOptions.type.LOCATION_ENTER));
        add(new DialogOptions("Leaving Location", "Leaving location", R.drawable.ic_map, DialogOptions.type.LOCATION_LEAVE));
        add(new DialogOptions("Time", "Time range", R.drawable.ic_time, DialogOptions.type.TIMERANGE));
        add(new DialogOptions("Days", "Day(s) of week", R.drawable.ic_date, DialogOptions.type.DAYSOFWEEK));
        add(new DialogOptions("Connecting to Wifi", "Connected to Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_CONNECT));
        add(new DialogOptions("Disconnecting from Wifi", "Disconnected from Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_DISCONNECT));
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // set singleton instance
        sInstance = this;

        mContainerCondition = (ViewGroup) findViewById(R.id.condition_container);
        mContainerAction = (ViewGroup) findViewById(R.id.action_container);


        // CONDITION
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerCondition, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(getString(R.string.condition_new));
        ((TextView) newView.findViewById(android.R.id.text2)).setText(getString(R.string.condition_new_sub));

        // LISTENER for NEW CONDITION
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Util.openDialogConditions(sInstance, optConditions);
            }
        });

        mContainerCondition.addView(newView, 0);

        // ACTION
        final ViewGroup newAction = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerCondition, false);

        ((TextView) newAction.findViewById(android.R.id.text1)).setText(getString(R.string.action_new));
        ((TextView) newAction.findViewById(android.R.id.text2)).setText(getString(R.string.action_new_sub));

        // LISTENER for NEW ACTION
        newAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
            }
        });

        mContainerAction.addView(newAction, 0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            Main.getInstance().options.put("eventSave", "0");

            finish();
            return true;
        }
        if (id == R.id.action_save) {
            return saveEvent();
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
    public static EventActivity getInstance() {
        if (sInstance == null) {
            return new EventActivity();
        }
        else
            return sInstance;
    }


    /**
     * Saving event!
     */
    private boolean saveEvent() {

        // container for condition in dialog
        if (conditions.size() > 0) {
            for (int i = 0; i < conditions.size(); i++) {
                Log.d("conditions",
                        conditions.get(i).getTitle() +", "+
                        conditions.get(i).getOptionType() +", "+
                        conditions.get(i).getSettings().toString() +", "
                );
            }
        }
        // if we have 0 conditions, we SHALL NOT PASS!
        else {
            Util.showMessageBox(getString(R.string.error_select_condition), true);

            return  false;
        }


        // do we have event name?
        if (((TextView) findViewById(R.id.event_name)).getText().length() == 0) {
            Util.showMessageBox("And you think you can get away without entering Event name?", true);
            return false;
        }


        // before saving, we have to ensure we have at least one condition and one activity.
        // if there's only one in ListView, it means its the one from "add new activity".
        // TODO: delete this part if everything else is okay.
        /*
        int num = mContainerCondition.getChildCount();

        if (num <= 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_select_condition))
                    .setIcon(R.drawable.ic_launcher)
                            //.setIcon(android.R.drawable.ic_notification_clear_all)
                    .setTitle(getString(R.string.error))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }
        Log.d("info", "number of dialogOptions: "+ mContainerCondition.getChildCount());

        eventName = (TextView) findViewById(R.id.event_name);

*/

        // if we got to this part, we are good to go and we have add new Event to Events array
        event = new Event();
        event.setName(((TextView) findViewById(R.id.event_name)).getText().toString());
        event.setConditions(conditions);
        // TODO: after generating actions array, fill event with them
        //event.setActions(actions);
        event.setEnabled(true);
        // TODO: add one or all settings for current event if needed
        // event.setSetting("this", "test");

        // finally, save event to events array
        Main.getInstance().events.add(event);

        //Main.getInstance().options.put("eventSave", "1");
        //Main.getInstance().options.put("eventName", eventName.getText().toString());


        finish();
        return true;

    }
}
