package gpapez.sfen;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;

/**
 * Notification for notification bar
 *
 *
 * Created by Gregor on 1.8.2014.
 */
public class Notification {

    /**
     * this is the ID which notification will always need.
     */
    private static final int APPLICATION_ID =
            1337;

    private static final int NOTIFICATION_FLAGS =
            android.app.Notification.FLAG_NO_CLEAR;

    /**
     * variables for all-around functions inside class
     */
    private Service mService;
    private Notification sInstance;
    private NotificationManager mNotificationManager;
    private PendingIntent mPendingIntent;

    /**
     * variables that will be passed when showing notification
     */
    private String mTitle;
    private String mDescription;
    private int mIcon;
    private ArrayList<String> mRunningEvents = new ArrayList<String>();



    /**
     * CONSTRUCTOR
     */
    public Notification(Service service) {
        resetInformation();
        mService = service;
    }
    public Notification() {
        resetInformation();
        mService = BackgroundService.getInstance();
    }

    /**
     * DESTRUCTOR
     */
    public void Destroy() {
        mNotificationManager.cancel(APPLICATION_ID);

        mPendingIntent.cancel();
    }

    /**
     * SINGLETON INSTANCE
     *
     * Singleton function that returns the current instance of our class
     * if it does not exist, it creates new instance.
     * @return instance of current class
     */
    public Notification getInstance() {
        if (sInstance == null) {
            return new Notification();
        }
        else
            return sInstance;
    }

    /**
     * get pendingintent or if null, create new one!
     */
    protected PendingIntent getPendingIntent() {
        if (mPendingIntent != null)
            return mPendingIntent;
        else {

            mPendingIntent = PendingIntent.getActivity(
                    mService,
                    0,
                    new Intent(mService, Main.class),
                    0
            );

            return mPendingIntent;

        }
    }




    /**
     * create/update Notification
     *
     * @param title       is the title of notification
     * @param description is the description (bottom line)
     * @param icon        well, its obvious, isn't it? o_O
     */
    protected void showNotification(String title, String description, int icon) {

        if (!title.equals("Sfen"))
            title = "Sfen - "+ title;


        /**
         * start NotificationManager
         */
        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) Main.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        }

        /**
         * android version dependent call!
         */
        android.app.Notification note;

        // JELLY_BEAN+
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
/*
            note = new android.app.Notification.Builder(BackgroundService.getInstance())
                    .setContentTitle(title)
                    .setContentText(description)
                    .setSmallIcon(icon).build();
*/

            /**
             * if we have more than one running events, change description
             */
            //System.out.println(mRunningEvents.toString() +" running events "+ mRunningEvents.size());

            if (mRunningEvents.size() == 0) {
                description = "No running events.";
            }
            else if (mRunningEvents.size() == 1) {
                description = description;
            }
            else if (mRunningEvents.size() > 1) {
                description = mRunningEvents.size() +" running events.";
            }



            //PendingIntent pi = getPendingIntent();
            android.app.Notification.Builder builder = new android.app.Notification.Builder(BackgroundService.getInstance())
                    .setContentTitle(title)
                    .setContentText(description)
                    .setTicker(title)
                    .setSmallIcon(icon)
                    .addAction(android.R.drawable.ic_menu_directions, "Open Sfen", getPendingIntent())
                    .setContentIntent(getPendingIntent())

                    ;

            note = new android.app.Notification.BigTextStyle(builder)
                    .bigText(showStringFromArray())
                    //.setSummaryText("Out of total "+ Main.getInstance().events.size() +" events.")
                    .build();



        }
        // LESS than JB
        else {
            note = new android.app.Notification(icon, title, System.currentTimeMillis());

            note.setLatestEventInfo(mService, title, description, getPendingIntent());
        }


        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        note.flags |= NOTIFICATION_FLAGS;



        mService.startForeground(APPLICATION_ID, note);

    }

    protected void showNotification() {
        prepareData();
        showNotification(this.mTitle, this.mDescription, this.mIcon);
    }


    /**
     * resets fields title, description and icon
     */
    protected void resetInformation() {
        mTitle = null;
        mDescription = null;
        mIcon = -1;
        mRunningEvents.clear();
    }

    /**
     * check if notification variables are full or empty.
     */
    protected boolean hasStoredData() {
        if (mTitle == null && mDescription == null && mIcon == -1)
            return false;
        else
            return true;
    }

    /**
     * prepares data if it is empty
     */
    private void prepareData() {
        if (!hasStoredData()) {
            mTitle = "Sfen";
            mDescription = "";
            mIcon = R.drawable.ic_launcher;
        }

    }

    /**
     * add running event to list
     */
    protected void addEventToList(String event) {
        if (!mRunningEvents.contains(event)) {
            mRunningEvents.add(event);
        }
    }


    /**
     * remove running event from list
     */
    protected void removeEventFromList(String event) {
        if (mRunningEvents.contains(event)) {
            mRunningEvents.remove(event);
        }

    }

    /**
     * sets data with one method
     */
    protected void saveData(String title, String description, int icon) {
        mTitle = title;
        mDescription = description;
        mIcon = icon;
        //System.out.println("saved data: "+ mTitle +", "+ mDescription +", "+ mIcon);
    }

    /**
     * create string with newlines from array of enabled Events
     */
    private String showStringFromArray() {
        String output = "";
        for (String single : mRunningEvents) {
            output += single +"\n";
        }

        if (mRunningEvents.size() == 0)
            output = "There are currently no Events running.";
//         if (mRunningEvents.size() <= 1)
//            output = "";

        //System.out.println("*** OUTPUT: "+ output);
        return output;
    }


    /**
     * setter before we show the notification
     */
    @Deprecated
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    @Deprecated
    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    @Deprecated
    public void setIcon(int mIcon) {
        this.mIcon = mIcon;
    }
}
