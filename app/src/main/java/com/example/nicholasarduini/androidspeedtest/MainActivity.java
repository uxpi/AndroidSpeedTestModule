package com.example.nicholasarduini.androidspeedtest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.luas.networkspeedtest.SpeedTest;
import com.android.luas.networkspeedtest.SpeedTestEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView downloadSpeedLabel;
    private TextView uploadSpeedLabel;
    private Button beginTestButton;
    private ProgressBar testProgress;
    private ProgressBar loadingCircle;

    private SpeedTest speedTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadSpeedLabel = findViewById(R.id.downloadSpeed);
        uploadSpeedLabel = findViewById(R.id.uploadSpeed);
        beginTestButton = findViewById(R.id.beingTestButton);
        testProgress = findViewById(R.id.testProgress);
        loadingCircle = findViewById(R.id.loadingCircle);
        loadingCircle.setVisibility(View.GONE);

        //https://speed.hetzner.de/100MB.bin
        //https://speed.hetzner.de/1GB.bin
        //https://speed.hetzner.de/10GB.bin
        //http://d11qof99tjkti7.cloudfront.net/data.zip
        //http://35.224.242.149/down
        //http://35.224.242.149/
        //http://ipv4.ikoula.testdebit.info/
        //http://104.197.165.181/download
        //https://nodespeed.forb.luas.ml/download

        speedTest = new SpeedTest(getApplicationContext());
        speedTest.setDownloadUrl("https://nodespeed.forb.luas.ml/download");
        speedTest.setUploadUrl("https://nodespeed.forb.luas.ml/upload");

        speedTest.speedTestEventListener(new SpeedTestEventListener() {
            @Override
            public void onDownloadChanged(final double speedMbps, final double elapsedTime) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        loadingCircle.setVisibility(View.GONE);
                        downloadSpeedLabel.setText(String.format("%.2f Mbps", speedMbps));

                        int progressPercentage = (int) (elapsedTime / ((double) SpeedTest.testLength) * 100);
                        testProgress.setProgress(progressPercentage);
                    }
                });
            }

            @Override
            public void onUploadChanged(final double speedMbps, final double elapsedTime) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        loadingCircle.setVisibility(View.GONE);
                        uploadSpeedLabel.setText(String.format("%.2f Mbps", speedMbps));

                        int progressPercentage = (int) (elapsedTime / ((double) SpeedTest.testLength) * 100);
                        testProgress.setProgress(progressPercentage);
                    }
                });
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


        beginTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableButtons();
                speedTest.startTest();
            }
        });
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

    public void enableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                testProgress.setProgress(100);
                beginTestButton.setEnabled(true);
                beginTestButton.setAlpha(1.0f);
                loadingCircle.setVisibility(View.GONE);
            }
        });
    }

    public void disableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                beginTestButton.setEnabled(false);
                beginTestButton.setAlpha(0.2f);
                loadingCircle.setVisibility(View.VISIBLE);
            }
        });
    }
}