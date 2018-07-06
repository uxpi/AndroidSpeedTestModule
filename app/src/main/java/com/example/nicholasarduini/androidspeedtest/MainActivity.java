package com.example.nicholasarduini.androidspeedtest;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
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

import static com.example.nicholasarduini.androidspeedtest.TelephonyModule.getRfInfo;

public class MainActivity extends AppCompatActivity {

    private TextView pingResult;
    private TextView downloadSpeedLabel;
    private TextView uploadSpeedLabel;
    private TextView totalDownBytes;
    private TextView totalUpBytes;
    private TextView speedLabel;
    private TextView testTypeLabel;
    private TextView startSpeedLabel;
    private TextView endSpeedLabel;
    private TextView speedMbsLabel;
    private ProgressBar testProgress;
    private ProgressBar loadingCircle;
    private CustomGauge speedGauge;
    private LinearLayout downloadLayout;
    private LinearLayout uploadLayout;
    private Switch sslSwitch;
    private MenuItem stopTestButton;
    private LineChart speedGraph;
    private Button startTestCircleButton;
    private Button cellDataButton;

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
        startSpeedLabel = findViewById(R.id.startSpeed);
        endSpeedLabel = findViewById(R.id.endSpeed);
        speedMbsLabel = findViewById(R.id.mbsLabel);
        testProgress = findViewById(R.id.testProgress);
        loadingCircle = findViewById(R.id.loadingCircle);
        speedGauge = findViewById(R.id.speedGauge);
        downloadLayout = findViewById(R.id.downloadLayout);
        uploadLayout = findViewById(R.id.uploadLayout);
        sslSwitch = findViewById(R.id.sslSwitch);
        speedGraph = findViewById(R.id.resultsGraph);
        startTestCircleButton = findViewById(R.id.startTestCircleButton);
        cellDataButton = findViewById(R.id.cellData);

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        hideResults();
        loadingCircle.setAlpha(0);
        sslSwitch.setChecked(true);
        setGauge(false, 0);

        speedTest = new SpeedTest(getApplicationContext());
        speedTest.setTestLength(8);
        speedTest.speedTestEventListener(new SpeedTestEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onPingResult(double ping) {
                pingResult.setText(String.format("Ping  %.2fms", ping));
                pingResult.animate().alpha(1).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }

            @Override
            public void onDownloadPreExecute() {
                testTypeLabel.setText(R.string.download);
                loadingCircle.setAlpha(0);
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
                testCompleteInit();
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

        startTestCircleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getServer();
                testStartingInit();
                hideResults();
                speedTest.startTest(sslSwitch.isChecked());
            }
        });

        cellDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getPermission();
            }
        });
    }

    private static Boolean mPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true;
                }
            }
        }

        updateTelphonyData();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private static CellData cellData;
    private void updateTelphonyData() {
        if(mPermissionGranted){
            TelephonyModule rfInfo = new TelephonyModule(getApplicationContext());
            cellData = rfInfo.getRfInfo();
            CellDataDialog cdd = new CellDataDialog(this, cellData);
            cdd.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speed_test, menu);
        stopTestButton = menu.findItem(R.id.stopTestAction);
        stopTestButton.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.stopTestAction){
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
        //http://d11qof99tjkti7.cloudfront.net/data.zip
        speedTest.setDownloadUrl("http://d11qof99tjkti7.cloudfront.net/data.zip");
        speedTest.setUploadUrl("http://ipv4.ikoula.testdebit.info/");
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

    public void testCompleteInit(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testProgress.setProgress(100);
                loadingCircle.setAlpha(0);
                testTypeLabel.setText("");
                stopTestButton.setVisible(false);
                startTestCircleButton.setAlpha(1);
                startTestCircleButton.setVisibility(View.VISIBLE);
                setGauge(false, 800);
            }
        });
    }

    public void testStartingInit(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speedGraph.getData().clearValues();
                speedGauge.setValue(0);
                speedLabel.setText("");
                testProgress.setProgress(0);
                loadingCircle.setAlpha(1);
                stopTestButton.setVisible(true);
                startTestCircleButton.setAlpha(0);
                startTestCircleButton.setVisibility(View.GONE);
                setGauge(true, 800);
            }
        });
    }

    public void setGauge(boolean testRunning, int duration){
        if(testRunning){
            animateGauge(360, 270, speedGauge, duration);
            speedGauge.setPointEndColor(getResources().getColor(R.color.blue_end));
            speedGauge.setPointStartColor(getResources().getColor(R.color.blue_start));
            speedGauge.setStrokeColor(getResources().getColor(R.color.light_grey));
            speedGauge.setSweepAngle(270);
            speedLabel.setAlpha(1);
            speedMbsLabel.setAlpha(1);
            startSpeedLabel.setAlpha(1);
            endSpeedLabel.setAlpha(1);
        } else {
            animateGauge(270, 360, speedGauge, duration);
            speedGauge.setPointEndColor(getResources().getColor(R.color.bellBlue));
            speedGauge.setPointStartColor(getResources().getColor(R.color.bellBlue));
            speedGauge.setStrokeColor(getResources().getColor(R.color.bellBlue));
            speedGauge.setSweepAngle(360);
            speedLabel.setAlpha(0);
            speedMbsLabel.setAlpha(0);
            startSpeedLabel.setAlpha(0);
            endSpeedLabel.setAlpha(0);
            speedGauge.setValue(0);
        }
    }

    public void animateGauge(int initialValue, int finalValue, final CustomGauge gauge, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(initialValue, finalValue);
        valueAnimator.setDuration(duration);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                gauge.setSweepAngle((int) valueAnimator.getAnimatedValue());
                gauge.setValue(0);

            }
        });
        valueAnimator.start();
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