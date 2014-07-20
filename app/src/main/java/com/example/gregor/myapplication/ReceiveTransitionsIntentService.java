package com.example.gregor.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
                Log.i("sfen", "Geofence transition: "+ transition);

                // we've entered or exited our geofence.
                // update latest Location trigger
                BackgroundService.getInstance().mTriggeredGeofences = LocationClient.getTriggeringGeofences(intent);
                BackgroundService.getInstance().mTriggeredGeoFenceTransition = transition;
                // check events again by sending broadcast
                Main.getInstance().sendBroadcast(
                        (transition == 1) ? "GEOFENCE_ENTER" : "GEOFENCE_EXIT"
                );

            }
            else {
                // handle the error
                Log.e("sfen", "Weird error when receiving Geofence transition.");
            }
        }
    }


}