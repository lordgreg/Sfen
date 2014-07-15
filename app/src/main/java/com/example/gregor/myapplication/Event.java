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
    private boolean enabled;
    private boolean running;
    private boolean matchAllConditions;
    private boolean forceRun = false;

    public boolean isMatchAllConditions() {
        return matchAllConditions;
    }

    public void setMatchAllConditions(boolean matchAllConditions) {
        this.matchAllConditions = matchAllConditions;
    }

    private ArrayList<DialogOptions> conditions = new ArrayList<DialogOptions>();
    private ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();

    private HashMap<String, String> settings;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Event() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setSettings(HashMap<String, String>settings) {
        this.settings = settings;
    }

    public HashMap<String, String>getSettings() {
        return settings;
    }
}
