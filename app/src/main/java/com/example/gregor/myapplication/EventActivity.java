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

import com.google.gson.Gson;

import java.util.ArrayList;

public class EventActivity extends Activity {
    private static EventActivity sInstance = null;
    protected ViewGroup mContainerCondition, mContainerAction;

    // containers if we're editing activity
    protected boolean isUpdating = false;
    protected boolean isChanged = false;
    protected int updateKey = -1;
    protected ArrayList<DialogOptions> updatedConditions;
    protected ArrayList<DialogOptions> updatedActions;


    // placeholder for current Event
    private Event event = null;

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



        // stop! hammertime!
        // lets check if we got any event passed to us!
        if (getIntent().getStringExtra("sEvent") != null) {
            isUpdating = true;

            event = (new Gson()).fromJson(getIntent().getExtras().getString("sEvent"), Event.class);
            updateKey = getIntent().getIntExtra("sEventIndexKey", -1);
            updatedConditions = new ArrayList<DialogOptions>();
            updatedActions = new ArrayList<DialogOptions>();

            //Log.e("EVENT FROM OBJ", event.getName() + " with " + event.getConditions().size() + " conditions- key from all events: " + updateKey);
            refreshView();
        }

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

            // if we changed activity and we want to cancel it now,
            // we should probably ask user if he's sure, right?
            if (isChanged) {
                if (saveEvent()) {
                    Log.e("changed", "it got changed.");
                    Main.getInstance().options.put("eventSave", "1");
                    //finish();
                    return true;
                }
                else
                    return false;
            }

            // nothing changed, just close the activity
            else {
                Main.getInstance().options.put("eventSave", "0");
                finish();
                return true;
            }


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
        if (conditions.size() == 0) {
            Util.showMessageBox(getString(R.string.error_select_condition), true);

            return false;
        }

        Log.e("cond", "cond size: "+ conditions.size());


        // do we have event name?
        if (((TextView) findViewById(R.id.event_name)).getText().length() == 0) {
            Util.showMessageBox("And you think you can get away without entering Event name?", true);
            return false;
        }

        // if we got to this part, we are good to go and we have add new Event to Events array
        // if we're editing event, just update it, otherwise create new object
        if (isUpdating == false) {
            event = new Event();
        }

        event.setName(((TextView) findViewById(R.id.event_name)).getText().toString());
        event.setConditions(conditions);
        // TODO: after generating actions array, fill event with them
        //event.setActions(actions);
        event.setEnabled(true);
        // TODO: add one or all settings for current event if needed
        // event.setSetting("this", "test");

        // finally, save/update event to events array
        if (isUpdating) {
            //events.set(events.indexOf(e), e);
            Main.getInstance().events.set(updateKey, event);
        }
        else {
            Main.getInstance().events.add(event);
        }

        // lets create option from main activity that we're actually saving event
        Main.getInstance().options.put("eventSave", "1");
        //Main.getInstance().options.put("eventName", eventName.getText().toString());


        finish();
        return true;

    }


    /**
     * refreshView is used only if we passed event from other activity
     * and would like to populate entries in eventactivity
     *
     * we have to update name, conditions and actions!
     */
    public void refreshView() {
        // clear containers
        //mContainerCondition.removeAllViews();
        //mContainerAction.removeAllViews();
        //conditions = event.getConditions();

        ((TextView) findViewById(R.id.event_name)).setText(event.getName());

        // add all conditions to container
        //ArrayList<DialogOptions> tempConditions = event.getConditions();
        //conditions = event.getConditions();
        //Util.addNewCondition(sInstance, conditions.get(0));
        for (DialogOptions cond : event.getConditions()) {
            Util.addNewCondition(sInstance, cond);
        }

        conditions = updatedConditions;


    }
}
