package gpapez.sfen;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
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
    private String cellId = "";
    private String cellType;
    private boolean isError;
    private String errorString;
    private Activity sActivity;
    //private Object mCell;
    //private Object cell;

    public CellConnectionInfo(Activity activity) {
        try {
            sActivity = activity;

            telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

            // cell code ID using getAllCellInfo can be used on >=4.1 of
            // Android JELLY_BEAN, but may be required in other situations too
            Boolean gotCellInfo = false;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {

                List<CellInfo> cellInfos = (List<CellInfo>) telephonyManager.getAllCellInfo();

                //Note: Only use first in list (assume it is best, last may be empty), we only add one Cell
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
                    isError = true;
                    errorString = activity.getString(R.string.no_mobile_connection);
                    Log.e("sfen", errorString);
                }
            }

        } catch (Exception e) {
            isError = true;
            errorString = activity.getString(R.string.no_mobile_connection);
            Log.e("sfen", errorString +"("+ e.toString() +")");
            e.printStackTrace();
        }
    }

    public boolean isError() {
        /**
         * LAST error check
         *
         * IF our cellID includes -1, there's something wrong with cell and it isn't
         * correct cellID meaning its an corrupted catch
         */
        if (cellId != null || cellId.equals("")) {
            if (cellId.contains("-1")) {
                isError = true;
                errorString = sActivity.getString(R.string.cell_wrong_cell_report, cellId);
                Log.e("sfen", errorString);
            }
        }

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


    private void setCellError(String cellType) {
        cellType = "";
        cellId = "NULL";
        isError = true;
        errorString = sActivity.getString(R.string.unknown_cell_type, cellType);
    }

    private void setCellType(CellInfo cell) {
        // cell code ID using getAllCellInfo can be used on >=4.1 of
        // Android JELLY_BEAN
        //This is only called in JB
        //identification and sectors:
        //  http://people.csail.mit.edu/bkph/cellular_repeater_numerology.shtml, wikipedia cell-id
        //Keep Lac before Cid for GSM, Wcdma as sector is encoded there
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                if (cell instanceof CellInfoLte) {
                    CellIdentityLte mCellId = ((CellInfoLte) cell).getCellIdentity();
                    cellType = "LTE";
                    int ci = mCellId.getCi() / 256;
                    //sector is not part of the base station, but the area it covers
                    int sector = mCellId.getCi() % 256;
                    //PCI is not part of the identification of the base station, sector is included in CI
                    cellId = mCellId.getMcc() + ":" + mCellId.getMnc() + ":" + mCellId.getTac() + ":" + ci + ":" + sector;
                } else if (cell instanceof CellInfoGsm) {
                    CellIdentityGsm mCellId = ((CellInfoGsm) cell).getCellIdentity();
                    cellType = "GSM";
                    cellId = mCellId.getMcc() + ":" + mCellId.getMnc() + ":" + mCellId.getLac() + ":" + mCellId.getCid();
                } else if (cell instanceof CellInfoCdma) {
                    CellIdentityCdma mCellId = ((CellInfoCdma) cell).getCellIdentity();
                    cellType = "CDMA";
                    cellId = "" + mCellId.getBasestationId();
                } else if (cell instanceof CellInfoWcdma) {
                    CellIdentityWcdma mCellId = ((CellInfoWcdma) cell).getCellIdentity();
                    cellType = "WCDMA";
                    cellId = mCellId.getMcc() + ":" + mCellId.getMnc() + ":" + mCellId.getLac() + ":" + mCellId.getCid();
                } else {
                    setCellError(cell.getClass().getSimpleName());
                    Log.e("sfen", getError());
                }
            }
    }

    private void setCellType(CellLocation cell) {
        //MNC/MCC (for GsmCellLocation/CdmaCellLocation) can be retrieved, ignore
        //TelephonyManager phoneMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        //String mccMnc = phoneMgr.getNetworkOperator();
        //String mcc = "";
        //String mnc = "";
        //if (mccMnc != null && mccMnc.length() >= 5) {
        //    mcc = mccMnc.substring(0, 3);
        //    mnc = mccMnc.substring(3, 5);
        //}
        if (cell instanceof GsmCellLocation) {
            GsmCellLocation mCell = (GsmCellLocation) cell;
            cellType = "GSM";
            cellId = mCell.getLac() + ":" + mCell.getCid();
        } else if (cell instanceof CdmaCellLocation) {
            CdmaCellLocation mCell = (CdmaCellLocation) cell;
            cellType = "CDMA";
            cellId = String.valueOf(mCell.getBaseStationId());
        } else {
            setCellError(cell.getClass().getSimpleName());
            Log.e("sfen", getError());
        }
    }
}
