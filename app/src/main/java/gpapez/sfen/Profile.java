package gpapez.sfen;

import android.net.Uri;

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
    private int brightnessValue;
    private boolean brightnessAuto = true;
    private String ringtone = "";
    private String notification = "";
    private int volumeRingtone = 80;
    private int volumeNotification = 80;
    private int volumeMusic = 80;
    private int volumeAlarm = 80;




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

//        /**
//         * set brightness to automode
//         */
//        brightnessAuto = true;
//
//        /**
//         * set default volumes
//         */
//        volumeRingtone = 80;
//        volumeNotification = 80;
//        volumeMusic = 80;
//        volumeAlarm = 80;

    }

    /**
     * function updates flags for active/non-active profiles
     * where id should be set to active.
     *
     * @param id
     */
    public static void updateActiveProfile(int id) {
        Profile p;

        for (int i = 0; i < BackgroundService.getInstance().profiles.size(); i++) {
            p = BackgroundService.getInstance().profiles.get(i);

            /**
             * found our match
             */
            if (p.getUniqueID() == id) {

                BackgroundService.getInstance().profiles.get(i).setActive(true);

            }

            /**
             * deactivate all others profiles
             */
            else {

                BackgroundService.getInstance().profiles.get(i).setActive(false);

            }

        }

    }


    /**
     * GetActiveProfile returns currently active profile.
     *
     * @return currently active profile
     */
    public static Profile getActiveProfile() {

        for (Profile p : BackgroundService.getInstance().profiles) {
            if (p.isActive())
                return p;
        }


        return null;

    }


    /**
     * returns Profile by Unique ID
     */
    public static Profile getProfileByUniqueID(int uniqueID) {

        for (Profile single : BackgroundService.getInstance().profiles) {

            if (single.getUniqueID() == uniqueID)
                return single;

        }

        return null;
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

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public int getBrightnessValue() {
        return brightnessValue;
    }

    public void setBrightnessValue(int brightnessValue) {
        this.brightnessValue = brightnessValue;
    }

    public boolean isBrightnessAuto() {
        return brightnessAuto;
    }

    public void setBrightnessAuto(boolean brightnessAuto) {
        this.brightnessAuto = brightnessAuto;
    }

    public Uri getRingtone() {
        if (ringtone.equals(""))
            return null;

        return Uri.parse(ringtone);
    }

    public void setRingtone(Uri ringtone) {
        this.ringtone = ringtone.toString();
    }

    public Uri getNotification() {
        if (notification.equals(""))
            return null;

        return Uri.parse(notification);
    }

    public void setNotification(Uri notification) {
        this.notification = notification.toString();
    }

    public int getVolumeAlarm() {
        return volumeAlarm;
    }

    public void setVolumeAlarm(int volumeAlarm) {
        this.volumeAlarm = volumeAlarm;
    }

    public int getVolumeMusic() {
        return volumeMusic;
    }

    public void setVolumeMusic(int volumeMusic) {
        this.volumeMusic = volumeMusic;
    }

    public int getVolumeNotification() {
        return volumeNotification;
    }

    public void setVolumeNotification(int volumeNotification) {
        this.volumeNotification = volumeNotification;
    }

    public int getVolumeRingtone() {
        return volumeRingtone;
    }

    public void setVolumeRingtone(int volumeRingtone) {
        this.volumeRingtone = volumeRingtone;
    }


    /**
     * resets unique id
     * (used when importing events and profiles)
     */
    public void resetUniqueId() {

        uniqueID = Math.abs(new Random().nextInt());

    }
}
