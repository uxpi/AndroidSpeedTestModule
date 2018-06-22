package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.android.luas.networkspeedtest.SpeedTest;
import com.android.luas.networkspeedtest.SpeedTestEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private TextView pingResult;
    private TextView downloadSpeedLabel;
    private TextView uploadSpeedLabel;
    private TextView totalDownBytes;
    private TextView totalUpBytes;
    private TextView speedLabel;
    private TextView testTypeLabel;
    private ProgressBar testProgress;
    private ProgressBar loadingCircle;
    private CustomGauge speedGauge;
    private LinearLayout downloadLayout;
    private LinearLayout uploadLayout;
    private Switch sslSwitch;
    private MenuItem startTestButton;
    private MenuItem stopTestButton;
    private LineChart speedGraph;

    private SpeedTest speedTest;

    private static int GAUGE_END_VALUE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pingResult = findViewById(R.id.pingResult);
        downloadSpeedLabel = findViewById(R.id.downloadSpeed);
        uploadSpeedLabel = findViewById(R.id.uploadSpeed);
        totalDownBytes = findViewById(R.id.totalDownBytes);
        totalUpBytes = findViewById(R.id.totalUpBytes);
        speedLabel = findViewById(R.id.speedLabel);
        testTypeLabel = findViewById(R.id.testTypeLabel);
        testProgress = findViewById(R.id.testProgress);
        loadingCircle = findViewById(R.id.loadingCircle);
        speedGauge = findViewById(R.id.speedGauge);
        downloadLayout = findViewById(R.id.downloadLayout);
        uploadLayout = findViewById(R.id.uploadLayout);
        sslSwitch = findViewById(R.id.sslSwitch);
        speedGraph = findViewById(R.id.resultsGraph);

        LineData data = new LineData();
        data.addDataSet(null);
        speedGraph.setData(data);
        speedGraph.getAxisRight().setDrawGridLines(false);
        speedGraph.getAxisLeft().setDrawGridLines(false);
        speedGraph.getXAxis().setDrawGridLines(false);
        speedGraph.getAxisRight().setDrawLabels(false);
        speedGraph.getAxisLeft().setDrawLabels(false);
        speedGraph.getXAxis().setDrawLabels(false);
        speedGraph.getAxisRight().setDrawAxisLine(false);
        speedGraph.getAxisLeft().setDrawAxisLine(false);
        speedGraph.getXAxis().setDrawAxisLine(false);
        speedGraph.getLegend().setEnabled(false);
        speedGraph.setDescription(null);
        speedGraph.setTouchEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        hideResults();
        loadingCircle.setVisibility(View.GONE);
        sslSwitch.setChecked(true);

        speedTest = new SpeedTest(getApplicationContext());
        speedTest.setTestLength(8);
        speedTest.speedTestEventListener(new SpeedTestEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onPingResult(double ping) {
                pingResult.setText(String.format("Ping  %.2fms", ping));
                pingResult.animate().alpha(1).setDuration(250).setInterpolator(new DecelerateInterpolator());
            }

            @Override
            public void onDownloadPreExecute() {
                testTypeLabel.setText(R.string.download);
                loadingCircle.setVisibility(View.GONE);
                speedGauge.setEndValue(GAUGE_END_VALUE);
            }

            @Override
            public void onUploadPreExecute() {
                testTypeLabel.setText(R.string.upload);
            }

            @Override
            public void onDownloadChanged(final double speedMbps, final double elapsedTime, final boolean updateGraph) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSpeed(speedMbps, elapsedTime, true, updateGraph);
                    }
                });
            }

            @Override
            public void onUploadChanged(final double speedMbps, final double elapsedTime, final boolean updateGraph) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateSpeed(speedMbps, elapsedTime, false, updateGraph);
                    }
                });
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onDownloadComplete(double speedMbps, long totalBytesRead) {
                downloadLayout.setVisibility(View.VISIBLE);
                downloadLayout.animate().alpha(1).setDuration(250).setInterpolator(new DecelerateInterpolator());
                downloadSpeedLabel.setText(String.format("%.2f", speedMbps));
                totalDownBytes.setText(String.format("%dMB", totalBytesRead / (1000 * 1000)));
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onUploadComplete(double speedMbps, long totalBytesWritten) {
                uploadLayout.setVisibility(View.VISIBLE);
                uploadLayout.animate().alpha(1).setDuration(250).setInterpolator(new DecelerateInterpolator());
                uploadSpeedLabel.setText(String.format("%.2f", speedMbps));
                totalUpBytes.setText(String.format("%dMB", totalBytesWritten / (1000 * 1000)));
            }

            @Override
            public void testComplete() {
                enableButtons();
            }

            @Override
            public void downloadError() {
                createAlert(getResources().getString(R.string.download_error));
            }

            @Override
            public void uploadError() {
                createAlert(getResources().getString(R.string.upload_error));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speed_test, menu);
        startTestButton = menu.findItem(R.id.startTestAction);
        stopTestButton = menu.findItem(R.id.stopTestAction);
        stopTestButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.startTestAction) {
            getServer();
            disableButtons();
            hideResults();
            resetValues();
            speedTest.startTest(sslSwitch.isChecked());
        } else if(id == R.id.stopTestAction){
            speedTest.stopTest();
        } else if(id == R.id.showHistoryAction){
            Intent myIntent = new Intent(MainActivity.this, SpeedResultsActivity.class);
            MainActivity.this.startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void addEntry(double y, boolean download) {
        if(speedGraph.getData() != null) {
            int index = download ? 0 : 1;

            ILineDataSet set = null;
            if(speedGraph.getData().getDataSets().size() > index) {
                set = speedGraph.getData().getDataSetByIndex(index);
            }

            if (set == null) {
                set = createSet(download);
                speedGraph.getData().addDataSet(set);
            }

            set.addEntry(new Entry(set.getEntryCount(), (float) (y)));
            speedGraph.getData().notifyDataChanged();
            speedGraph.notifyDataSetChanged();
            speedGraph.setVisibleXRangeMaximum(120);
            speedGraph.setDrawGridBackground(false);
            speedGraph.moveViewToX(speedGraph.getData().getEntryCount());
        }
    }

    private LineDataSet createSet(boolean download) {

        LineDataSet set = new LineDataSet(null, "");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        if(download){
            set.setColor(Color.MAGENTA);
        } else {
            set.setColor(Color.CYAN);
        }

        set.setDrawCircles(false);
        set.setLineWidth(4f);
        set.setDrawValues(false);
        return set;
    }

    public void getServer(){
        if(sslSwitch.isChecked()){
            speedTest.setDownloadUrl("https://nodespeed.forb.luas.ml/download");
            speedTest.setUploadUrl("https://nodespeed.forb.luas.ml/upload");
        } else {
            speedTest.setDownloadUrl("http://nodespeed.forb.luas.ml/download");
            speedTest.setUploadUrl("http://nodespeed.forb.luas.ml/upload");
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateSpeed(double speedMbps, double elapsedTime, boolean download, boolean updateGraph){
        speedLabel.setText(String.format("%.2f", speedMbps));

        if(speedMbps > GAUGE_END_VALUE){
            speedGauge.setValue(GAUGE_END_VALUE);
        } else {
            speedGauge.setValue((int) speedMbps);
        }

        int progressPercentage = (int) (elapsedTime / ((double) SpeedTest.getCurrentTestLength()) * 100);
        testProgress.setProgress(progressPercentage);

        if(updateGraph) { addEntry(speedMbps, download); }
    }

    public void createAlert(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(MainActivity.this);
                aBuilder.setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                aBuilder.setMessage(message);
                aBuilder.setCancelable(true);
                AlertDialog alert = aBuilder.create();
                alert.show();
            }
        });
    }

    public void hideResults(){
        testTypeLabel.setText("");
        pingResult.setAlpha(0);
        downloadLayout.setAlpha(0);
        uploadLayout.setAlpha(0);
        uploadLayout.setVisibility(View.GONE);
    }

    public void resetValues(){
        speedGraph.getData().clearValues();
        speedGauge.setValue(0);
        speedLabel.setText("");

    }

    public void enableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testProgress.setProgress(100);
                loadingCircle.setVisibility(View.GONE);
                testTypeLabel.setText("");
                startTestButton.setVisible(true);
                stopTestButton.setVisible(false);
            }
        });
    }

    public void disableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testProgress.setProgress(0);
                loadingCircle.setVisibility(View.VISIBLE);
                startTestButton.setVisible(false);
                stopTestButton.setVisible(true);
            }
        });
    }
}

//https://speed.hetzner.de/100MB.bin
//https://speed.hetzner.de/1GB.bin
//https://speed.hetzner.de/10GB.bin
//http://d11qof99tjkti7.cloudfront.net/data.zip
//http://35.224.242.149/down
//http://35.224.242.149/
//http://ipv4.ikoula.testdebit.info/
//http://104.197.165.181/download
//https://nodespeed.forb.luas.ml/download