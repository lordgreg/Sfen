package gpapez.sfen;

import android.telephony.PhoneStateListener;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by Gregor on 30.7.2014.
 */
public class ReceiverPhoneState extends PhoneStateListener {

    public void onCellLocationChanged(GsmCellLocation CellId){
        super.onCellLocationChanged(CellId);

        Main.getInstance().sendBroadcast("CELL_LOCATION_CHANGED");

        //ci.setText(CellId.getCid());
        //lac.setText(CellId.getLac());
        /*bts= baseStation.getBaseStationId();
        Lac=cellId.getLac();*/

    }

}
