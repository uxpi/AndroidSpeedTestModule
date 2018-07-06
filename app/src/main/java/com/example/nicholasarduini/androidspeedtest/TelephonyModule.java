package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.content.Context;
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

    private TelephonyManager tm;
    static List<CellInfo> cellInfoList;

    @SuppressLint("MissingPermission")
    public TelephonyModule(Context context) {
        tm = context.getSystemService(TelephonyManager.class);

        cellInfoList = tm.getAllCellInfo();
    }

    public static String decToHex(int dec) {
        return String.format("%x", dec);
    }

    public static int hexToDec(String hex) {
        return Integer.parseInt(hex, 16);
    }


    public static CellData getRfInfo() {
        CellData cellData = new CellData();
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

                //cid = cellIdentityLte.getCi();
                //cellIdHex = decToHex(cellIdentityLte.getCi());
                //tac = cellIdentityLte.getTac();

                System.out.println(cellData.toString());
            }
        }

        return cellData;
    }
}




