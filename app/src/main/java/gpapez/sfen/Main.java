package gpapez.sfen;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends Activity {

    // singleton object
    private static Main sInstance = null;

    // background service object
    protected static Intent bgService = null;

    // TAG
    protected String TAG;

    // if main activity is visible or not (will change onResume and onPause)
    protected boolean isVisible = false;

    // this variable will set itself to false on the last line of onCreate method
    private boolean isCreating = true;

    // position of current tab
    protected int mTabPosition = 0;
    private int mSavedTabPosition = 0;

    // tab fragment
    // create fragments
    protected FragmentEvent fragmentEvent;
    protected FragmentProfile fragmentProfile;

    // HashMap with options. Instead of creating more variables to use around
    // activities, I've created HashMap; putting settings here if needed.
    HashMap<String, String> options = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set singleton instance
        sInstance = this;

        // tag
        TAG = sInstance.getClass().getPackage().getName();


        /**
         * create fragment
         */
        fragmentEvent = new FragmentEvent();
        fragmentProfile = new FragmentProfile();


        // Set up the action bar to show tabs.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // for each of the sections in the app, add a tab to the action bar.
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.events))
                .setTag(getString(R.string.events))
                .setTabListener(new MainTabListener(fragmentEvent)));
        actionBar.addTab(actionBar.newTab().setText(getString(R.string.profiles))
                .setTag(getString(R.string.profiles))
                .setTabListener(new MainTabListener(fragmentProfile)));
//        actionBar.addTab(actionBar.newTab().setText("Whitelists")
//                .setTabListener(this));





        /**
         * set tab position
         */
        try {
            mSavedTabPosition = savedInstanceState.getInt("TAB_POSITION", 0);
            mTabPosition = mSavedTabPosition;
        }
        catch (Exception e) {}

//        System.out.println("*** TAB POSITION: "+ mSavedTabPosition);


        actionBar.setSelectedNavigationItem(mSavedTabPosition);


        /**
         * Find our service in the list of all running services
         * If found, get its intent and we're good to continue its activity
         *
         * Otherwise, create new BackGround service.
         */
        boolean mIsBackgroundServiceRunning = false;

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //System.out.println("service: "+ service.service.getClassName());
            if ((getClass().getPackage().getName() +".BackgroundService").equals(service.service.getClassName())) {
                //System.out.println("our background service is running.");

                bgService = BackgroundService.getInstance().sIntent;

                if (bgService != null) {

                    //System.out.println(bgService.toString());
                    mIsBackgroundServiceRunning = true;
                    sendBroadcast("EVENT_ENABLED");
                    break;
                }
            }
        }

        if (!mIsBackgroundServiceRunning) {
            bgService = new Intent(this, BackgroundService.class);
            startService(bgService);
        }


        /**
         * if first time running show dialog with welcome to Sfen
         */
        runCountTrigger();




        // end of onCreate
        isCreating = false;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /**
         * save current position of tab here
         */
        outState.putInt("TAB_POSITION", mTabPosition);


        /**
         * save fragments
         */
//        if (mTabPosition == 0)
//            getFragmentManager()
//                    .putFragment(outState, FragmentEvent.class.getName(), fragmentEvent);
//
//        if (mTabPosition == 1)
//            getFragmentManager()
//                    .putFragment(outState, FragmentProfile.class.getName(), fragmentProfile);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // set to visible
        isVisible = true;

        /**
         * set tab position
         */
        //mTabPosition = mSavedTabPosition;

        refreshCurrentView();

    }

    @Override
    protected void onPause() {
        super.onPause();

        // set to invisible
        isVisible = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // set visibility of menu items with icons
        if (mTabPosition == 0) {
            menu.findItem(R.id.event_add_new).setVisible(true);
            menu.findItem(R.id.profile_add_new).setVisible(false);
        }
        // profiles
        else if (mTabPosition == 1) {
            menu.findItem(R.id.event_add_new).setVisible(false);
            menu.findItem(R.id.profile_add_new).setVisible(true);
        }
        // whitelist
        else if (mTabPosition == 2) {
            menu.findItem(R.id.event_add_new).setVisible(false);
            menu.findItem(R.id.profile_add_new).setVisible(false);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.event_add_new) {

            startActivity(new Intent(this, EventActivity.class));

            return true;
        }

        if (id == R.id.profile_add_new) {
            startActivity(new Intent(this, ProfileActivity.class));

            return true;
        }

        // close the application and, of course background process
        if (id == R.id.action_exit) {
            // quitting? so soon? ask nicely, windows mode, if person is sure!
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage(getString(R.string.service_will_be_stopped))
                    .setIcon(getResources().getDrawable(R.drawable.ic_launcher))
                    .setTitle(getString(R.string.error))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (bgService != null) {
                                stopService(bgService);
                                BackgroundService.getInstance().stopForeground(true);
                            }
                            finish();

                        }
                    })

                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            //return;
                        }
                    });

            // open the dialog now :)
            builder.show();

        }

        if (id == R.id.action_preferences) {

            startActivity(new Intent(this, PreferencesActivity.class));

        }

        if (id == R.id.action_about) {

            startActivity(new Intent(this, AboutActivity.class));

        }

        // export settings
        if (id == R.id.action_export) {
            Gson gson = new Gson();

            HashMap<String, String> exportedValues = new HashMap<String, String>();

            String exportString = "";

            /**
             * we have to export events and profiles separately
             */
            exportString +=
                    "<<EVENTS>>"+
                    gson.toJson(BackgroundService.getInstance().events)

                    // delimiter
                    +"<<>>"+

                    "<<PROFILES>>"+
                    gson.toJson(BackgroundService.getInstance().profiles)
            ;




            //exportedValues.put("events", gson.toJson(BackgroundService.getInstance().events));
            //System.out.println("EVENTS\n==========================\n"+ exportedValues.get("events"));

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            //ClipData clip = ClipData.newPlainText("Sfen", exportedValues.get("events"));
            ClipData clip = ClipData.newPlainText("Sfen", exportString);
            clipboard.setPrimaryClip(clip);

            Util.showMessageBox(getString(R.string.copied_to_clipboard), true);

        }

        // import settings
        if (id == R.id.action_import) {
            //final TextView info = new TextView(sInstance);
            //info.setText("Paste import string:");
            final EditText input = new EditText(sInstance);

            input.setSingleLine(true);
            input.setLines(5);
            input.setGravity(Gravity.TOP);
            input.setHorizontallyScrolling(false);

            //input.setHeight();
            LinearLayout newView = new LinearLayout(sInstance);
            LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            newView.setLayoutParams(parms);
            newView.setOrientation(LinearLayout.VERTICAL);
            newView.setPadding(15, 15, 15, 15);
            newView.addView(input, 0);

            final AlertDialog.Builder builder = new AlertDialog.Builder(Main.getInstance());

            builder
                    .setView(newView)
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(getString(R.string.import_string))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            if (input.length() == 0) {
                                Util.showMessageBox(getString(R.string.nothing_to_import), true);
                                return;
                            }

                            /**
                             * call the import command
                             */
                            Util.importSettings(input.getText().toString());

                            /**
                             * done, refresh view at the end
                             */
                            refreshCurrentView();



                        }
                    })
                            //.set
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // just close the dialog if we didn't select the days
                            dialog.dismiss();

                        }
                    });


            builder.show();



        }

        if (id == R.id.action_logs) {

            startActivity(new Intent(this, Logs.class));

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        // adding instance of Analytics object
        EasyTracker.getInstance(this).activityStart(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        // finishing analytics activity.
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }


    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public static Main getInstance() {
//        if (sInstance == null) {
//            return new Main();
//        }
//        else
            return sInstance;
    }


    /**
     * SEND NEW BROADCAST
     */
    protected void sendBroadcast(String broadcast) {
        if (broadcast.length() > 0) {
            Intent intent = new Intent();
            //android.util.Log.e("send broadcast", sInstance.getClass().getPackage().getName() +"."+ broadcast);
            intent.setAction(sInstance.getClass().getPackage().getName() +"."+ broadcast);
            sendBroadcast(intent);
        }
    }


    /**
     * call proper view refresh based on current tab selected
     */
    protected void refreshCurrentView() {
        String tag = "";
        if (mTabPosition == 0) {
            fragmentEvent.refreshEventsView();
        }
        else if (mTabPosition == 1) {
            fragmentProfile.refreshProfilesView();
        }

    }

    /**
     * depending on nth run, show or do something
     */
    protected void runCountTrigger() {

        int runCount = Preferences.getSharedPreferences(this).getInt("RUN_COUNT", 0);


        if (runCount == 0) {

            /**
             * show welcome dialog
             */
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(getString(R.string.welcome_to_sfen))
                    .setMessage(getString(R.string.prepare_simple_events_and_profiles))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            /**
                             * string with prepared events & profiles
                             */
                            String preparedString = "<<EVENTS>>[{\"actions\":[],\"conditions\":[{\"description\":\"Day(s) of week\",\"icon\":\"ic_date\",\"title\":\"Days\",\"optionType\":\"DAYSOFWEEK\",\"settings\":{\"text1\":\"Days (5)\",\"text2\":\"Monday, Tuesday, Wednesday, Thursday, Friday\",\"selectedDays\":\"[0,1,2,3,4]\"},\"maxNumber\":0,\"uniqueID\":706033541},{\"description\":\"Time Range\",\"icon\":\"ic_time\",\"title\":\"Time Range\",\"optionType\":\"TIMERANGE\",\"settings\":{\"fromHour\":\"7\",\"toHour\":\"15\",\"toMinute\":\"0\",\"fromMinute\":\"0\",\"text1\":\"Time Range\",\"text2\":\"From 07:00 to 15:00\"},\"maxNumber\":0,\"uniqueID\":1692299036}],\"name\":\"At work\",\"delayed\":false,\"enabled\":true,\"forceRun\":false,\"hasRun\":true,\"matchAllConditions\":true,\"delayRecheckConditions\":false,\"priority\":1,\"profile\":610007126,\"runOnce\":false,\"running\":true,\"delayMinutes\":3,\"uniqueID\":1835553301},{\"actions\":[{\"description\":\"Will make a sheep sound\",\"icon\":\"ic_sound\",\"title\":\"Play Sfen\",\"optionType\":\"ACT_PLAYSFEN\",\"settings\":{\"text1\":\"Play Sfen\",\"text2\":\"Sound of Sfen will be heard\"},\"maxNumber\":0,\"uniqueID\":-1},{\"description\":\"Disable Wifi when conditions met\",\"icon\":\"ic_wifi\",\"title\":\"Disable Wifi\",\"optionType\":\"ACT_WIFIDISABLE\",\"settings\":{\"text1\":\"Disable Wifi\",\"text2\":\"Wifi will be Disabled\"},\"maxNumber\":0,\"uniqueID\":-1}],\"conditions\":[{\"description\":\"Selected battery level\",\"icon\":\"ic_battery\",\"title\":\"Battery level\",\"optionType\":\"BATTERY_LEVEL\",\"settings\":{\"BATTERY_LEVEL_TO\":\"20\",\"BATTERY_LEVEL_FROM\":\"0\",\"text1\":\"Battery level\",\"text2\":\"Battery between 0% and 20%\"},\"maxNumber\":0,\"uniqueID\":1165744599}],\"name\":\"Battery low\",\"delayed\":false,\"enabled\":true,\"forceRun\":false,\"hasRun\":false,\"matchAllConditions\":true,\"delayRecheckConditions\":false,\"priority\":1,\"profile\":-1,\"runOnce\":false,\"running\":false,\"delayMinutes\":3,\"uniqueID\":2039440392},{\"actions\":[{\"description\":\"Will make a sheep sound\",\"icon\":\"ic_sound\",\"title\":\"Play Sfen\",\"optionType\":\"ACT_PLAYSFEN\",\"settings\":{\"text1\":\"Play Sfen\",\"text2\":\"Sound of Sfen will be heard\"},\"maxNumber\":0,\"uniqueID\":-1},{\"description\":\"Will show dialog with text\",\"icon\":\"ic_dialog\",\"title\":\"Dialog with text\",\"optionType\":\"ACT_DIALOGWITHTEXT\",\"settings\":{\"text\":\"Battery full!\",\"text1\":\"Dialog with text\",\"text2\":\"Battery full!\"},\"maxNumber\":0,\"uniqueID\":-1}],\"conditions\":[{\"description\":\"Status of battery\",\"icon\":\"ic_battery\",\"title\":\"Battery status\",\"optionType\":\"BATTERY_STATUS\",\"settings\":{\"BATTERY_STATUS_KEY\":\"3\",\"BATTERY_STATUS\":\"Full\",\"text1\":\"Battery status\",\"text2\":\"Battery is Full\"},\"maxNumber\":0,\"uniqueID\":1297573910}],\"name\":\"Battery full\",\"delayed\":false,\"enabled\":true,\"forceRun\":false,\"hasRun\":false,\"matchAllConditions\":true,\"delayRecheckConditions\":false,\"priority\":1,\"profile\":-1,\"runOnce\":false,\"running\":false,\"delayMinutes\":3,\"uniqueID\":1903268961},{\"actions\":[],\"conditions\":[{\"description\":\"If Headset is connected\",\"icon\":\"ic_headset\",\"title\":\"Headset connected\",\"optionType\":\"HEADSET_CONNECTED\",\"settings\":{\"text1\":\"Headset connected\",\"text2\":\"Headset is Connected\"},\"maxNumber\":0,\"uniqueID\":783402977}],\"name\":\"Headset on\",\"delayed\":false,\"enabled\":true,\"forceRun\":false,\"hasRun\":false,\"matchAllConditions\":true,\"delayRecheckConditions\":false,\"priority\":1,\"profile\":909521167,\"runOnce\":false,\"running\":false,\"delayMinutes\":3,\"uniqueID\":646793162},{\"actions\":[],\"conditions\":[{\"description\":\"Time Range\",\"icon\":\"ic_time\",\"title\":\"Time Range\",\"optionType\":\"TIMERANGE\",\"settings\":{\"fromHour\":\"22\",\"toHour\":\"7\",\"toMinute\":\"0\",\"fromMinute\":\"0\",\"text1\":\"Time Range\",\"text2\":\"From 22:00 to 07:00\"},\"maxNumber\":0,\"uniqueID\":271955113}],\"name\":\"At night\",\"delayed\":false,\"enabled\":true,\"forceRun\":false,\"hasRun\":false,\"matchAllConditions\":true,\"delayRecheckConditions\":false,\"priority\":3,\"profile\":1785221699,\"runOnce\":false,\"running\":false,\"delayMinutes\":3,\"uniqueID\":674702520}]<<>><<PROFILES>>[{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Night\",\"callAllowDenies\":[],\"icon\":\"ic_night\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":false,\"isLockedFor\":0,\"defaultNotification\":true,\"isVibrate\":true,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":1785221699,\"volumeAlarm\":7,\"volumeMusic\":4,\"volumeNotification\":0,\"volumeRingtone\":4},{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Meeting\",\"callAllowDenies\":[],\"icon\":\"ic_work\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":true,\"isLockedFor\":1,\"defaultNotification\":true,\"isVibrate\":true,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":1105441116,\"volumeAlarm\":7,\"volumeMusic\":3,\"volumeNotification\":0,\"volumeRingtone\":0},{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Normal\",\"callAllowDenies\":[],\"icon\":\"ic_day\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":false,\"isLockedFor\":0,\"defaultNotification\":true,\"isVibrate\":true,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":909521167,\"volumeAlarm\":7,\"volumeMusic\":11,\"volumeNotification\":5,\"volumeRingtone\":5},{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Work\",\"callAllowDenies\":[],\"icon\":\"ic_work\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":false,\"isLockedFor\":0,\"defaultNotification\":true,\"isVibrate\":false,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":610007126,\"volumeAlarm\":7,\"volumeMusic\":3,\"volumeNotification\":0,\"volumeRingtone\":5},{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Silent\",\"callAllowDenies\":[],\"icon\":\"ic_deny\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":false,\"isLockedFor\":0,\"defaultNotification\":true,\"isVibrate\":false,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":1652943054,\"volumeAlarm\":7,\"volumeMusic\":0,\"volumeNotification\":0,\"volumeRingtone\":0},{\"actions\":[],\"ringtone\":\"content://settings/system/ringtone\",\"notification\":\"content://settings/system/notification_sound\",\"name\":\"Silent \\u0026 Vibrate\",\"callAllowDenies\":[],\"icon\":\"ic_deny\",\"defaultRingtone\":true,\"isActive\":false,\"isLocked\":false,\"isLockedFor\":0,\"defaultNotification\":true,\"isVibrate\":true,\"isVolumeButtonsDisable\":false,\"brightnessValue\":80,\"brightnessDefault\":true,\"brightnessAuto\":true,\"uniqueID\":1331147513,\"volumeAlarm\":7,\"volumeMusic\":0,\"volumeNotification\":0,\"volumeRingtone\":0}]";

                            Util.importSettings(preparedString);

                            refreshCurrentView();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();

        }

        else if (runCount == 5) {

            /**
             * show donate dialog
             */
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(getString(R.string.app_name))
                    .setMessage(getString(R.string.donate_proposal_description))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            Intent browserIntent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=gregorp%40gmail%2ecom&lc=SI&item_name=Sfen&item_number=sfen&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
                            startActivity(browserIntent);

                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();


        }

        /**
         * increase the value of run number
         */
        Preferences.getSharedPreferences(this).edit().putInt("RUN_COUNT", ++runCount).apply();


    }



}
