package gpapez.sfen;

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
    public enum type {
        // conditions
        LOCATION_ENTER, LOCATION_LEAVE, WIFI_CONNECT, WIFI_DISCONNECT, TIMERANGE, DAYSOFWEEK,
        SCREEN_ON, SCREEN_OFF,

        // actions
        ACT_NOTIFICATION, ACT_PLAYSOUND, ACT_OPENAPPLICATION, ACT_DIALOGWITHTEXT,
        ACT_WIFIENABLE, ACT_WIFIDISABLE
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
