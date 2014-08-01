package gpapez.sfen;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

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


    /**
     * CONSTRUCTOR
     */
    public Notification(Service service) {
        mService = service;
    }
    public Notification() {
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

        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        //NotificationManager mNM = Main.getInstance().mNM;
        android.app.Notification note = new android.app.Notification(icon, title, System.currentTimeMillis());

        note.setLatestEventInfo(mService, title, description, getPendingIntent());
        note.flags |= NOTIFICATION_FLAGS;

        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) Main.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
            //mNM.notify(1337, note);
        }

        //mNM.notify(1337, note);

        //context.getApplicationContext().st
        mService.startForeground(APPLICATION_ID, note);

    }

    protected void showNotification() {
        showNotification(mTitle, mDescription, mIcon);
    }


    /**
     * resets fields title, description and icon
     */
    protected void resetInformation() {
        mTitle = null;
        mDescription = null;
        mIcon = -1;
    }


    /**
     * setter before we show the notification
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public void setIcon(int mIcon) {
        this.mIcon = mIcon;
    }
}
