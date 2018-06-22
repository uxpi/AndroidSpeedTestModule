package com.android.luas.networkspeedtest;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SpeedTestTools {

    private long bytesWritten;
    private double totalSpeeds;
    private int totalSpeedsCount;
    private double elapsedTime;
    private double startTime;
    private double stopTime;
    private double delayTime = 0.5;

    private final static String FILE_NAME = "/upload.txt";

    SpeedTestTools() {
        resetTime();
    }

    public double getElapsedTime(){
        return elapsedTime;
    }

    public void setDelayTime(double dt){
        this.delayTime = dt;
    }

    public double speedTestCalc(long byteCount){
        bytesWritten += byteCount;
        stopTime = System.currentTimeMillis() / 1000;
        elapsedTime = (stopTime - startTime);
        double speedMbps = ((bytesWritten / elapsedTime / 1024.0 / 1024.0) * 8);

        if(Double.isInfinite(speedMbps)) {
            return 0;
        }

        if(elapsedTime > delayTime){
            totalSpeedsCount++;
            totalSpeeds += speedMbps;
            speedMbps = totalSpeeds / totalSpeedsCount;
        }

        return speedMbps;
    }

    public void resetTime(){
        startTime = System.currentTimeMillis() / 1000;
        stopTime = startTime;
        bytesWritten = 0;
        totalSpeeds = 0;
        totalSpeedsCount = 0;
        elapsedTime = 0;
    }

    public static double pingUrl(String url){
        try {
            Process p = Runtime.getRuntime().exec("ping -c 1 " + url);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            stdInput.readLine();
            String ping = stdInput.readLine().split("time=")[1].split(" ")[0];

            p.destroy();
            return Double.parseDouble(ping);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private static File createFile(Context context, int megaBytes){
        int bytes = 1000*1000;
        OutputStream outputStream;
        File file = null;
        try {
            file = new File(context.getFilesDir().getAbsolutePath() + FILE_NAME);
            file.createNewFile();

            byte[] buf = new byte[bytes];
            outputStream = new BufferedOutputStream(new FileOutputStream(file));

            for(int i = 0; i < megaBytes; i++){
                outputStream.write(buf);
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

    public static File getFile(Context context){
        File file = new File(context.getFilesDir().getAbsolutePath() + FILE_NAME);
        if(file.length() <= 0){
            file = createFile(context, 1000);
        }

        return file;
    }

}
