package com.android.luas.networkspeedtest;

public interface SpeedTestEventListener {
    void onDownloadPreExecute();
    void onUploadPreExecute();
    void onDownloadChanged(final double speedMbps, final double elapsedTime);
    void onUploadChanged(final double speedMbps, final double elapsedTime);
    void onDownloadComplete(final double speedMbps);
    void onUploadComplete(final double speedMbps);
    void testComplete();
    void downloadError();
    void uploadError();
}
