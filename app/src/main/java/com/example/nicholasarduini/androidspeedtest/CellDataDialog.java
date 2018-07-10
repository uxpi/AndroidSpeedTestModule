package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CellDataDialog extends Dialog {
    private Button dismissButton;
    private TextView eNodeB, localCellId, earfcn, rsrp, sinr, pci, mcc, mnc, ta, lat, lon;
    private CellData cellData;

    public CellDataDialog(Activity activity, CellData cellData) {
        super(activity);
        this.cellData = cellData;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cell_data_dialog);

        eNodeB = findViewById(R.id.eNodeB);
        localCellId = findViewById(R.id.localCellId);
        earfcn = findViewById(R.id.earfcn);
        rsrp = findViewById(R.id.rsrp);
        sinr = findViewById(R.id.sinr);
        pci = findViewById(R.id.pci);
        mcc = findViewById(R.id.mcc);
        mnc = findViewById(R.id.mnc);
        ta = findViewById(R.id.ta);
        lat = findViewById(R.id.lat);
        lon = findViewById(R.id.lon);

        eNodeB.setText(String.valueOf(String.format("%d", cellData.geteNodeB())));
        localCellId.setText(String.valueOf(String.format("%d", cellData.getLocalCellId())));
        earfcn.setText(String.valueOf(String.format("%d", cellData.getEarfcn())));
        rsrp.setText(String.valueOf(String.format("%d", cellData.getdBm())));
        sinr.setText(String.valueOf(String.format("%d", cellData.getSinr())));
        pci.setText(String.valueOf(String.format("%d", cellData.getPci())));
        mcc.setText(String.valueOf(String.format("%d", cellData.getMcc())));
        mnc.setText(String.valueOf(String.format("%d", cellData.getMnc())));
        ta.setText(String.valueOf(String.format("%d", cellData.getTa())));
        lat.setText(String.valueOf(String.format("%f", cellData.getLat())));
        lon.setText(String.valueOf(String.format("%f", cellData.getLon())));


        dismissButton = findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
