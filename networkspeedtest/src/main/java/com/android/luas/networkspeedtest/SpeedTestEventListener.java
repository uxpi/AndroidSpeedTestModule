package com.android.luas.networkspeedtest;

public interface SpeedTestEventListener {
    void onDownloadPreExecute();
    void onUploadPreExecute();
    void onDownloadChanged(final double speedMbps, final double elapsedTime);
    void onUploadChanged(final double speedMbps, final double elapsedTime);
    void onDownloadComplete(final double speedMbps, final long totalBytesRead);
    void onUploadComplete(final double speedMbps, final long totalBytesWritten);
    void testComplete();
    void downloadError();
    void uploadError();
}
