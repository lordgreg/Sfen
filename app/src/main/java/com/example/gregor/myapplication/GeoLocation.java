package com.example.gregor.myapplication;

import android.os.Bundle;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Created by Gregor on 18.7.2014.
 */
public class GeoLocation implements GooglePlayServicesClient.ConnectionCallbacks {
    private LocationClient mLocationClient;

    /**
     * constructor
     */
    public GeoLocation() {

    }


    @Override
    public void onConnected(Bundle bundle) {

        System.out.println("geofence connected.");

    }

    @Override
    public void onDisconnected() {

    }
}
