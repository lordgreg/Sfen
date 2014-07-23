package gpapez.sfen;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregor on 18.7.2014.
 */
public class GeoLocation implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener,
        LocationClient.OnRemoveGeofencesResultListener {

    private ArrayList<Geofence> mGeofences = new ArrayList<Geofence>();
    private List<String> mGeofenceIds = null;
    private LocationClient mLocationClient;
    public enum REQUEST_TYPE {ADD, REMOVE, REMOVE_ALL};
    private REQUEST_TYPE mRequestType;
    private PendingIntent mGeoPendingIntent;
    private PendingIntent mCurrentIntent;
    private boolean isGeoConnectionInProgress = false;

    // Storage for a context from the calling client
    private Context mContext;

    /**
     * constructor
     */
    public GeoLocation(Context context) {
        mContext = context;

        // start location client for geofences
        //mLocationClient = new LocationClient(mContext, this, this);

    }

    protected void AddGeofences(ArrayList<Geofence> toAdd) {

        // check if we have google play services installed
        if (!Util.hasGooglePlayServices()) {
            return ;
        }

        // set type
        mRequestType = REQUEST_TYPE.ADD;

        // create new geofence array
        mGeofences = toAdd;


        // if request is in progress, don't continue, otherwise, lets start
        if (!isGeoConnectionInProgress) {
            isGeoConnectionInProgress = true;
            getLocationClient().connect();
        }

        else {
            Log.e("sfen", "Cannot start new connection. One is already running.");
        }


    }

    protected void RemoveGeofences(PendingIntent requestIntent) {

        // check if we have google play services installed
        if (!Util.hasGooglePlayServices()) {
            return ;
        }

        // set type
        mRequestType = REQUEST_TYPE.REMOVE_ALL;

        // retrieve intent
        mGeoPendingIntent = requestIntent;

        // create new location client.
        //mLocationClient = new LocationClient(mContext, this, this);

        // if request is in progress, don't continue, otherwise, lets start
        if (!isGeoConnectionInProgress) {
            isGeoConnectionInProgress = true;
            getLocationClient().connect();
        }

        else {
            Log.e("sfen", "Cannot start new connection. One is already running.");
        }
    }

    protected void RemoveGeofencesById(List<String> toRemove) {
        // if list is 0 or null, return error
        if ((toRemove.size() == 0) || (toRemove == null)) {
            Log.e("sfen", "Zero (0) gefoences to remove sent.");
            return ;
        }

        else {
            // set type
            mRequestType = REQUEST_TYPE.REMOVE;

            // set current array
            mGeofenceIds = toRemove;

            // and remove them
            try {
                getLocationClient().connect();
            }
            catch (Exception e) {
                Log.e("sfen", "Problem removing geofences. Are selected Ids even running?");
                e.printStackTrace();
            }
        }

    }


    private GooglePlayServicesClient getLocationClient() {
        if (mLocationClient == null) {

            mLocationClient = new LocationClient(mContext, this, this);
        }
        return mLocationClient;

    }


    /*
     * Create a PendingIntent that triggers an IntentService in your
     * app when a geofence transition occurs.
     */
    protected PendingIntent getTransitionPendingIntent() {
        if (mGeoPendingIntent != null) {
            return mGeoPendingIntent;
        }

        else {

            // Create an explicit Intent
            Intent intent = new Intent(mContext,
                    ReceiveTransitionsIntentService.class);
        /*
         * Return the PendingIntent
         */
            return PendingIntent.getService(
                    mContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    public PendingIntent getRequestPendingIntent() {
        return getTransitionPendingIntent();
    }


    /**
     * GEOFENCES NEEDED FUNCTIONS
     */
    @Override
    public void onConnected(Bundle bundle) {

        switch (mRequestType) {
            case ADD:
                mGeoPendingIntent = getTransitionPendingIntent();

                mLocationClient.addGeofences(mGeofences, mGeoPendingIntent, this);

                break;

            case REMOVE:
                mLocationClient.removeGeofences(mGeofenceIds, this);

                break;

            case REMOVE_ALL:
                mLocationClient.removeGeofences(mGeoPendingIntent, this);

                break;

        }

    }

    @Override
    public void onDisconnected() {
        mLocationClient = null;
        isGeoConnectionInProgress = false;
    }




    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("sfen", "Geo connection failed.");
        isGeoConnectionInProgress = false;
    }

    /*
     * Provide the implementation of
     * OnAddGeofencesResultListener.onAddGeofencesResult.
     * Handle the result of adding the geofences
     *
     */
    @Override
    public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
        // If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Log.d("sfen", "Geofences added ("+ statusCode +").");
        }

        // location access unavailable?
        else if (statusCode == LocationStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            Util.showMessageBox("Geofence service is not available now. Typically this is because " +
                    "the user turned off location access in settings > location access ("+
                    statusCode +")", true);

        }

        // other random error
        else {
            // If adding the geofences failed
            Log.e("sfen", "Error creating new Location listener ("+ statusCode +").");
        }
        // Turn off the in progress flag and disconnect the client
        //mInProgress = false;
        mLocationClient.disconnect();
        isGeoConnectionInProgress = false;
    }


    @Override
    public void onRemoveGeofencesByPendingIntentResult(int statusCode,
                                                       PendingIntent requestIntent) {
        // If removing the geofences was successful
        if (statusCode == LocationStatusCodes.SUCCESS) {
            Log.d("sfen", "All geofences removed ("+ statusCode +").");

        } else {
            Log.e("sfen", "Error trying to remove geofences ("+ statusCode +").");
            // If adding the geocodes failed

        }


        mLocationClient.disconnect();
        isGeoConnectionInProgress = false;
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(
            int statusCode, String[] geofenceRequestIds) {
        // If removing the geocodes was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            Log.d("sfen", "Geofences removed ("+ statusCode +").");
        } else {
            // If removing the geofences failed
            Log.e("sfen", "Removing geofences failed ("+ statusCode +").");
        }

        mLocationClient.disconnect();
        isGeoConnectionInProgress = false;
    }


}
