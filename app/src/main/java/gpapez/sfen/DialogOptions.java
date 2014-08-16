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
    private String icon;

    // list of possible Conditions in Options
    static final ArrayList<DialogOptions> optConditions = new ArrayList<DialogOptions>() {{
        add(new DialogOptions(Main.getInstance().getString(R.string.inside_location), Main.getInstance().getString(R.string.inside_location_description), R.drawable.ic_map, DialogOptions.type.LOCATION_ENTER));
        add(new DialogOptions(Main.getInstance().getString(R.string.outside_location), Main.getInstance().getString(R.string.outside_location_description), R.drawable.ic_map, DialogOptions.type.LOCATION_LEAVE));
        add(new DialogOptions(Main.getInstance().getString(R.string.time_range), Main.getInstance().getString(R.string.time_range_description), R.drawable.ic_time, DialogOptions.type.TIMERANGE));
        add(new DialogOptions(Main.getInstance().getString(R.string.specific_time), Main.getInstance().getString(R.string.specific_time_description), R.drawable.ic_time, DialogOptions.type.TIME));
        add(new DialogOptions(Main.getInstance().getString(R.string.days), Main.getInstance().getString(R.string.days_of_week_description), R.drawable.ic_date, DialogOptions.type.DAYSOFWEEK));
        add(new DialogOptions(Main.getInstance().getString(R.string.connected_to_wifi), Main.getInstance().getString(R.string.connected_to_wifi_description), R.drawable.ic_wifi, DialogOptions.type.WIFI_CONNECT));
        add(new DialogOptions(Main.getInstance().getString(R.string.disconnected_from_wifi), Main.getInstance().getString(R.string.disconnected_from_wifi_description), R.drawable.ic_wifi, DialogOptions.type.WIFI_DISCONNECT));
        add(new DialogOptions(Main.getInstance().getString(R.string.screen_on), Main.getInstance().getString(R.string.screen_on_description), R.drawable.ic_screen, DialogOptions.type.SCREEN_ON));
        add(new DialogOptions(Main.getInstance().getString(R.string.screen_off), Main.getInstance().getString(R.string.screen_off_description), R.drawable.ic_screen, DialogOptions.type.SCREEN_OFF));
        add(new DialogOptions(Main.getInstance().getString(R.string.connected_to_cells), Main.getInstance().getString(R.string.connected_to_cells_description), R.drawable.ic_cell, DialogOptions.type.CELL_IN));
        add(new DialogOptions(Main.getInstance().getString(R.string.not_connected_to_cells), Main.getInstance().getString(R.string.not_connected_to_cells_description), R.drawable.ic_cell, DialogOptions.type.CELL_OUT));
        add(new DialogOptions(Main.getInstance().getString(R.string.event_running), Main.getInstance().getString(R.string.event_running_description), R.drawable.ic_launcher, DialogOptions.type.EVENT_RUNNING));
        add(new DialogOptions(Main.getInstance().getString(R.string.event_not_running), Main.getInstance().getString(R.string.event_not_running_description), R.drawable.ic_launcher, DialogOptions.type.EVENT_NOTRUNNING));
        add(new DialogOptions(Main.getInstance().getString(R.string.gps_enabled), Main.getInstance().getString(R.string.gps_enabled_description), R.drawable.ic_map, DialogOptions.type.GPS_ENABLED));
        add(new DialogOptions(Main.getInstance().getString(R.string.gps_disabled), Main.getInstance().getString(R.string.gps_disabled_description), R.drawable.ic_map, DialogOptions.type.GPS_DISABLED));
        add(new DialogOptions(Main.getInstance().getString(R.string.battery_level), Main.getInstance().getString(R.string.battery_level_description), R.drawable.ic_battery, DialogOptions.type.BATTERY_LEVEL));
        add(new DialogOptions(Main.getInstance().getString(R.string.battery_status), Main.getInstance().getString(R.string.battery_status_description), R.drawable.ic_battery, DialogOptions.type.BATTERY_STATUS));
        add(new DialogOptions(Main.getInstance().getString(R.string.bluetooth_on), Main.getInstance().getString(R.string.bluetooth_on_description), R.drawable.ic_bluetooth, type.BLUETOOTH_ON));
        add(new DialogOptions(Main.getInstance().getString(R.string.bluetooth_off), Main.getInstance().getString(R.string.bluetooth_off_description), R.drawable.ic_bluetooth, type.BLUETOOTH_OFF));
        add(new DialogOptions(Main.getInstance().getString(R.string.headset_connected), Main.getInstance().getString(R.string.headset_connected_description), R.drawable.ic_headset, type.HEADSET_CONNECTED));
        add(new DialogOptions(Main.getInstance().getString(R.string.headset_disconnected), Main.getInstance().getString(R.string.headset_disconnected_description), R.drawable.ic_headset, type.HEADSET_DISCONNECTED));


    }};



    // list of possible Actions in Options
    //context.getResources().getDrawable(R.drawable.ic_launcher)
    static final ArrayList<DialogOptions> optActions = new ArrayList<DialogOptions>() {{
        //add(new DialogOptions("Show notification", "Will show notification in notification area", android.R.drawable.ic_dialog_info, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions(Main.getInstance().getString(R.string.show_notification), Main.getInstance().getString(R.string.show_notification_description), R.drawable.ic_notification, DialogOptions.type.ACT_NOTIFICATION));
        add(new DialogOptions(Main.getInstance().getString(R.string.enable_wifi), Main.getInstance().getString(R.string.enable_wifi_description), R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIENABLE));
        add(new DialogOptions(Main.getInstance().getString(R.string.disable_wifi), Main.getInstance().getString(R.string.disable_wifi_description), R.drawable.ic_wifi, DialogOptions.type.ACT_WIFIDISABLE));
        add(new DialogOptions(Main.getInstance().getString(R.string.enable_mobile_data), Main.getInstance().getString(R.string.enable_mobile_data_description), R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEENABLE));
        add(new DialogOptions(Main.getInstance().getString(R.string.disable_mobile_data), Main.getInstance().getString(R.string.disable_mobile_data_description), R.drawable.ic_mobiledata, DialogOptions.type.ACT_MOBILEDISABLE));
        add(new DialogOptions(Main.getInstance().getString(R.string.vibrate), Main.getInstance().getString(R.string.vibrate_description), R.drawable.ic_launcher, DialogOptions.type.ACT_VIBRATE));
        add(new DialogOptions(Main.getInstance().getString(R.string.play_sfen), Main.getInstance().getString(R.string.play_sfen_description), R.drawable.ic_sound, DialogOptions.type.ACT_PLAYSFEN));
        add(new DialogOptions(Main.getInstance().getString(R.string.dialog_with_text), Main.getInstance().getString(R.string.dialog_with_text_description), R.drawable.ic_dialog, DialogOptions.type.ACT_DIALOGWITHTEXT));
        add(new DialogOptions(Main.getInstance().getString(R.string.open_application), Main.getInstance().getString(R.string.open_application_description), R.drawable.ic_dialog, DialogOptions.type.ACT_OPENAPPLICATION));
        add(new DialogOptions(Main.getInstance().getString(R.string.open_shortcut), Main.getInstance().getString(R.string.open_shortcut_description), R.drawable.ic_dialog, DialogOptions.type.ACT_OPENSHORTCUT));
        add(new DialogOptions(Main.getInstance().getString(R.string.run_event), Main.getInstance().getString(R.string.run_event_description), R.drawable.ic_event, DialogOptions.type.ACT_RUNEVENT));
    }};


    public enum type {
        // conditions
        LOCATION_ENTER, LOCATION_LEAVE, WIFI_CONNECT, WIFI_DISCONNECT, TIMERANGE, TIME, DAYSOFWEEK,
        SCREEN_ON, SCREEN_OFF, CELL_IN, CELL_OUT, EVENT_RUNNING, EVENT_NOTRUNNING,
        GPS_ENABLED, GPS_DISABLED, BATTERY_LEVEL, BATTERY_STATUS, BLUETOOTH_ON, BLUETOOTH_OFF,
        HEADSET_CONNECTED, HEADSET_DISCONNECTED,

        // actions
        ACT_NOTIFICATION, ACT_PLAYSFEN, ACT_PLAYSOUND, ACT_OPENAPPLICATION, ACT_DIALOGWITHTEXT,
        ACT_WIFIENABLE, ACT_WIFIDISABLE, ACT_MOBILEENABLE, ACT_MOBILEDISABLE,
        ACT_VIBRATE, ACT_LOCKSCREENDISABLE, ACT_LOCKSCREENENABLE, ACT_OPENSHORTCUT,
        ACT_RUNEVENT
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
