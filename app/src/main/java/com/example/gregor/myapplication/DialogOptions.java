package com.example.gregor.myapplication;

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
    public enum type {CONDITION, ACTION};
    private type optionType;

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
}
