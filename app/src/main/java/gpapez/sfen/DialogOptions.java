package gpapez.sfen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This is the DialogOptions class that has more
 * variables than hashmap that we would use
 * when showing Options
 * Created by Gregor on 9.7.2014.
 */
public class DialogOptions {
    private String title;
    private String description;
    private int icon;

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



    // list of possible Actions in Options
    //context.getResources().getDrawable(R.drawable.ic_launcher)
    static final ArrayList<DialogOptions> optActions = new ArrayList<DialogOptions>() {{
        //add(new DialogOptions("Show notification", "Will show notification in notification area", android.R.drawable.ic_dialog_info, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions("Show notification", "Will show notification in notification area", R.drawable.ic_notification, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions("Enable Wifi", "Enable Wifi when conditions met", R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIENABLE));
        add(new DialogOptions("Disable Wifi", "Disable Wifi when conditions met", R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIDISABLE));
        add(new DialogOptions("Enable Mobile Data", "Available for rooted phones only", R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEENABLE));
        add(new DialogOptions("Disable Mobile Data", "Available for rooted phones only", R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEDISABLE));
        add(new DialogOptions("Vibrate", "Vibrate phone when triggered", R.drawable.ic_launcher, DialogOptions.type.ACT_VIBRATE));
        add(new DialogOptions("Play Sfen", "Will make a sheep sound", R.drawable.ic_sound, DialogOptions.type.ACT_PLAYSFEN));
        add(new DialogOptions("Dialog with text", "Will show dialog with text", R.drawable.ic_dialog, DialogOptions.type.ACT_DIALOGWITHTEXT));
        add(new DialogOptions("Open application", "Will open specified application", R.drawable.ic_dialog, DialogOptions.type.ACT_OPENAPPLICATION));
        add(new DialogOptions("Open shortcut", "Will open specified shortcut", R.drawable.ic_dialog, DialogOptions.type.ACT_OPENSHORTCUT));
    }};


    public enum type {
        // conditions
        LOCATION_ENTER, LOCATION_LEAVE, WIFI_CONNECT, WIFI_DISCONNECT, TIMERANGE, TIME, DAYSOFWEEK,
        SCREEN_ON, SCREEN_OFF, CELL_IN, CELL_OUT, EVENT_RUNNING, EVENT_NOTRUNNING,
        GPS_ENABLED, GPS_DISABLED, BATTERY_LEVEL, BATTERY_STATUS,

        // actions
        ACT_NOTIFICATION, ACT_PLAYSFEN, ACT_PLAYSOUND, ACT_OPENAPPLICATION, ACT_DIALOGWITHTEXT,
        ACT_WIFIENABLE, ACT_WIFIDISABLE, ACT_MOBILEENABLE, ACT_MOBILEDISABLE,
        ACT_VIBRATE, ACT_LOCKSCREENDISABLE, ACT_LOCKSCREENENABLE, ACT_OPENSHORTCUT
    };
    private type optionType;
    private int maxNumber;
    private int uniqueID = -1;

    private HashMap<String, String> settings = new HashMap<String, String>();

    public DialogOptions() {
        super();
        if (uniqueID == -1) {
            uniqueID = new Random().nextInt(Integer.MAX_VALUE) + 1;
        }
    }

    public int getUniqueID() {
        if (uniqueID == -1) {
            uniqueID = new Random().nextInt(Integer.MAX_VALUE) + 1;
        }
        return uniqueID;
    }

    public DialogOptions(String title, String description, int icon, type optionType)
    {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.optionType = optionType;
    }
    public DialogOptions(String title, String description, int icon, type optionType, int maxNumber) {
        this(title, description, icon, optionType);
        this.maxNumber = maxNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIcon() {
        return icon;
    }

    public type getOptionType() {
        return optionType;
    }

    public HashMap<String, String> getSettings() {
        return settings;
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        if (settings.get(key) == null)
            return null;
        return settings.get(key);
    }

    public String isItemConditionOrAction() {
        String mType = this.optionType.name();

        if (mType.startsWith("ACT_"))
            return "ACTION";
        else
            return "CONDITION";

    }

    public boolean isCondition() {
        if (isItemConditionOrAction().equals("CONDITION"))
            return true;
        else
            return false;

    }

    public boolean isAction() {
        if (isItemConditionOrAction().equals("ACTION"))
            return true;
        else
            return false;

    }


}
