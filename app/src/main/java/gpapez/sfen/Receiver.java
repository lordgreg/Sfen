package gpapez.sfen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Gregor on 14.7.2014.
 */
public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BackgroundService.getInstance().receiverAction = action;
        PowerManager.WakeLock mWakeLock = null;

        boolean mCallBroadcast = true;
        Log.i("sfen", "received: " + action);

        /**
         * For all possible broadcasts, we have to check if we have any Event
         * that matches all the conditions, right-io?
         */
        if (action.equals(getClass().getPackage().getName() +".ALARM_TRIGGER")) {

            // when triggering for alarm, we have to use wakelock so it wakes
            // up the device and calls eventfinder
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sfen");
            mWakeLock.acquire();

        }

        //if (action.equals(WifiManager.EXTRA_SUPPLICANT_CONNECTED)) {
        //if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();

            //System.out.println("*** WIFI STATE: "+ state);

            if (state == NetworkInfo.State.CONNECTING ||
                    state == NetworkInfo.State.DISCONNECTING ||
                    state == NetworkInfo.State.SUSPENDED ||
                    state == NetworkInfo.State.UNKNOWN
                    ) {
                mCallBroadcast = false;
            }

            if (state == NetworkInfo.State.CONNECTED) {
                // save latest connected SSID
                BackgroundService.getInstance().mLatestSSID =
                        manager.getConnectionInfo().getSSID().replace("\"", "");
            }

        }


        // RUN OUR FUNCTION
        if (mCallBroadcast)
            BackgroundService.getInstance().EventFinder(context, intent);

        // release wakelock if set
        if (mWakeLock != null)
            mWakeLock.release();
    }

}
