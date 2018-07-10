package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Created by brad on 2017-11-01.
 */

public class TelephonyModule {

    private static TelephonyManager tm;
    private static Context context;
    static List<CellInfo> cellInfoList;

    @SuppressLint("MissingPermission")
    public TelephonyModule(Context c) {
        tm = c.getSystemService(TelephonyManager.class);
        context = c;

        cellInfoList = tm.getAllCellInfo();
    }

    public static String decToHex(int dec) {
        return String.format("%x", dec);
    }

    public static int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }


    @SuppressLint("MissingPermission")
    public static CellData getRfInfo() {
        CellData cellData = new CellData();
        tm.getAllCellInfo();
        if(cellInfoList.size() <= 0) { return cellData; }
        CellInfo cellInfo = cellInfoList.get(0);
        if (cellInfo instanceof CellInfoLte) {
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();

            //check serving cell
            if (cellIdentityLte.getPci() < 1000) {
                String cellIdHex = decToHex(cellIdentityLte.getCi());
                cellData.seteNodeB(hexToDec(cellIdHex.substring(0, cellIdHex.length() - 2)));
                cellData.setLocalCellId(hexToDec(cellIdHex.substring(cellIdHex.length() - 2, cellIdHex.length())));
                cellData.setEarfcn(cellIdentityLte.getEarfcn());
                cellData.setdBm(cellSignalStrengthLte.getDbm());
                cellData.setSinr(Integer.parseInt(cellSignalStrengthLte.toString().substring(26, 28).trim()));
                cellData.setPci(cellIdentityLte.getPci());
                cellData.setMcc(cellIdentityLte.getMcc());
                cellData.setMnc(cellIdentityLte.getMnc());
                cellData.setTa(cellSignalStrengthLte.getTimingAdvance());

                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                cellData.setLat(location.getLatitude());
                cellData.setLon(location.getLongitude());

                //cid = cellIdentityLte.getCi();
                //cellIdHex = decToHex(cellIdentityLte.getCi());
                //tac = cellIdentityLte.getTac();
            }
        }

        return cellData;
    }
}




