package gpapez.sfen;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.os.PowerManager;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

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


    public void onCellLocationChanged(GsmCellLocation CellId){
        super.onCellLocationChanged(CellId);


        // when we change cells, we have to wake up to send broadcast
        PowerManager pm = (PowerManager) BackgroundService.getInstance()
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sfen");
        mWakeLock.acquire();


        /**
         * send broadcast when CELL LOCATION CHANGES
         */
        BackgroundService.getInstance().sendBroadcast("CELL_LOCATION_CHANGED");

        // close down the wakelock
        mWakeLock.release();

    }

}
