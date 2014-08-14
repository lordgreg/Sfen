package gpapez.sfen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Gregor on 3.8.2014.
 */
public class ProfileActivity extends Activity {

    private static ProfileActivity sInstance = null;
    protected ViewGroup mContainerAction;
    protected ViewGroup mContainerCallAllowDeny;

    // in case of updating profile
    protected boolean isUpdating = false;
    protected boolean isChanged = false;
    protected int updateKey = -1;
    protected ArrayList<DialogOptions> updatedActions;

    // allow & deny list
    protected ArrayList<CallAllowDeny> callAllowDeny = new ArrayList<CallAllowDeny>();

    // placeholder for current Profile
    protected Profile profile = null;

    // arrays for conditions and actions
    final HashMap<String, String> options = new HashMap<String, String>();
    protected ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();

    // request codes (creating shortcuts)
    final int REQUEST_PICK_SHORTCUT = 0x100;
    final int REQUEST_CREATE_SHORTCUT = 0x200;
    final int REQUEST_RINGTONE_RESULT = 3;
    final int REQUEST_NOTIFICATION_RESULT = 4;

    // options hashmap




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // set singleton instance
        sInstance = this;


        /**
         * CONTAINERs
         */
        mContainerAction = (ViewGroup) findViewById(R.id.action_container);
        mContainerCallAllowDeny = (ViewGroup) findViewById(R.id.calllist_container);



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
                Util.actionFrom = Util.ACTION_FROM.PROFILE;
                Util.openDialog(sInstance, DialogOptions.optActions, "Pick action");
            }
        });

        mContainerAction.addView(newAction, 0);


        // stop! hammertime!
        // lets check if we got any event passed to us!
        if (getIntent().getStringExtra("sProfile") != null) {
            isUpdating = true;

            profile = (new Gson()).fromJson(getIntent().getExtras().getString("sProfile"), Profile.class);
            updateKey = getIntent().getIntExtra("sProfileIndexKey", -1);
            updatedActions = new ArrayList<DialogOptions>();

            getActionBar().setTitle("Editing "+ profile.getName());
            //getActionBar().

            //Log.e("EVENT FROM OBJ", event.getName() + " with " + event.getConditions().size() + " conditions- key from all events: " + updateKey);
            refreshView();
        }

        /**
         * we're creating new!
         */
        else {
            profile = new Profile();

            refreshView();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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

            finish();
            return true;


        }
        if (id == R.id.action_save) {
            // if event was successfully saved, check if we have to create alarms
            // geofaces if we have such conditions
            if (saveProfile()) {
                //refreshView();
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
    public static ProfileActivity getInstance() {
        if (sInstance == null) {
            return new ProfileActivity();
        }
        else
            return sInstance;
    }

    /**
     * Saving/updating event!
     */
    private boolean saveProfile() {


        // do we have event name?
        if (((TextView) findViewById(R.id.profile_name)).getText().length() == 0) {
            Util.showMessageBox("And you think you can get away without entering Event name?", true);
            return false;
        }

//        if (!isUpdating) {
//            profile = new Profile();
//        }

        /**
         * get parameters from ui and save it to our Profile object
         */
        profile.setName(((TextView) findViewById(R.id.profile_name)).getText().toString());
        profile.setVibrate(((CheckBox) findViewById(R.id.profile_vibrate)).isChecked());


        /**
         * if we didn't set ringtone and notification tones, set them to defaults
         */
        if (profile.getRingtone() == null) {
            profile.setRingtone(
                    RingtoneManager.getActualDefaultRingtoneUri(sInstance, RingtoneManager.TYPE_RINGTONE)
            );

            profile.setNotification(
                    RingtoneManager.getActualDefaultRingtoneUri(sInstance, RingtoneManager.TYPE_NOTIFICATION)
            );

        }


        // if tag of image button is null, save icon as ic_notification
        if ((Integer)((ImageButton) findViewById(R.id.profile_icon)).getTag() == null) {
            profile.setIcon(R.drawable.ic_notification);
        }
        else
            profile.setIcon((Integer) ((ImageButton) findViewById(R.id.profile_icon)).getTag());


        /**
         * save actions too
         */
        profile.setActions(actions);



        // finally, save/update profile to profiles array
        if (isUpdating) {
            //events.set(events.indexOf(e), e);
            BackgroundService.getInstance().profiles.set(updateKey, profile);
        }
        else {
            BackgroundService.getInstance().profiles.add(profile);
        }


        /**
         * there's one more thing we really REALLY have to do,
         * edit profile at all events
         */
        if (isUpdating) {
            for (int i = 0; i < BackgroundService.getInstance().events.size(); i++) {
                Event e = BackgroundService.getInstance().events.get(i);

                if (e.getProfile() != null && e.getProfile().getUniqueID() == profile.getUniqueID()) {
                    e.setProfile(profile);

                    BackgroundService.getInstance().events.set(i, e);

                    System.out.println("Updated event "+
                            BackgroundService.getInstance().events.get(i).getName() +"" +
                            "with new profile.");

                }
            }
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
        ((TextView) findViewById(R.id.profile_name)).setText(profile.getName());
        ((CheckBox) findViewById(R.id.profile_vibrate)).setChecked(profile.isVibrate());

        //Uri uri = profile.getRingtone();
        //Ringtone ring = RingtoneManager.getRingtone(sInstance, profile.get);

        if (isUpdating && profile.getRingtone() != null) {
            Ringtone ringtone = RingtoneManager.getRingtone(sInstance, profile.getRingtone());
            ((TextView) findViewById(R.id.ringtone_name)).setText(ringtone.getTitle(sInstance));
        }
        else {
            Uri ringtoneDefaultUri = Uri.parse("content://settings/system/ringtone");
            profile.setRingtone(ringtoneDefaultUri);

            Ringtone ringtoneDefault = RingtoneManager.getRingtone(sInstance, ringtoneDefaultUri);
            ((TextView) findViewById(R.id.ringtone_name)).setText(ringtoneDefault.getTitle(sInstance));
        }

        if (isUpdating && profile.getNotification() != null) {
            Gson gson = new Gson();
            Ringtone notification = RingtoneManager.getRingtone(sInstance, profile.getNotification());
            ((TextView) findViewById(R.id.notification_name)).setText(
                    notification.getTitle(sInstance));
        }
        else {
            Uri notificationDefaultUri = Uri.parse("content://settings/system/notification_sound");
            profile.setNotification(notificationDefaultUri);

            Ringtone ringtoneDefault = RingtoneManager.getRingtone(sInstance, notificationDefaultUri);
            ((TextView) findViewById(R.id.notification_name)).setText(ringtoneDefault.getTitle(sInstance));
        }

        // if icon isn't empty, add icon too.
        if (profile.getIcon() != 0) {
            ((ImageButton) findViewById(R.id.profile_icon)).setImageDrawable(getResources().getDrawable(profile.getIcon()));
            ((ImageButton) findViewById(R.id.profile_icon)).setTag(profile.getIcon());
        }

        // also, would be great if we add all actions to container, no?
        ArrayList<DialogOptions> allAct = profile.getActions();
        for (DialogOptions act : profile.getActions()) {
            addNewAction(sInstance, act, 0);
        }

        actions = updatedActions;


        /**
         * add allow/deny call list. if none, create new button
         */
        if (callAllowDeny == null || callAllowDeny.size() == 0) {

            // addnew
            final ViewGroup newRow = (ViewGroup) LayoutInflater.from(this).inflate(
                    R.layout.condition_action_header, mContainerAction, false);

            ((TextView) newRow.findViewById(android.R.id.text1)).setText(getString(R.string.calllist_new));
            ((TextView) newRow.findViewById(android.R.id.text2)).setText(getString(R.string.calllist_new_sub));

            // LISTENER for NEW ACTION
            newRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getBaseContext(), "picking new action", Toast.LENGTH_SHORT).show();
                    Util.actionFrom = Util.ACTION_FROM.PROFILE;

                    // run static CallAllowDeny.openSelection

                }
            });

            mContainerCallAllowDeny.addView(newRow, 0);


        }

    }


    /**
     *
     * ADD NEW ACTION
     *
     */
    protected void addNewAction(final Activity context, final DialogOptions entry, final int index) {

        // the only thing we have to check if we're editing entry is,
        // if we have at least one setting stored. if so, all is good in our wonderland
        //final boolean isEditing = (cond.getSettings().size() > 0) ? true : false;

        // add condition to list of conditions of Event
        if (isUpdating) {
            updatedActions.add(entry);
        }
        // adding NEW
        else {
            if (actions == null)
                actions = new ArrayList<DialogOptions>();

            actions.add(entry);
        }

        // get options that we need for interface
        String title = entry.getSetting("text1");
        String description = entry.getSetting("text2");
        int icon = entry.getIcon();

        // add new row to actions/conditions now
        final ViewGroup newRow;

        newRow = (ViewGroup) LayoutInflater.from(context).inflate(
                R.layout.condition_single_item, mContainerAction, false);

        ((TextView) newRow.findViewById(android.R.id.text1)).setText(title);
        ((TextView) newRow.findViewById(android.R.id.text2))
                .setText(description);
        //((TextView) newRow.findViewById(android.R.id.text2))
        //        .setMovementMethod(new ScrollingMovementMethod());

        ((ImageButton) newRow.findViewById(R.id.condition_icon))
                .setImageDrawable(context.getResources().getDrawable(icon));

        newRow.findViewById(R.id.condition_single_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: clicking our newly added condition
                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);
                Util util = new Util();
                util.openSubDialog(context, entry, index);
                //showMessageBox("clicked " + entry.getTitle() + ", " + entry.getOptionType() +" type: "+ entry.isItemConditionOrAction() +" on index "+ index, false);
            }
        });

        /**
         * delete button for single item
         */
        newRow.findViewById(R.id.condition_single_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when clicking recycle bin at condition, remove it from view and
                // from array of all conditions

                int index = ((ViewGroup) newRow.getParent()).indexOfChild(newRow);

                removeAction(index, entry);

            }
        });


        // add action to container
        mContainerAction.addView(newRow, index);

    }

    /**
     *
     * REMOVE ACTION
     *
     */
    protected void removeAction(final int index, final DialogOptions entry) {
        // when clicking recycle bin at condition/action, remove it from view and
        // from array of all conditions/actions

        // remove ACTION from container first
        mContainerAction.removeViewAt(index);


        //container.removeView(newRow);

        // UPDATING SINGLE EVENT!!!
        // remove from conditions, depending on if we're adding to new event
        // or existing event
        if (isUpdating) {

            updatedActions.remove(updatedActions.indexOf(entry));

            // we changed something, so set the changed boolean
            isChanged = true;


        }

        // CREATING SINGLE profile!!!
        else {

                actions.remove(actions.indexOf(entry));

        }
    }


    /**
     * onClick: PROFILE NAME
     */
    public void onClickProfileName(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // Set an EditText view to get user input
        final TextView info = new TextView(this);
        final EditText input = new EditText(this);

//        info.setText("Input text");

        if (isUpdating) {
            input.setText(profile.getName());
        }

        // select all text in edittext
        input.setSelectAllOnFocus(true);

        // auto open soft keyboard
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);



        LinearLayout newView = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);
        //newView.addView(info, 0);
        newView.addView(input, 0);



        builder
                .setView(newView)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Enter Profile name")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (input.getText().toString().equals("")) {
                            Util.showMessageBox("You must enter profile name!", false);
                            return;
                        } else {

                            ((TextView) findViewById(R.id.profile_name)).setText(input.getText().toString());

                        }

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // just close the dialog if we didn't select the days
                        dialog.dismiss();

                        // close the keyboard if any
                        if (imm != null) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        }

                    }
                });


        builder.show();


    }


    /**
     *
     * OnClick: PROFILE ICON
     *
     * opens up a dialog with all possible icons
     *
     */
    public void onClickProfileIcon(View v) {

        // grid layout
        GridView newView = new GridView(sInstance);
        GridView.LayoutParams params = new GridView.LayoutParams(
                GridView.LayoutParams.MATCH_PARENT,
                GridView.LayoutParams.MATCH_PARENT
        );


        newView.setLayoutParams(params);
        newView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        newView.setColumnWidth(
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 68, getResources().getDisplayMetrics())
        );
        newView.setNumColumns(GridView.AUTO_FIT);
        newView.setGravity(Gravity.CENTER);
        newView.setPadding(15, 15, 15, 15);

        newView.setAdapter(new ImageAdapter(this));


        final AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);

        builder
                .setView(newView)
                .setTitle("Select Icon")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();

        newView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                dialog.dismiss();

                /**
                 * store icon ID to imageview of profileactivity
                 */
                ImageButton imageButton = (ImageButton) findViewById(R.id.profile_icon);
                imageButton.setImageResource(ImageAdapter.mThumbIds[position]);
                imageButton.setTag(ImageAdapter.mThumbIds[position]);
                //Toast.makeText(sInstance, "" + position, Toast.LENGTH_SHORT).show();

            }
        });

        dialog.show();


    }


    /**
     *
     * OnClick: PROFILE VOLUMES
     *
     */
    public void onClickVolumes(View v) {

        /**
         * build up dialog interface with items
         */
        final AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);

        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_profile_volumes, null);

        /**
         * set max progress depending on AudioManager settings
         */
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        ((SeekBar) dialogView.findViewById(R.id.seekbar_ringtone)).setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING));
        ((SeekBar) dialogView.findViewById(R.id.seekbar_notification)).setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
        ((SeekBar) dialogView.findViewById(R.id.seekbar_music)).setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        ((SeekBar) dialogView.findViewById(R.id.seekbar_alarm)).setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
        //audioManager.getStreamMaxVolume(AudioManager.STREAM_RING

        /**
         * set current progress
         */
        ((SeekBar) dialogView.findViewById(R.id.seekbar_ringtone)).setProgress(profile.getVolumeRingtone());
        ((SeekBar) dialogView.findViewById(R.id.seekbar_notification)).setProgress(profile.getVolumeNotification());
        ((SeekBar) dialogView.findViewById(R.id.seekbar_music)).setProgress(profile.getVolumeMusic());
        ((SeekBar) dialogView.findViewById(R.id.seekbar_alarm)).setProgress(profile.getVolumeAlarm());


        builder
                .setView(dialogView)
                .setTitle("Volumes")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        /**
                         * close the dialog
                         */
                        dialogInterface.dismiss();

                        /**
                         * save volumes to profile
                         */
                        profile.setVolumeRingtone(
                                ((SeekBar) dialogView.findViewById(R.id.seekbar_ringtone)).getProgress()
                        );

                        profile.setVolumeNotification(
                                ((SeekBar) dialogView.findViewById(R.id.seekbar_notification)).getProgress()
                        );

                        profile.setVolumeMusic(
                                ((SeekBar) dialogView.findViewById(R.id.seekbar_music)).getProgress()
                        );

                        profile.setVolumeAlarm(
                                ((SeekBar) dialogView.findViewById(R.id.seekbar_alarm)).getProgress()
                        );

                    }
                })
        .show();

    }

    /**
     *
     * OnClick: PROFILE VIBRATE
     *
     */
    public void onClickProfileVibrate(View v) {

        if (isUpdating) {

            profile.setVibrate(
                    ((CheckBox) findViewById(R.id.profile_vibrate)).isChecked()
            );

        }
    }

    /**
     *
     * OnClick: PROFILE RINGTONE
     *
     */
    public void onClickProfileRingtone(View v) {

        Uri ringtoneUri = profile.getRingtone();

        final Intent ringtone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));

        // TODO: add selected ringtone when opening!) RingtoneManager.EXTRA_RINGTONE_PICKED_URI

        startActivityForResult(ringtone, REQUEST_RINGTONE_RESULT);


    }

    /**
     *
     * OnClick: PROFILE NOTIFICATION SOUND
     *
     */
    public void onClickProfileNotificationSound(View v) {

        Uri notificationUri = profile.getNotification();

        final Intent ringtone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationUri);
        ringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        startActivityForResult(ringtone, REQUEST_NOTIFICATION_RESULT);

    }


    /**
     *
     * OnClick: PROFILE BRIGHTNESS
     *
     * Will open dialog with progressbar and checkbox for
     * brightness setting
     *
     * At the end, it will send broadcast to start the actions
     *
     */
    public void onClickProfileBrightness(View v) {

        /**
         * (create &) open dialog
         */
        // Set an EditText view to get user input
        final CheckBox checkBox = new CheckBox(sInstance);
        final SeekBar seekBar = new SeekBar(sInstance);
        seekBar.setMax(255); // MAX VALUE = SCREEN_BRIGHTNESS
        seekBar.setProgress(255 / 2);

        checkBox.setText("Automagically adjust brightness");

        /**
         * checking automode disables seekbar
         */
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setEnabled(!checkBox.isChecked());
            }
        });

        // Updating Profile, add values from config to dialog
        if (isUpdating) {
            checkBox.setChecked(profile.isBrightnessAuto());

            // seekbar is enabled only if checkbox isn't checked
            seekBar.setEnabled(!checkBox.isChecked());

            if (profile.getBrightnessValue() >= 0)
                seekBar.setProgress(profile.getBrightnessValue());
        }
        else {
            seekBar.setProgress(profile.getBrightnessValue());
            checkBox.setChecked(profile.isBrightnessAuto());
        }


        LinearLayout newView = new LinearLayout(sInstance);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(parms);
        newView.setOrientation(LinearLayout.VERTICAL);
        newView.setPadding(15, 15, 15, 15);
        newView.addView(seekBar, 0);
        newView.addView(checkBox, 1);

        /**
         * add checkbox for default settings
         */
        final CheckBox checkDefault = new CheckBox(sInstance);
        checkDefault.setChecked(profile.isBrightnessDefault());

        if (profile.isBrightnessDefault()) {
            seekBar.setEnabled(false);
            checkBox.setEnabled(false);
        }

        checkDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setEnabled(!checkDefault.isChecked());

                if (!checkBox.isChecked())
                    seekBar.setEnabled(!checkDefault.isChecked());
            }
        });
        checkDefault.setText("Use default system settings");
        newView.addView(checkDefault, 2);


        final AlertDialog.Builder builder = new AlertDialog.Builder(sInstance);


        builder
                .setView(newView)
                .setTitle("Brightness")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        /**
                         * save selections to options
                         */
                        //options.put("BRIGHTNESS_VALUE", String.valueOf(seekBar.getProgress()));
                        profile.setBrightnessValue(seekBar.getProgress());
                        profile.setBrightnessAuto(checkBox.isChecked());
                        profile.setBrightnessDefault(checkDefault.isChecked());


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();


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
                final DialogOptions cond = new DialogOptions("Shortcut", name,
                        R.drawable.ic_dialog, DialogOptions.type.ACT_OPENSHORTCUT);

                cond.setSetting("intent_uri", intent.toUri(Intent.URI_INTENT_SCHEME));

                cond.setSetting("text1", cond.getTitle());
                cond.setSetting("text2", cond.getDescription());


                /**
                 * add new action
                 */
                addNewAction(sInstance, cond, 0);

                break;


            /**
             * selected ringtone from Intent
             */
            case REQUEST_RINGTONE_RESULT:

                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
                // Get your title here `ringtone.getTitle(this)`
                //System.out.println("picked ringtone "+ ringtone.getTitle(sInstance));
//                Uri ringtoneDefaultUri = RingtoneManager.getActualDefaultRingtoneUri(sInstance, RingtoneManager.TYPE_RINGTONE);
//                Ringtone ringtoneDefault = RingtoneManager.getRingtone(sInstance, ringtoneDefaultUri);
//
//                System.out.println(ringtone.getTitle(sInstance) +" vs "+ ringtoneDefault.getTitle(sInstance));
//                System.out.println(ringtone.hashCode() +" vs "+ ringtoneDefault.hashCode());
//                System.out.println(uri +" vs "+ ringtoneDefaultUri);
                //ImageButton imageButton = (ImageButton) findViewById(R.id.profile_icon);
                TextView textView = (TextView) findViewById(R.id.ringtone_name);
                textView.setText(ringtone.getTitle(sInstance));

                /*RingtoneManager.setActualDefaultRingtoneUri(
                        Context,
                        RingtoneManager.TYPE_RINGTONE,
                        Uri
                                .parse("Media file uri"));
                */
                //RingtoneManager.
                //RingtoneManager

                //gson = new Gson();

                profile.setRingtone(uri);


                break;

            /**
             * selected notification form Intent
             */
            case REQUEST_NOTIFICATION_RESULT:

                uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                ringtone = RingtoneManager.getRingtone(this, uri);
                // Get your title here `ringtone.getTitle(this)`
                //System.out.println("picked ringtone "+ ringtone.getTitle(sInstance));
//                Uri ringtoneDefaultUri = RingtoneManager.getActualDefaultRingtoneUri(sInstance, RingtoneManager.TYPE_RINGTONE);
//                Ringtone ringtoneDefault = RingtoneManager.getRingtone(sInstance, ringtoneDefaultUri);
//
//                System.out.println(ringtone.getTitle(sInstance) +" vs "+ ringtoneDefault.getTitle(sInstance));
//                System.out.println(ringtone.hashCode() +" vs "+ ringtoneDefault.hashCode());
//                System.out.println(uri +" vs "+ ringtoneDefaultUri);
                //ImageButton imageButton = (ImageButton) findViewById(R.id.profile_icon);
                textView = (TextView) findViewById(R.id.notification_name);
                textView.setText(ringtone.getTitle(sInstance));

                profile.setNotification(uri);


                break;

        }



    }

}
