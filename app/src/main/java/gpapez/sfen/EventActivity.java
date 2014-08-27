package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

public class EventActivity extends Activity {
    private static EventActivity sInstance = null;
    protected ViewGroup mContainerCondition;
    protected ViewGroup mContainerAction;
    protected ViewGroup mContainerProfile;

    // containers if we're editing activity
    protected boolean isUpdating = false;
    protected boolean isChanged = false;
    protected int updateKey = -1;
    protected ArrayList<DialogOptions> updatedConditions;
    protected ArrayList<DialogOptions> updatedActions;


    // placeholder for current Event
    protected Event event = null;

    // event profile
    protected Profile profile = null;

    // arrays for conditions and actions
    protected ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();
    protected ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();

    // on activity result
    final int REQUEST_PICK_SHORTCUT = 0x100;
    final int REQUEST_CREATE_SHORTCUT = 0x200;
    final int REQUEST_FILEMANAGER_SHORTCUT = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // set singleton instance
        sInstance = this;

        mContainerCondition = (ViewGroup) findViewById(R.id.condition_container);
        mContainerAction = (ViewGroup) findViewById(R.id.action_container);
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
                Util.openDialog(sInstance, DialogOptions.optConditions(sInstance), getString(R.string.pick_condition));
                isChanged = true;
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
                Util.actionFrom = Util.ACTION_FROM.EVENT;
                Util.openDialog(sInstance, DialogOptions.optActions(sInstance), getString(R.string.pick_action));
                isChanged = true;
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
            profile = event.getProfile();

            getActionBar().setTitle(getString(R.string.editing_event, event.getName()));
            //getActionBar().

            //Log.e("EVENT FROM OBJ", event.getName() + " with " + event.getConditions().size() + " conditions- key from all events: " + updateKey);
            refreshView();
        }

        /**
         * NEW EVENT
         */
        else {

            event = new Event();

        }


        /**
         * CREATE NEW PROFILE view if we're not updating Event
         */
        if (!isUpdating || event.getProfile() == null) {

            profile = new Profile();
            profile.setUniqueID(-1);

            // PROFILE
            final ViewGroup newProfile = (ViewGroup) LayoutInflater.from(this).inflate(
                    R.layout.dialog_pick_single, mContainerProfile, false);

            ((TextView) newProfile.findViewById(android.R.id.text1)).setText(getString(R.string.profile));
            ((TextView) newProfile.findViewById(android.R.id.text2)).setText(getString(R.string.profile_select));
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

            onBackPressed();
            return true;


        }
        if (id == R.id.action_save) {
            //return saveEvent();
            // if event was successfully saved, check if we have to create alarms
            // geofaces if we have such conditions
            if (saveEvent()) {
                // update conditions for current event
                BackgroundService.getInstance().updateEventConditionTimers(new ArrayList<Event>() {{
                    add(event);
                }});


                return true;
            } else
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * SINGLETON INSTANCE
     * <p/>
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     *
     * @return instance of current class
     */
    public static EventActivity getInstance() {
        if (sInstance == null) {
            return new EventActivity();
        } else
            return sInstance;
    }


    /**
     * Saving/updating event!
     */
    private boolean saveEvent() {

        // do we have event name?
        if (((TextView) findViewById(R.id.event_name)).getText().length() == 0) {
            Util.showMessageBox(getString(R.string.enter_event_name), true);
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
         * did we select profile OR action?
         */
        if (profile.getUniqueID() == -1 && actions.size() == 0) {
            Util.showMessageBox(getString(R.string.select_profile_or_action), true);

            return false;
        }


        // if we got to this part, we are good to go and we have add new Event to Events array
        // if we're editing event, just update it, otherwise create new object
//        if (!isUpdating) {
//            event = new Event();
//        }

        event.setName(((TextView) findViewById(R.id.event_name)).getText().toString());
        event.setConditions(conditions);
        event.setActions(actions);
        event.setEnabled(((Switch) findViewById(R.id.event_enabled)).isChecked());
        event.setMatchAllConditions(((CheckBox) findViewById(R.id.event_allconditions)).isChecked());
        event.setRunOnce(((CheckBox) findViewById(R.id.event_runonce)).isChecked());

        if (profile == null)
            event.setProfileID(-1);
        else
            event.setProfile(profile);


        // finally, save/update event to events array
        if (isUpdating) {
            //events.set(events.indexOf(e), e);
            BackgroundService.getInstance().events.set(updateKey, event);
        } else {
            BackgroundService.getInstance().events.add(event);
        }


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
     * <p/>
     * we have to update name, conditions and actions!
     */
    public void refreshView() {
        ((TextView) findViewById(R.id.event_name)).setText(event.getName());
        ((TextView) findViewById(R.id.priority)).setText(event.getPriorityString());
        ((Switch) findViewById(R.id.event_enabled)).setChecked(event.isEnabled());
        //((Switch) findViewById(R.id.event_allconditions)).setChecked(event.isMatchAllConditions());
        ((CheckBox) findViewById(R.id.event_allconditions)).setChecked(event.isMatchAllConditions());
        ((CheckBox) findViewById(R.id.event_runonce)).setChecked(event.isRunOnce());
        ((CheckBox) findViewById(R.id.event_delayed)).setChecked(event.isDelayed());

        // add all conditions to container
        //ArrayList<DialogOptions> tempConditions = event.getConditions();
        //conditions = event.getConditions();
        //Util.addNewCondition(sInstance, conditions.get(0));
        for (DialogOptions cond : event.getConditions()) {
            //Util.addNewCondition(sInstance, cond, 0);
            Util.actionFrom = Util.ACTION_FROM.EVENT;
            Util.addNewConditionOrAction(sInstance, cond, 0);
        }

        conditions = updatedConditions;

        // also, would be great if we add all actions to container, no?
        //ArrayList<DialogOptions> allAct = event.getActions();
        for (DialogOptions act : event.getActions()) {
            Util.actionFrom = Util.ACTION_FROM.EVENT;
            Util.addNewConditionOrAction(sInstance, act, 0);
        }

        actions = updatedActions;


        /**
         * insert saved profile to container
         */
        if (event.getProfile() != null) {

            final ViewGroup newProfile = (ViewGroup) LayoutInflater.from(this).inflate(
                    R.layout.dialog_pick_single, mContainerProfile, false);

            ((TextView) newProfile.findViewById(android.R.id.text1)).setText(event.getProfile().getName());
            ((TextView) newProfile.findViewById(android.R.id.text2)).setText(
                    (event.getProfile().isActive() ? getString(R.string.active) : getString(R.string.ready))
            );
            ((ImageButton) newProfile.findViewById(R.id.dialog_icon))
                    .setImageDrawable(getResources().getDrawable(event.getProfile().getIcon()));


            // LISTENER for NEW PROFILE
            newProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openDialogProfiles();
                }
            });

            mContainerProfile.addView(newProfile, 0);
        }


    }


    /**
     * EDIT NAME HANDLER
     * <p/>
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
            eventName.setText(((TextView) findViewById(R.id.event_name)).getText());
            //((TextView) findViewById(R.id.event_name)).setText(event.getName());
        } else {
            eventName.setText(getString(R.string.enter_event_name));
        }

        // select all text in edittext
        eventName.setSelectAllOnFocus(true);

        // auto open soft keyboard
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        builder
                .setView(promptView)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(getString(R.string.event_pick_name))

                        //.setView(findViewById())
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // just close the dialog if we didn't select the days
                        dialog.dismiss();

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                        } else {
                            Util.showMessageBox(getString(R.string.event_name_cannot_be_empty), false);
                        }

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
     * ONCLICK: Event Priority
     * <p/>
     * opens builder with possible priorities
     */
    public void onClickEventPriority(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //String[] priorities = {"Low", "Bah", "Normal", "Beee!", "High"};
        String[] priorities = event.getPriorityList().toArray(new String[event.getPriorityList().size()]);

        int currentPriority = event.getPriority() - 1;

        final int[] selected = new int[1];

        builder
                .setTitle(getString(R.string.priority))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        event.setPriority(selected[0] + 1);

                        ((TextView) findViewById(R.id.priority)).setText(event.getPriorityString());

                        isChanged = true;

                        //System.out.println("Priority set is: "+ event.getPriority());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setSingleChoiceItems(priorities, currentPriority, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected[0] = which;
                    }
                })
                .show();

    }


    /**
     * OPEN PROFILES DIALOG
     */
    private void openDialogProfiles() {

        /**
         * NO PROFILES???
         */
        if (BackgroundService.getInstance().profiles.size() == 0) {
            Util.showMessageBox(getString(R.string.no_profiles), true);


            return;
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
                        //.setTitle("Pick Profile")
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // canceling main dialog
                        dialog.dismiss();
                    }
                });


        final AlertDialog dialog = builder.create();


        Profile pTemp = new Profile();
        pTemp.setUniqueID(-1);
        pTemp.setName(getString(R.string.no_profile_selected));
        pTemp.setIcon(R.drawable.ic_profile);
        pTemp.setActive(false);


        ArrayList<Profile> showProfilesInDialog = new ArrayList<Profile>();
        showProfilesInDialog.add(pTemp);
        showProfilesInDialog.addAll(BackgroundService.getInstance().profiles);

        for (final Profile single : showProfilesInDialog) {
            //for (int i = 0; i < BackgroundService.getInstance().profiles.size(); i++) {
            //final DialogOptions opt = BackgroundService.getInstance().profiles.get(i);

            newRow = (ViewGroup) inflater.inflate(R.layout.dialog_pick_single, mContainerOptions, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(single.getName());

            // if we're adding real profile show its state.
            if (single.getUniqueID() != -1) {
                ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                        (single.isActive() ? getString(R.string.active) : getString(R.string.ready))
                );
            }
            // show subtext if dummy profile
            else {
                ((TextView) newRow.findViewById(android.R.id.text2)).setText(
                        ""
                );
            }

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
                    ((ViewGroup) mContainerOptions.getParent()).removeView(mContainerOptions);
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
                            single.isActive() ? getString(R.string.active) : getString(R.string.ready)
                    );
                    ((ImageButton) newProfile.findViewById(R.id.dialog_icon))
                            .setImageDrawable(getResources().getDrawable(single.getIcon()));


                    /**
                     * if we picked NONE, we have to update settings
                     */
                    if (single.getUniqueID() == -1) {
                        ((TextView) newProfile.findViewById(android.R.id.text1)).setText(getString(R.string.profile));
                        ((TextView) newProfile.findViewById(android.R.id.text2)).setText(getString(R.string.profile_select));
                    }


                    // LISTENER for NEW PROFILE
                    newProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
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

                    isChanged = true;

                }
            });

            newRow = null;

            dialog.show();
        }

    }

    public void onClickEventDelayed(View v) {

        final CheckBox checkBox = (CheckBox) findViewById(R.id.event_delayed);

        /**
         * DISABLING DELAY
         */
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);

            event.setDelayed(false);
            isChanged = true;
        }
        /**
         * ENABLING DELAY (open dialog with options)
         */
        else {
            checkBox.setChecked(true);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final EditText input = new EditText(sInstance);
            final TextView info = new TextView(sInstance);
            final TextView info2 = new TextView(sInstance);
            final CheckBox checkBox1 = new CheckBox(sInstance);

            info.setText(getString(R.string.number_of_minutes));
            info.setPadding(15, 15, 15, 5);
            info2.setPadding(15, 5, 15, 15);
            info2.setText(getString(R.string.delay_event_description));
            input.setInputType(InputType.TYPE_CLASS_NUMBER);

            input.setText(String.valueOf(event.getDelayMinutes()));
            checkBox1.setChecked(event.isDelayRecheckConditions());
            checkBox1.setText(getString(R.string.delay_event_recheck));

            LinearLayout newView = new LinearLayout(sInstance);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            newView.setLayoutParams(parms);
            newView.setOrientation(LinearLayout.VERTICAL);
            newView.setPadding(15, 15, 15, 15);
            newView.addView(info, 0);
            newView.addView(input, 1);
            newView.addView(checkBox1, 2);
            newView.addView(info2, 3);

            ScrollView scrollView = new ScrollView(Main.getInstance());
            scrollView.addView(newView);

            builder
                    .setIcon(R.drawable.ic_time)
                    .setTitle(getString(R.string.delay_options))
                    .setView(scrollView)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            String delay = input.getText().toString();

                            if (delay == null || "".equals(delay) || delay.equals("0")) {
                                Util.showMessageBox(getString(R.string.delay_enter_minutes), true);
                                checkBox.setChecked(false);

                                return;
                            }

                            /**
                             * enable event delay actions
                             */
                            event.setDelayEnable(Integer.parseInt(input.getText().toString()), checkBox1.isChecked());

                            isChanged = true;
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            checkBox.setChecked(false);
                        }
                    })
                    .show();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * we've got a OK result, continue
         */
        /**
         * if requestCode is REQUEST_PICK_SHORTCUT, we're continuing on creating shortcut
         */if (resultCode == RESULT_OK) switch (requestCode) {

            case REQUEST_PICK_SHORTCUT:
                startActivityForResult(data, REQUEST_CREATE_SHORTCUT);

                break;

            case REQUEST_CREATE_SHORTCUT:
                String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                //Log.d("sfen", "shortcut name: " + name);

                Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);

                // start shortcut
                //startActivity(intent);
                // http://stackoverflow.com/questions/13147174/android-create-run-shortcuts

                /**
                 * shortcut created. save it.
                 */
                final DialogOptions cond = new DialogOptions(getString(R.string.shortcut), name,
                        R.drawable.ic_dialog, DialogOptions.type.ACT_OPENSHORTCUT);

                cond.setSetting("intent_uri", intent.toUri(Intent.URI_INTENT_SCHEME));

                cond.setSetting("text1", cond.getTitle());
                cond.setSetting("text2", cond.getDescription());


                /**
                 * add new action
                 */
                Util.addNewConditionOrAction(sInstance, cond, 0);

                isChanged = true;

                break;


            case REQUEST_FILEMANAGER_SHORTCUT:

                String path = data.getData().getPath();
                /**
                 * file retrieved. save it.
                 */
                File f = new File(path);

                /**
                 * is it even bash file?
                 */
                if (!f.getName().endsWith(".sh")) {

                    Util.showMessageBox(
                            getString(R.string.file_not_bash, f.getName()),
                            false);

                    break;

                }

                final DialogOptions condFile = new DialogOptions(getString(R.string.shortcut), path,
                        R.drawable.ic_dialog, DialogOptions.type.ACT_RUNSCRIPT);

                condFile.setSetting("FILE", path);
//
                condFile.setSetting("text1", sInstance.getString(R.string.run_script) +" "+
                        f.getName());
                condFile.setSetting("text2", path);


                /**
                 * add new action
                 */
                Util.addNewConditionOrAction(sInstance, condFile, 0);

                isChanged = true;

                break;


        }

    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (isChanged) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setTitle(getString(R.string.error))
                    .setIcon(R.drawable.ic_launcher)
                    .setMessage(getString(R.string.event_changed))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                            saveEvent();

                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    })

                    .show();

        }
        else
            super.onBackPressed();

    }
}