package gpapez.sfen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.LocationClient;

/**
 * Created by Gregor on 9.9.2014.
 */
public class ReceiverGeoLocation extends BroadcastReceiver {

    Context context;

    Intent broadcastIntent = new Intent();


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        Log.i("sfen", "Action recieved: "+ intent.getAction());

//        if (LocationClient.hasError(intent)) {
//            handleError(intent);
//        }
//        else {
//            handleEnterExit(intent);
//        }


    }
}
