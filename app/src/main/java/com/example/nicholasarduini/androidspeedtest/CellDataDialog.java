package com.example.nicholasarduini.androidspeedtest;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CellDataDialog extends Dialog {
    private Activity activity;
    private Dialog dialog;
    private Button dismissButton;
    private TextView eNodeB, localCellId, earfcn, rsrp, sinr, pci, mcc, mnc, ta;
    private CellData cellData;

    public CellDataDialog(Activity activity, CellData cellData) {
        super(activity);
        this.activity = activity;
        this.cellData = cellData;
    }

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

        eNodeB.setText(Integer.toString(cellData.geteNodeB()));
        localCellId.setText(Integer.toString(cellData.getLocalCellId()));
        earfcn.setText(Integer.toString(cellData.getEarfcn()));
        rsrp.setText(Integer.toString(cellData.getdBm()));
        sinr.setText(Integer.toString(cellData.getSinr()));
        pci.setText(Integer.toString(cellData.getPci()));
        mcc.setText(Integer.toString(cellData.getMcc()));
        mnc.setText(Integer.toString(cellData.getMnc()));
        ta.setText(Integer.toString(cellData.getTa()));


        dismissButton = findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
