package com.example.gregor.myapplication;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for our event which will include
 * name,
 * status (enabled/disabled),
 * array of Conditions
 * array of Actions
 *
 * Created by Gregor on 10.7.2014.
 */
public class Event {
    private String name;
    private boolean status;

    private ArrayList<DialogOptions> conditions;
    private ArrayList<DialogOptions> actions;

    private HashMap<String, String> settings;

    public Event() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public ArrayList<DialogOptions> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<DialogOptions> conditions) {
        this.conditions = conditions;
    }

    public ArrayList<DialogOptions> getActions() {
        return actions;
    }

    public void setActions(ArrayList<DialogOptions> actions) {
        this.actions = actions;
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        return settings.get(key);
    }
}
