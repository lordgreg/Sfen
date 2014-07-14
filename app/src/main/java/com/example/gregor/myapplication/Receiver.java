package com.example.gregor.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Gregor on 14.7.2014.
 */
public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        /**
         * For all possible broadcasts, we have to check if we have any Event
         * that matches all the conditions, right-io?
         */
        if (action.equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {

            // CONNECTED to WIFI?
            if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true)) {

                Log.e("broadcast", action);
                //Log.e("broadcast", WifiManager.EXTRA_SUPPLICANT_CONNECTED +", "+ WifiManager.EXTRA_NETWORK_INFO +", "+ WifiManager.EXTRA_WIFI_INFO +", "+ WifiManager.EXTRA_BSSID);

                //context.getSystemService()
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //String ssid = wifiInfo.toString();
                //Log.e("broadcast", "Connected to "+ ssid);
            }
            else {
                Main.getInstance().setNotification("Broadcast Recieved", "Disconnected from WiFi", R.drawable.ic_launcher);
            }



        }

    }
}
