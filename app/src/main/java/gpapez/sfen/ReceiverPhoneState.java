package gpapez.sfen;

import android.content.Context;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
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

//        CALL_STATE_IDLE
//        CALL_STATE_RINGING
//        CALL_STATE_OFFHOOK

    }


    public void onCellLocationChanged(GsmCellLocation CellId){
        super.onCellLocationChanged(CellId);


        // when we change cells, we have to wake up to send broadcast
        PowerManager pm = (PowerManager) BackgroundService.getInstance()
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sfen");
        mWakeLock.acquire();


        Main.getInstance().sendBroadcast("CELL_LOCATION_CHANGED");

        // close down the wakelock
        mWakeLock.release();

    }

}
