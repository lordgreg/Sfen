package gpapez.sfen;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;

import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.List;

/**
 * Created by Gregor on 30.7.2014.
 */
public class CellConnectionInfo {
    private TelephonyManager telephonyManager;
    private String cellId;
    private String cellType;
    private boolean isError;
    private String errorString;
    //private Object mCell;
    //private Object cell;

    public CellConnectionInfo(Activity activity) {
        try {
            telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

            // cell code ID using getAllCellInfo can be used on >=4.1 of
            // Android JELLY_BEAN, but may be required in other situations too
            Boolean gotCellInfo = false;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {

                List<CellInfo> cellInfos = (List<CellInfo>) telephonyManager.getAllCellInfo();


                if (cellInfos != null && cellInfos.size() > 0) {
                    gotCellInfo = true;
                    CellInfo cell = cellInfos.get(0);

                    setCellType(cell);

                    //this.cellId = cellId;
                    //Log.d("CELL ID (tostring)", cell.toString());
                    //Log.d("CELL ID", cellId);
                    //txtView.append("Celica: "+ cellId +"\n");
                }
            }
            // if not JB, use other option
            if (!gotCellInfo) {

                CellLocation cell = telephonyManager.getCellLocation();
                if (cell != null) {
                    //String cellId, cellType = "";
                    //cellType = cell.getClass().getSimpleName();
                    setCellType(cell);
                } else {
                    errorString = "No mobile connection.";
                    Log.e("sfen", errorString);
                }
            }

        } catch (Exception e) {
            isError = true;
            errorString = "No mobile connection.";
            Log.e("sfen", errorString +"("+ e.toString() +")");
            e.printStackTrace();
        }
    }

    public boolean isError() {
        return isError;
    }

    public String getError() {
        return errorString;
    }

    public String getCellId() {
        return cellId;
    }


    public String getCellType() {
        return cellType;
    }


    private void setCellType(CellInfo cell) {
        cellType = cell.getClass().getSimpleName();

        // cell code ID using getAllCellInfo can be used on >=4.1 of
        // Android JELLY_BEAN
        //This is only called in JB
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                if (cellType.compareTo("CellInfoLte") == 0) {
                    CellInfoLte mCell = (CellInfoLte) cell;
                    cellType = "LTE";
                    cellId = mCell.getCellIdentity().getMcc() + ":" + mCell.getCellIdentity().getMnc() + ":" + mCell.getCellIdentity().getCi();
                } else if (cellType.compareTo("CellInfoGsm") == 0) {
                    CellInfoGsm mCell = (CellInfoGsm) cell;
                    cellType = "GSM";
                    cellId = mCell.getCellIdentity().getMcc() + ":" + mCell.getCellIdentity().getMnc() + ":" + mCell.getCellIdentity().getCid();
                } else if (cellType.compareTo("CellInfoCdma") == 0) {
                    CellInfoCdma mCell = (CellInfoCdma) cell;
                    cellType = "CDMA";
                    cellId = "" + mCell.getCellIdentity().getBasestationId();
                } else if (cellType.compareTo("CellInfoWcdma") == 0) {
                    CellInfoWcdma mCell = (CellInfoWcdma) cell;
                    cellType = "WCDMA";
                    cellId = mCell.getCellIdentity().getMcc() + ":" + mCell.getCellIdentity().getMnc() + ":" + mCell.getCellIdentity().getCid();
                } else {
                    cellType = "";
                    cellId = "NULL";
                    isError = true;
                    errorString = "Unknown cell type (" + cellType + ")!";
                    Log.e("sfen", errorString);
                }
            }
    }

    private void setCellType(CellLocation cell) {
        cellType = cell.getClass().getSimpleName();

        if (cellType.compareTo("GsmCellLocation") == 0) {
            GsmCellLocation mCell = (GsmCellLocation) cell;
            cellType = "GSM";
            cellId = mCell.getCid() + ":" + mCell.getLac() + ":" + mCell.getPsc();
        } else if (cellType.compareTo("CdmaCellLocation") == 0) {
            CdmaCellLocation mCell = (CdmaCellLocation) cell;
            cellType = "CDMA";
            cellId = String.valueOf(mCell.getBaseStationId());
        } else {
            cellType = "";
            cellId = "NULL";
            isError = true;
            errorString = "Unknown cell type (" + cellType + ")!";
            Log.e("sfen", errorString);
        }
    }
}
