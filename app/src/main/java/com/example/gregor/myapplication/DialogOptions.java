package com.example.gregor.myapplication;

import java.util.HashMap;

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
    public enum type {LOCATION_ENTER, LOCATION_LEAVE, WIFI_CONNECT, WIFI_DISCONNECT, TIMERANGE, DAYSOFWEEK};
    private type optionType;
    private int maxNumber;
    private HashMap<String, String> settings;

    public DialogOptions() {
        super();
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
        return settings.get(key);
    }
}
