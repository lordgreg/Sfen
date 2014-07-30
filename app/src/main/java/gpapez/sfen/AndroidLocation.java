package gpapez.sfen;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Gregor on 4.7.2014.
 */
public class AndroidLocation extends Activity implements LocationListener {
    private LocationManager locationManager;
    private String provider;
    private Criteria criteria;
    private Location location;
    private Context context;
    private double latitude;
    private double longitude;
    private boolean isError;
    private String errorString;
    private boolean gpsEnabled;
    private boolean networkEnabled;

    public AndroidLocation(Context context) {
        // Get the location manager
        this.context = context;
        provider = null;
        //context = super.getApplicationContext();

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        criteria = new Criteria();
        List<String> providers = (List<String>) locationManager.getAllProviders();

        // check enabled and possible providers!
        try{gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //System.out.println("gps enabled: "+ gpsEnabled +", networkEnabled: "+ networkEnabled);

        if (gpsEnabled) {
            provider = locationManager.GPS_PROVIDER;
        }
        else if (networkEnabled) {
            provider = locationManager.NETWORK_PROVIDER;
        }
//        else {
//            provider = null;
//        }

        if (provider == null) {
            errorString = "Location setting has to be enabled in System Settings.";
            isError = true;
            Log.e("sfen", "Location setting has to be enabled for location manager to work.");
        }
        else {
            //provider = locationManager.getBestProvider(criteria, false);
            location = locationManager.getLastKnownLocation(provider);


            // Initialize the location fields
            if (location != null) {
                //System.out.println("Provider " + provider + " has been selected.");
                onLocationChanged(location);
            } else {
                errorString = "Location not available";
                isError = true;
                Log.e("sfen", errorString);

            }
        }



    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(context, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(context, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isError() {
        return isError;
    }

    public String getError() {
        return errorString;
    }

    public String getProvider() { return provider; }

}
