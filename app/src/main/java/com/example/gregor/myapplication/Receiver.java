package com.example.gregor.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by Gregor on 14.7.2014.
 */
public class Receiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BackgroundService.getInstance().receiverAction = action;

        Log.e("broadcast", "RECIEVED: "+ action);

        /**
         * For all possible broadcasts, we have to check if we have any Event
         * that matches all the conditions, right-io?
         */

        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();


            // CONNECTED to WIFI?
            //System.out.println("**************** "+ state.toString());

            // other options: CONNECTING, DISCONNECTED
            if (state == NetworkInfo.State.CONNECTED) {
                //System.out.println("connected to wifi.");

                // save latest connected SSID
                BackgroundService.getInstance().mLatestSSID =
                        manager.getConnectionInfo().getSSID().replace("\"", "");

/*
                Log.e("broadcast", action);
                //Log.e("broadcast", WifiManager.EXTRA_SUPPLICANT_CONNECTED +", "+ WifiManager.EXTRA_NETWORK_INFO +", "+ WifiManager.EXTRA_WIFI_INFO +", "+ WifiManager.EXTRA_BSSID);

                //context.getSystemService()
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //String ssid = wifiInfo.toString();
                //Log.e("broadcast", "Connected to "+ ssid);
                */
            }
            //else {
                //Main.getInstance().setNotification("Broadcast Recieved", "Disconnected from WiFi", R.drawable.ic_launcher);
            //    System.out.println("disconnected from wifi...");
           // }



        }


        // RUN OUR FUNCTION
        BackgroundService.getInstance().EventFinder(context, intent);
    }


}
