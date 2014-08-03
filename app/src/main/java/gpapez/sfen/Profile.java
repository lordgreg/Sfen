package gpapez.sfen;

import java.util.ArrayList;
import java.util.Random;

/**
 * PROFILE CLASS
 */
public class Profile {

    /**
     *
     * VARIABLES
     *
     */
    private String name;
    private int icon;
    private int uniqueID;
    private boolean isActive;

    private ArrayList<DialogOptions> actions = new ArrayList<DialogOptions>();

    /**
     * SETTING VARIABLES
     */
    private boolean isVibrate;



    /**
     *
     * CONSTRUCTOR
     *
     */
    public Profile() {

        /**
         * creating unique id for current profile
         */
        uniqueID = Math.abs(new Random().nextInt());

        /**
         * set profile to not running
         */
        isActive = false;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DialogOptions> getActions() {
        return actions;
    }

    public void setActions(ArrayList<DialogOptions> actions) {
        this.actions = actions;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public void setVibrate(boolean isVibrate) {
        this.isVibrate = isVibrate;
    }
}
