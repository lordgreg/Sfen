package com.example.gregor.myapplication;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

/**
 * Created by Gregor on 18.7.2014.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();



        // Give it the category for all intents sent by the Intent Service
        //broadcastIntent.addCategory("com.example.gregor.myapplication.CATEGORY_LOCATION_SERVICES");
        // Give it the category for all intents sent by the Intent Service

        //broadcastIntent.addCategory(CATEGORY_LOCATION_SERVICES);
        System.out.println("**************** Map change found!");

        // First check for errors
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
        }
        else {
            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) ||
                    (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                System.out.println("geofence detected: "+ transition);

                // we've entered or exited our geofence.
                // update latest Location trigger
                BackgroundService.getInstance().mTriggeredGeofences = LocationClient.getTriggeringGeofences(intent);
                BackgroundService.getInstance().mTriggeredGeoFenceTransition = transition;
                // check events again by sending broadcast
                Main.getInstance().sendBroadcast("EVENT_ENABLED");
                //BackgroundService.getInstance().EventFinder(BackgroundService.getInstance(), BackgroundService.getInstance().sIntent);
            }
            else {
                // handle the error
            }
        }
    }


}