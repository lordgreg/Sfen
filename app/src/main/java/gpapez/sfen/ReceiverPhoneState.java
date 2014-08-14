package gpapez.sfen;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Calendar;

/**
 * Created by Gregor on 30.7.2014.
 */
public class ReceiverPhoneState extends PhoneStateListener {

    /**
     * phone riniging phone state
     * @param state
     * @param incomingNumber
     */
    public void onCallStateChanged (int state, String incomingNumber) {
        /**
         * callstatechanged VARIABLES
         */
        Vibrator v;
        Ringtone ringtone;

        /**
         * retrieve current profile
         *
         * if there is none, skip whole process
         */
        Profile profile = Profile.getActiveProfile();

        if (profile == null)
            return ;


        /**
         * get audiomanager object
         */
        AudioManager audioManager = (AudioManager)BackgroundService
                .getInstance().getSystemService(Context.AUDIO_SERVICE);


        switch (state) {

            /*
            States:
            TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.CALL_STATE_IDLE
            TelephonyManager.CALL_STATE_OFFHOOK
            */

            /**
             * Device call state: Off-hook. At least one call exists that is dialing, active,
             * or on hold, and no calls are ringing or waiting
             *
             * Here, we store setting in shared preferences that we're Offhook
             */
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Preferences.getSharedPreferences().edit().putBoolean(
                        "CALL_STATE_OFFHOOK",
                        true).apply();


                break;

            /**
             * FINISHED TALKING!
             *
             * Its idle time, we don't have any calls active. Set CALL_STATE_HOOK
             * in preferences to FALSE
             */
            case TelephonyManager.CALL_STATE_IDLE:

                Preferences.getSharedPreferences().edit().putBoolean(
                        "CALL_STATE_OFFHOOK",
                        false).apply();

                /**
                 * since we're not on the phone anymore, change default sound volume to the one
                 * set at notification.
                */
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        profile.getVolumeNotification(),
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


                break;

            /**
             * RRRRRRRRRRRRIIIIIIIIIIIIIINNNNNNNNNNNNGGGGGGGGGGGGG
             *
             * when phone is ringing, check our currently active profile and quickly set
             * desired options!
             */
            case TelephonyManager.CALL_STATE_RINGING:

                /**
                 * if we already have another call active, stop executing actions
                 */
                if (Preferences.getSharedPreferences().getBoolean("CALL_STATE_OFFHOOK", false)) {
                    break ;
                }

                /**
                 * set ringer mode first, depending on our profile decisions
                 */
                if (profile.getVolumeRingtone() == 0 && !profile.isVibrate())
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                else if (profile.getVolumeRingtone() == 0 && profile.isVibrate())
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

                else
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);


                /**
                 * set loudness-
                 */
                audioManager.setStreamVolume(
                        AudioManager.STREAM_RING,
                        profile.getVolumeRingtone(),
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


                break;
        }

    }


    public void onCellLocationChanged(CellLocation cellLocation){
        super.onCellLocationChanged(cellLocation);

        /**
         *
         * if new location doesn't throw any errors, continue, otherwise, stop actions
         *
         */
        String cellId = "";

        CellConnectionInfo cellInfo = new CellConnectionInfo(Main.getInstance());

        if (cellInfo.isError()) {
//            Log.d("sfen", "No mobile: "+ cellInfo.getError());
            return ;
        }

        else
            cellId = cellInfo.getCellId();


//        Log.d("sfen", "Got mobile cell: "+ cellInfo.getCellId());

        // when we change cells, we have to wake up to send broadcast
        PowerManager pm = (PowerManager) BackgroundService.getInstance()
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sfen");
        mWakeLock.acquire();


        /**
         * retrieve permanent info
         */
        boolean isRecordingPermanent =
                Preferences
                        .getSharedPreferences().getBoolean("CellRecordPermanent", false);




        /**
         * permanent recording saves all cells
         */
        if (isRecordingPermanent) {

            Cell.addCellIdToArray(cellId);

        }

        /**
         * permanent is disabled, then check times
         */
        else {


            /**
             * saving new cell id to preferences?
             */
            Calendar calendar = Calendar.getInstance();
            Calendar calendarUntil = null;
            try {
                Gson gson = new Gson();
                calendarUntil = gson.fromJson(
                        Preferences
                                .getSharedPreferences().getString("CellRecordUntil", null),
                        Calendar.class
                );
            } catch (Exception e) {
            }

            /**
             * is saved date there?
             */
            if (calendarUntil != null) {

                /**
                 * save new id into array IF save time meets conditions
                 */
                if (calendarUntil.after(calendar)) {

                    Cell.addCellIdToArray(cellId);


                }

                /**
                 * if until date did already passed current date, clear it from settings
                 */
                else {

                    // update date in preferences with empty string
                    BackgroundService.getInstance().mPreferences.setPreferences(
                            "CellRecordUntil", new String()
                    );

                }

            }

        }


        /**
         * send broadcast when CELL LOCATION CHANGES
         */
        BackgroundService.getInstance().sendBroadcast("CELL_LOCATION_CHANGED");

        // close down the wakelock
        mWakeLock.release();

    }

}
