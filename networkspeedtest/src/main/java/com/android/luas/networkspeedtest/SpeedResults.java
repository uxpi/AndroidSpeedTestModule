package com.android.luas.networkspeedtest;

public class SpeedResults {
    private int id;
    private String time;
    private double downloadSpeed;
    private double uploadSpeed;
    private long totalDownloadMB;
    private long totalUploadMB;
    private double pingTime;
    private boolean sslOn;

    SpeedResults() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(double downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public double getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(double uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public long getTotalDownloadMB() {
        return totalDownloadMB;
    }

    public void setTotalDownloadMB(long totalDownloadMB) {
        this.totalDownloadMB = totalDownloadMB;
    }

    public long getTotalUploadMB() {
        return totalUploadMB;
    }

    public void setTotalUploadMB(long totalUploadMB) {
        this.totalUploadMB = totalUploadMB;
    }

    public double getPingTime() {
        return pingTime;
    }

    public void setPingTime(double pingTime) {
        this.pingTime = pingTime;
    }

    public boolean isSslOn() {
        return sslOn;
    }

    public void setSslOn(boolean sslOn) {
        this.sslOn = sslOn;
    }
}
