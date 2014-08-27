package gpapez.sfen;

import android.content.Context;

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
    private String icon;


    protected static ArrayList<DialogOptions> optConditions(final Context context) {

        // list of possible Conditions in Options
        return new ArrayList<DialogOptions>() {{
            add(new DialogOptions(context.getString(R.string.inside_location), context.getString(R.string.inside_location_description), R.drawable.ic_map, DialogOptions.type.LOCATION_ENTER));
            add(new DialogOptions(context.getString(R.string.outside_location), context.getString(R.string.outside_location_description), R.drawable.ic_map, DialogOptions.type.LOCATION_LEAVE));
            add(new DialogOptions(context.getString(R.string.time_range), context.getString(R.string.time_range_description), R.drawable.ic_time, DialogOptions.type.TIMERANGE));
            add(new DialogOptions(context.getString(R.string.specific_time), context.getString(R.string.specific_time_description), R.drawable.ic_time, DialogOptions.type.TIME));
            add(new DialogOptions(context.getString(R.string.days), context.getString(R.string.days_of_week_description), R.drawable.ic_date, DialogOptions.type.DAYSOFWEEK));
            add(new DialogOptions(context.getString(R.string.connected_to_wifi), context.getString(R.string.connected_to_wifi_description), R.drawable.ic_wifi, DialogOptions.type.WIFI_CONNECT));
            add(new DialogOptions(context.getString(R.string.disconnected_from_wifi), context.getString(R.string.disconnected_from_wifi_description), R.drawable.ic_wifi, DialogOptions.type.WIFI_DISCONNECT));
            add(new DialogOptions(context.getString(R.string.screen_on), context.getString(R.string.screen_on_description), R.drawable.ic_screen, DialogOptions.type.SCREEN_ON));
            add(new DialogOptions(context.getString(R.string.screen_off), context.getString(R.string.screen_off_description), R.drawable.ic_screen, DialogOptions.type.SCREEN_OFF));
            add(new DialogOptions(context.getString(R.string.connected_to_cells), context.getString(R.string.connected_to_cells_description), R.drawable.ic_cell, DialogOptions.type.CELL_IN));
            add(new DialogOptions(context.getString(R.string.not_connected_to_cells), context.getString(R.string.not_connected_to_cells_description), R.drawable.ic_cell, DialogOptions.type.CELL_OUT));
            add(new DialogOptions(context.getString(R.string.event_running), context.getString(R.string.event_running_description), R.drawable.ic_launcher, DialogOptions.type.EVENT_RUNNING));
            add(new DialogOptions(context.getString(R.string.event_not_running), context.getString(R.string.event_not_running_description), R.drawable.ic_launcher, DialogOptions.type.EVENT_NOTRUNNING));
            add(new DialogOptions(context.getString(R.string.event_conditions_true), context.getString(R.string.event_conditions_true_description), R.drawable.ic_launcher, DialogOptions.type.EVENT_CONDITIONS_TRUE));
            add(new DialogOptions(context.getString(R.string.event_conditions_false), context.getString(R.string.event_conditions_false_description), R.drawable.ic_launcher, type.EVENT_CONDITIONS_FALSE));
            add(new DialogOptions(context.getString(R.string.gps_enabled), context.getString(R.string.gps_enabled_description), R.drawable.ic_map, DialogOptions.type.GPS_ENABLED));
            add(new DialogOptions(context.getString(R.string.gps_disabled), context.getString(R.string.gps_disabled_description), R.drawable.ic_map, DialogOptions.type.GPS_DISABLED));
            add(new DialogOptions(context.getString(R.string.battery_level), context.getString(R.string.battery_level_description), R.drawable.ic_battery, DialogOptions.type.BATTERY_LEVEL));
            add(new DialogOptions(context.getString(R.string.battery_status), context.getString(R.string.battery_status_description), R.drawable.ic_battery, DialogOptions.type.BATTERY_STATUS));
            add(new DialogOptions(context.getString(R.string.bluetooth_connected), context.getString(R.string.bluetooth_connected_description), R.drawable.ic_bluetooth, type.BLUETOOTH_CONNECTED));
            add(new DialogOptions(context.getString(R.string.bluetooth_disconnected), context.getString(R.string.bluetooth_disconnected_description), R.drawable.ic_bluetooth, type.BLUETOOTH_DISCONNECTED));
            add(new DialogOptions(context.getString(R.string.headset_connected), context.getString(R.string.headset_connected_description), R.drawable.ic_headset, type.HEADSET_CONNECTED));
            add(new DialogOptions(context.getString(R.string.headset_disconnected), context.getString(R.string.headset_disconnected_description), R.drawable.ic_headset, type.HEADSET_DISCONNECTED));


        }};

    }

    // list of possible Actions in Options
    //context.getResources().getDrawable(R.drawable.ic_launcher)


    protected static ArrayList<DialogOptions> optActions(final Context context) {

        return new ArrayList<DialogOptions>() {{
            //add(new DialogOptions("Show notification", "Will show notification in notification area", android.R.drawable.ic_dialog_info, DialogOptions.type.ACT_NOTIFICATION));
            add(new DialogOptions(context.getString(R.string.show_notification), context.getString(R.string.show_notification_description), R.drawable.ic_notification, DialogOptions.type.ACT_NOTIFICATION));
            add(new DialogOptions(context.getString(R.string.enable_wifi), context.getString(R.string.enable_wifi_description), R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIENABLE));
            add(new DialogOptions(context.getString(R.string.disable_wifi), context.getString(R.string.disable_wifi_description), R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIDISABLE));
            add(new DialogOptions(context.getString(R.string.enable_mobile_data), context.getString(R.string.enable_mobile_data_description), R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEENABLE));
            add(new DialogOptions(context.getString(R.string.disable_mobile_data), context.getString(R.string.disable_mobile_data_description), R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEDISABLE));
            add(new DialogOptions(context.getString(R.string.vibrate), context.getString(R.string.vibrate_description), R.drawable.ic_launcher, DialogOptions.type.ACT_VIBRATE));
            add(new DialogOptions(context.getString(R.string.play_sfen), context.getString(R.string.play_sfen_description), R.drawable.ic_sound, DialogOptions.type.ACT_PLAYSFEN));
            add(new DialogOptions(context.getString(R.string.dialog_with_text), context.getString(R.string.dialog_with_text_description), R.drawable.ic_dialog, DialogOptions.type.ACT_DIALOGWITHTEXT));
            add(new DialogOptions(context.getString(R.string.open_application), context.getString(R.string.open_application_description), R.drawable.ic_dialog, DialogOptions.type.ACT_OPENAPPLICATION));
            add(new DialogOptions(context.getString(R.string.open_shortcut), context.getString(R.string.open_shortcut_description), R.drawable.ic_dialog, DialogOptions.type.ACT_OPENSHORTCUT));
            add(new DialogOptions(context.getString(R.string.run_event), context.getString(R.string.run_event_description), R.drawable.ic_event, DialogOptions.type.ACT_RUNEVENT));
            add(new DialogOptions(context.getString(R.string.run_script), context.getString(R.string.run_script_description), R.drawable.ic_dialog, type.ACT_RUNSCRIPT));
        }};

    }


    public enum type {
        // conditions
        LOCATION_ENTER, LOCATION_LEAVE, WIFI_CONNECT, WIFI_DISCONNECT, TIMERANGE, TIME, DAYSOFWEEK,
        SCREEN_ON, SCREEN_OFF, CELL_IN, CELL_OUT, EVENT_RUNNING, EVENT_NOTRUNNING,
        GPS_ENABLED, GPS_DISABLED, BATTERY_LEVEL, BATTERY_STATUS, BLUETOOTH_CONNECTED,
        BLUETOOTH_DISCONNECTED, HEADSET_CONNECTED, HEADSET_DISCONNECTED,
        EVENT_CONDITIONS_TRUE, EVENT_CONDITIONS_FALSE,

        // actions
        ACT_NOTIFICATION, ACT_PLAYSFEN, ACT_PLAYSOUND, ACT_OPENAPPLICATION, ACT_DIALOGWITHTEXT,
        ACT_WIFIENABLE, ACT_WIFIDISABLE, ACT_MOBILEENABLE, ACT_MOBILEDISABLE,
        ACT_VIBRATE, ACT_LOCKSCREENDISABLE, ACT_LOCKSCREENENABLE, ACT_OPENSHORTCUT,
        ACT_RUNEVENT, ACT_RUNSCRIPT
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

    public DialogOptions(String title, String description, int icon, type optionType) {
        this.title = title;
        this.description = description;
        this.icon = Main.getInstance().getResources().getResourceEntryName(icon);
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
        return BackgroundService.getInstance().getResources()
                .getIdentifier(
                        icon,
                        "drawable",
                        BackgroundService.getInstance().getPackageName());

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
