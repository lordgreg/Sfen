package com.example.gregor.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

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

    // list of possible Actions in Options
    //context.getResources().getDrawable(R.drawable.ic_launcher)
    static final ArrayList<DialogOptions> optActions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions("Show notification", "Will show notification in notification area", android.R.drawable.ic_dialog_info, DialogOptions.type.ACT_NOTIFICATION));
        //add(new DialogOptions("Show dialog with text", "Dialog window with specific text will be shown", android.R.drawable.ic_dialog_alert, DialogOptions.type.ACT_DIALOGWITHTEXT));
        //add(new DialogOptions("Play sound", "Play specific sound", android.R.drawable.ic_dialog_alert, DialogOptions.type.ACT_PLAYSOUND));
        //add(new DialogOptions("Open application", "Opens specified application", android.R.drawable.ic_dialog_alert, DialogOptions.type.ACT_OPENAPPLICATION));
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
                    Util.openDialog(sInstance, optConditions, "Pick condition");
            }
        });

        mContainerCondition.addView(newView, 0);

        // ACTION
        final ViewGroup newAction = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerAction, false);

        ((TextView) newAction.findViewById(android.R.id.text1)).setText(getString(R.string.action_new));
        ((TextView) newAction.findViewById(android.R.id.text2)).setText(getString(R.string.action_new_sub));

        // LISTENER for NEW ACTION
        newAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                Util.openDialog(sInstance, optActions, "Pick action");
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

            getActionBar().setTitle("Editing "+ event.getName());
            //getActionBar().

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

        if (id == android.R.id.home ||
            id == R.id.action_cancel) {
/*
            Log.e("changed", "updating: "+ isUpdating +", changed: "+ isChanged);
            // if we changed activity and we want to cancel it now,
            // we should probably ask user if he's sure, right?
            if (isChanged) {
                Util.showYesNoDialog(sInstance, "You've made changes to event. Cancel anyway?",
                        new HashMap<String, String>());
                boolean dialogBool = Boolean.getBoolean(Main.getInstance().options.get("showYesNoDialog"));

                Log.e("changed", "dialog bool: "+ dialogBool);
                if (dialogBool) {
                    Main.getInstance().options.put("eventSave", "0");
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
            */
         /*
            if (isChanged) {
                if (saveEvent()) {
                    //Log.e("changed", "it got changed.");
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
*/
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
     * Saving/updating event!
     */
    private boolean saveEvent() {

        // container for condition in dialog
        /*
        if (conditions.size() > 0) {
            for (int i = 0; i < conditions.size(); i++) {
                Log.d("conditions",
                        conditions.get(i).getTitle() +", "+
                        conditions.get(i).getOptionType() +", "+
                        conditions.get(i).getSettings().toString() +", "
                );
            }
        }
        */

        // do we have event name?
        if (((TextView) findViewById(R.id.event_name)).getText().length() == 0) {
            Util.showMessageBox("And you think you can get away without entering Event name?", true);
            return false;
        }


        // if we have 0 conditions, we SHALL NOT PASS!
        if (conditions.size() == 0) {
            Util.showMessageBox(getString(R.string.error_select_condition), true);

            return false;
        }

        // if we have 0 conditions, we SHALL NOT PASS!
        if (actions.size() == 0) {
            Util.showMessageBox("Funny, really. Now, add at least one action, ok?", true);

            return false;
        }


        // if we got to this part, we are good to go and we have add new Event to Events array
        // if we're editing event, just update it, otherwise create new object
        if (isUpdating == false) {
            event = new Event();
        }

        event.setName(((TextView) findViewById(R.id.event_name)).getText().toString());
        event.setConditions(conditions);
        event.setActions(actions);
        event.setEnabled(((Switch) findViewById(R.id.event_enabled)).isChecked());
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


        // at the end, send broadcast, if the event is enabled
        if (event.isEnabled()/* && !event.isRunning()*/) {
            // sending broadcast that we've enabled event
            Main.getInstance().sendBroadcast("EVENT_ENABLED");
        }

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
        ((TextView) findViewById(R.id.event_name)).setText(event.getName());
        ((Switch) findViewById(R.id.event_enabled)).setChecked(event.isEnabled());
        //((Switch) findViewById(R.id.event_allconditions)).setChecked(event.isMatchAllConditions());
        ((CheckBox) findViewById(R.id.event_allconditions)).setChecked(event.isMatchAllConditions());

        // add all conditions to container
        //ArrayList<DialogOptions> tempConditions = event.getConditions();
        //conditions = event.getConditions();
        //Util.addNewCondition(sInstance, conditions.get(0));
        for (DialogOptions cond : event.getConditions()) {
            Util.addNewCondition(sInstance, cond);
        }

        conditions = updatedConditions;

        // also, would be great if we add all actions to container, no?
        ArrayList<DialogOptions> allAct = event.getActions();
        for (DialogOptions act : event.getActions()) {
            Util.addNewAction(sInstance, act);
        }

        actions = updatedActions;

    }


    /**
     * EDIT NAME HANDLER
     *
     * from xml: android:onClick="editName"
     */
    public void onClickEventName(View v) {
        // (ViewGroup) LayoutInflater.from(this).inflate(
        //R.layout.condition_action_header, mContainerCondition, false);
        //ViewGroup promptView = (ViewGroup) LayoutInflater.from(sInstance)
        View promptView = getLayoutInflater().inflate(R.layout.dialog_prompt_text, null);
        final EditText eventName = ((EditText) promptView.findViewById(R.id.prompt_text));

        final AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);

        // updating or new?
        if (isUpdating || isChanged) {
           eventName.setText(((TextView)findViewById(R.id.event_name)).getText());
            //((TextView) findViewById(R.id.event_name)).setText(event.getName());
        }
        else {
            eventName.setText("Enter event name");
        }

        // select all text in edittext
        eventName.setSelectAllOnFocus(true);

        // auto open soft keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(eventName, InputMethodManager.SHOW_IMPLICIT);

        builder
                .setView(promptView)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Pick name")

                //.setView(findViewById())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // just close the dialog if we didn't select the days
                        dialog.dismiss();

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // close dialog
                        dialogInterface.dismiss();

                        if (eventName.getText().length() > 0) {
                            ((TextView) findViewById(R.id.event_name)).setText(eventName.getText());

                            // updating event? changed then.
                            // actually, tell that we've updated anyways
                            //if (isUpdating)
                                isChanged = true;
                        }
                        else {
                            Util.showMessageBox("Event name cannot be empty.", false);
                        }
                    }
                });

        builder.show();

    }

    /**
     * EDIT TOGGLE EVENT
     */
    public void onClickEventEnabled(View v) {
        // get switch id
        Switch s = (Switch) findViewById(R.id.event_enabled);

        //Util.showMessageBox("clicked toggle field: "+ s.isChecked(), false);


        // if we are updating, update our event with proper toggle
        if (isUpdating) {
            event.setEnabled(s.isChecked());

            // we have to force update since the name has changed
            event.setForceRun(true);

            isChanged = true;
        }
    }

    /**
     * EDIT CONDITION MATCH TOGGLE
     */
    public void onClickEventAllConditions(View v) {
        //Switch s = (Switch) findViewById(R.id.event_allconditions);
        CheckBox s = (CheckBox) findViewById(R.id.event_allconditions);

        // if we are updating, update our event with proper toggle
        if (isUpdating) {
            event.setMatchAllConditions(s.isChecked());

            isChanged = true;
        }
    }
}
