package gpapez.sfen;

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
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

public class EventActivity extends Activity {
    private static EventActivity sInstance = null;
    protected ViewGroup mContainerCondition;
    protected ViewGroup mContainerProfile;

    // containers if we're editing activity
    protected boolean isUpdating = false;
    protected boolean isChanged = false;
    protected int updateKey = -1;
    protected ArrayList<DialogOptions> updatedConditions;


    // placeholder for current Event
    protected Event event = null;

    // event profile
    protected Profile profile = null;

    // arrays for conditions and actions
    protected ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();


    // list of possible Conditions in Options
    static final ArrayList<DialogOptions> optConditions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions("Inside Location", "Inside location", R.drawable.ic_map, DialogOptions.type.LOCATION_ENTER));
        add(new DialogOptions("Outside Location", "Outside location", R.drawable.ic_map, DialogOptions.type.LOCATION_LEAVE));
        add(new DialogOptions("Time Range", "Time range", R.drawable.ic_time, DialogOptions.type.TIMERANGE));
        add(new DialogOptions("Specific Time", "Specific Time", R.drawable.ic_time, DialogOptions.type.TIME));
        add(new DialogOptions("Days", "Day(s) of week", R.drawable.ic_date, DialogOptions.type.DAYSOFWEEK));
        add(new DialogOptions("Connected to Wifi", "Connected to Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_CONNECT));
        add(new DialogOptions("Disconnected from Wifi", "Disconnected from Wifi", R.drawable.ic_wifi, DialogOptions.type.WIFI_DISCONNECT));
        add(new DialogOptions("Screen On", "If screen is on", R.drawable.ic_screen, DialogOptions.type.SCREEN_ON));
        add(new DialogOptions("Screen Off", "If screen is off", R.drawable.ic_screen, DialogOptions.type.SCREEN_OFF));
        add(new DialogOptions("Connected to Cells", "When connected to specific Cell ID's", R.drawable.ic_cell, DialogOptions.type.CELL_IN));
        add(new DialogOptions("Not connected to Cells", "When not connected to specific Cell ID's", R.drawable.ic_cell, DialogOptions.type.CELL_OUT));
        add(new DialogOptions("Event running", "Another Event currently running", R.drawable.ic_launcher, DialogOptions.type.EVENT_RUNNING));
        add(new DialogOptions("Event not running", "Another Event currently not running", R.drawable.ic_launcher, DialogOptions.type.EVENT_NOTRUNNING));
        add(new DialogOptions("GPS enabled", "If GPS is enabled", R.drawable.ic_map, DialogOptions.type.GPS_ENABLED));
        add(new DialogOptions("GPS disabled", "If GPS is disabled", R.drawable.ic_map, DialogOptions.type.GPS_DISABLED));
        add(new DialogOptions("Battery level", "Selected battery level", R.drawable.ic_battery, DialogOptions.type.BATTERY_LEVEL));
        add(new DialogOptions("Battery status", "Status of battery", R.drawable.ic_battery, DialogOptions.type.BATTERY_STATUS));

    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // set singleton instance
        sInstance = this;

        mContainerCondition = (ViewGroup) findViewById(R.id.condition_container);
        mContainerProfile = (ViewGroup) findViewById(R.id.profile_container);

        // CONDITION
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.condition_action_header, mContainerCondition, false);

        ((TextView) newView.findViewById(android.R.id.text1)).setText(getString(R.string.condition_new));
        ((TextView) newView.findViewById(android.R.id.text2)).setText(getString(R.string.condition_new_sub));

        // LISTENER for NEW CONDITION
        newView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    BackgroundService.getInstance().mUtil.openDialog(sInstance, optConditions, "Pick condition");
            }
        });

        mContainerCondition.addView(newView, 0);


        // stop! hammertime!
        // lets check if we got any event passed to us!
        if (getIntent().getStringExtra("sEvent") != null) {
            isUpdating = true;

            event = (new Gson()).fromJson(getIntent().getExtras().getString("sEvent"), Event.class);
            updateKey = getIntent().getIntExtra("sEventIndexKey", -1);
            updatedConditions = new ArrayList<DialogOptions>();
            profile = event.getProfile();

            getActionBar().setTitle("Editing "+ event.getName());
            //getActionBar().

            //Log.e("EVENT FROM OBJ", event.getName() + " with " + event.getConditions().size() + " conditions- key from all events: " + updateKey);
            refreshView();
        }


        /**
         * CREATE NEW PROFILE view if we're not updating Event
         */
        if (!isUpdating) {

            // PROFILE
            final ViewGroup newProfile = (ViewGroup) LayoutInflater.from(this).inflate(
                    R.layout.dialog_pick_single, mContainerProfile, false);

            ((TextView) newProfile.findViewById(android.R.id.text1)).setText("Profile");
            ((TextView) newProfile.findViewById(android.R.id.text2)).setText("Click here to select profile");
            ((ImageButton) newProfile.findViewById(R.id.dialog_icon))
                    .setImageDrawable(getResources().getDrawable(R.drawable.ic_profile));


            // LISTENER for NEW PROFILE
            newProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                    //BackgroundService.getInstance().mUtil.openDialog(sInstance, optActions, "Pick action");
                    openDialogProfiles();
                }
            });

            mContainerProfile.addView(newProfile, 0);
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

            Main.getInstance().options.put("eventSave", "0");
            finish();
            return true;


        }
        if (id == R.id.action_save) {
            //return saveEvent();
            // if event was successfully saved, check if we have to create alarms
            // geofaces if we have such conditions
            if (saveEvent()) {
                // update conditions for current event
                BackgroundService.getInstance().updateEventConditionTimers(new ArrayList<Event>(){{add(event);}});


                return true;
            }
            else
                return false;
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
//        if (actions.size() == 0) {
//            Util.showMessageBox("Funny, really. Now, add at least one action, ok?", true);
//
//            return false;
//        }

        /**
         * did we select profile?
         */
        if (profile == null) {
            Util.showMessageBox("You must select Event profile!", true);

            return false;
        }


        // if we got to this part, we are good to go and we have add new Event to Events array
        // if we're editing event, just update it, otherwise create new object
        if (!isUpdating) {
            event = new Event();
        }

        event.setName(((TextView) findViewById(R.id.event_name)).getText().toString());
        event.setConditions(conditions);
        event.setEnabled(((Switch) findViewById(R.id.event_enabled)).isChecked());
        event.setMatchAllConditions(((CheckBox) findViewById(R.id.event_allconditions)).isChecked());
        event.setRunOnce(((CheckBox) findViewById(R.id.event_runonce)).isChecked());
        event.setProfile(profile);

        // TODO: add one or all settings for current event if needed
        // event.setSetting("this", "test");

        // finally, save/update event to events array
        if (isUpdating) {
            //events.set(events.indexOf(e), e);
            BackgroundService.getInstance().events.set(updateKey, event);
        }
        else {
            BackgroundService.getInstance().events.add(event);
        }

        // lets create option from main activity that we're actually saving event
        //Main.getInstance().options.put("eventSave", "1");
        //Main.getInstance().options.put("eventName", eventName.getText().toString());


        // at the end, send broadcast, if the event is enabled
        if (event.isEnabled()/* && !event.isRunning()*/) {
            // sending broadcast that we've enabled event
            // but because we've updated the event, force refresh
            event.setForceRun(true);
            Main.getInstance().sendBroadcast("EVENT_ENABLED");
        }
        // otherwise, if event went disabled, stop it
        else if (!event.isEnabled()) {
            //event.setForceRun(true);
            event.setRunning(false);
            event.setEnabled(false);
            event.setHasRun(false);
            //event.setRunOnce(false);
            //Util.showNotification(BackgroundService.getInstance(),
            //        getString(R.string.app_name), "", R.drawable.ic_launcher);
            Main.getInstance().sendBroadcast("EVENT_DISABLED");
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
        ((CheckBox) findViewById(R.id.event_runonce)).setChecked(event.isRunOnce());

        // add all conditions to container
        //ArrayList<DialogOptions> tempConditions = event.getConditions();
        //conditions = event.getConditions();
        //Util.addNewCondition(sInstance, conditions.get(0));
        for (DialogOptions cond : event.getConditions()) {
            //Util.addNewCondition(sInstance, cond, 0);
            BackgroundService.getInstance().mUtil.addNewConditionOrAction(sInstance, cond, 0);
        }

        conditions = updatedConditions;

//        // also, would be great if we add all actions to container, no?
//        ArrayList<DialogOptions> allAct = event.getActions();
//        for (DialogOptions act : event.getActions()) {
//            BackgroundService.getInstance().mUtil.addNewConditionOrAction(sInstance, act, 0);
//        }
//
//        actions = updatedActions;


        /**
         * insert saved profile to container
         */
        final ViewGroup newProfile = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.dialog_pick_single, mContainerProfile, false);

        ((TextView) newProfile.findViewById(android.R.id.text1)).setText(event.getProfile().getName());
        ((TextView) newProfile.findViewById(android.R.id.text2)).setText(
                (event.getProfile().isActive() ? "Active" : "Ready")
        );
        ((ImageButton) newProfile.findViewById(R.id.dialog_icon))
                .setImageDrawable(getResources().getDrawable(event.getProfile().getIcon()));


        // LISTENER for NEW PROFILE
        newProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                //BackgroundService.getInstance().mUtil.openDialog(sInstance, optActions, "Pick action");
                openDialogProfiles();
            }
        });

        mContainerProfile.addView(newProfile, 0);


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
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

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

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                        }

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

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
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


    /**
     *
     * OPEN PROFILES DIALOG
     *
     */
    private void openDialogProfiles() {

        /**
         * NO PROFILES???
         */
        if (BackgroundService.getInstance().profiles.size() == 0) {
            Util.showMessageBox("There are no profiles! Go create one now!", true);


            return ;
        }

        /**
         * needed variables
         */
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final LayoutInflater inflater = LayoutInflater.from(this);
        final View dialogView = inflater.inflate(R.layout.dialog_pick_condition, null);
        final ViewGroup mContainerOptions = (ViewGroup) dialogView.findViewById(R.id.condition_pick);
        ViewGroup newRow;

        /**
         * create builder
         */
        builder
                .setView(dialogView)
                .setIcon(this.getResources().getDrawable(R.drawable.ic_launcher))
                .setTitle("Pick Profile")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // canceling main dialog
                        dialog.dismiss();
                    }
                });


        final AlertDialog dialog = builder.create();




        for (final Profile single : BackgroundService.getInstance().profiles) {
        //for (int i = 0; i < BackgroundService.getInstance().profiles.size(); i++) {
            //final DialogOptions opt = BackgroundService.getInstance().profiles.get(i);

            newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, mContainerOptions, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(single.getName());
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                    (single.isActive() ? "Active" : "Ready")
            );

            ((ImageButton) newRow.findViewById(R.id.dialog_icon))
                    .setImageDrawable(sInstance.getResources().getDrawable(single.getIcon()));


            /**
             * add profile to container of dialog
             */
            mContainerOptions.addView(newRow);

            newRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    ((ViewGroup)mContainerOptions.getParent()).removeView(mContainerOptions);
                    //openSubDialog(context, opt, 0);
                    // DO THING HERE!

                    /**
                     * remove current 1st position of container
                     */
                    mContainerProfile.removeViewAt(0);

                    /**
                     * create new viewgroup
                     */
                    final ViewGroup newProfile = (ViewGroup) LayoutInflater.from(sInstance).inflate(
                            R.layout.dialog_pick_single, mContainerProfile, false);

                    ((TextView) newProfile.findViewById(android.R.id.text1)).setText(single.getName());
                    ((TextView) newProfile.findViewById(android.R.id.text2)).setText(
                            single.isActive() ? "Active" : "Ready"
                    );
                    ((ImageButton) newProfile.findViewById(R.id.dialog_icon))
                            .setImageDrawable(getResources().getDrawable(single.getIcon()));


                    // LISTENER for NEW PROFILE
                    newProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                            //BackgroundService.getInstance().mUtil.openDialog(sInstance, optActions, "Pick action");
                            openDialogProfiles();
                        }
                    });

                    /**
                     * add to container
                     */
                    mContainerProfile.addView(newProfile, 0);


                    /**
                     * save to event
                     */
                    profile = single;

                }
            });

            newRow = null;

            dialog.show();
        }

    }
}
