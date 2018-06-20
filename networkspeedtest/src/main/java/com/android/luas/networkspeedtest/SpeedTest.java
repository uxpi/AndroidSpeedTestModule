package com.android.luas.networkspeedtest;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpeedTest {

    final MediaType BinType = MediaType.parse("application/x-binary; charset=utf-8");
    private String uploadUrl = "https://nodespeed.forb.luas.ml/upload";
    private String downloadUrl = "https://nodespeed.forb.luas.ml/download";

    private static int initialTestLength = 2;
    private static int testLength = 8;

    private SpeedTestEventListener speedTestEventListener;

    private ArrayList<Response> responses;
    private DownloadTest[] downloadTasks;
    private UploadTest[] uploadTasks;

    private OkHttpClient client;
    private SpeedTestTools speedTools;
    private File uploadFile;
    public static int currentTestLength = initialTestLength;
    private final static int threads = 4;
    private int callsToDownloadPostExecute = 0;
    private int callsToUploadPostExecute = 0;
    private int callsToDownloadPreExecute = 0;
    private int callsToUploadPreExecute = 0;
    private int callsToInitialDownloadPostExecute = 0;
    private boolean downloadErrorPresented = false;
    private boolean uploadErrorPresented = false;
    private double lastDownloadSpeed = 0.00;
    private double lastUploadSpeed = 0.00;
    private long totalBytesRead = 0;
    private long totalBytesWritten = 0;
    private int numInitialDownloads = 1;

    private boolean initialDownload = true;

    public SpeedTest(Context context) {
        speedTools = new SpeedTestTools();
        responses = new ArrayList<>(threads);
        downloadTasks = new DownloadTest[threads];
        uploadTasks = new UploadTest[threads];

        uploadFile = SpeedTestTools.getFile(context);
    }

    public void setTestLength(int length){
        testLength = length;
    }

    public void setUploadUrl(String url){
        this.uploadUrl = url;
    }

    public void setDownloadUrl(String url){
        this.downloadUrl = url;
    }

    public void startTest(){
        downloadErrorPresented = false;
        uploadErrorPresented = false;
        totalBytesRead = 0;
        totalBytesWritten = 0;
        startInitialDownload();
    }

    public void speedTestEventListener(SpeedTestEventListener eventListener){
        speedTestEventListener = eventListener;
    }

    public void startInitialDownload(){
        callsToInitialDownloadPostExecute = 0;
        initialDownload = true;
        currentTestLength = initialTestLength;
        speedTools.resetTime();
        createClient();
        for(int i = 0; i < threads; i++){
            new InitialDownload().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadUrl);
        }
    }

    private void startDownload(){
        initialDownload = false;
        currentTestLength = testLength;
        createClient();
        speedTools.resetTime();
        speedTools.setDelayTime(2.0);
        callsToDownloadPostExecute = 0;
        callsToDownloadPreExecute = 0;
        for(int i = 0; i < threads; i++){
            downloadTasks[i] = new DownloadTest();
            downloadTasks[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadUrl);
        }
    }

    private void startUpload(){
        createClient();
        speedTools.resetTime();
        speedTools.setDelayTime(0.5);
        callsToUploadPostExecute = 0;
        callsToUploadPreExecute = 0;
        for(int i = 0; i < threads; i++){
            uploadTasks[i] = new UploadTest();
            uploadTasks[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uploadUrl);
        }
    }

    final ProgressResponseBody.ProgressListener downloadProgressListener = new ProgressResponseBody.ProgressListener() {
        @Override
        public void onProgress(final double speedMbps, final double elapsedTime, long byteCount) {
            if(initialDownload) { return; }
            lastDownloadSpeed = speedMbps;
            totalBytesRead += byteCount;
            speedTestEventListener.onDownloadChanged(speedMbps, elapsedTime);
        }
    };

    final ProgressRequestBody.ProgressListener uploadProgressListener = new ProgressRequestBody.ProgressListener() {
        @Override
        public void onProgress(final double speedMbps, final double elapsedTime, long byteCount) {
            lastUploadSpeed = speedMbps;
            totalBytesWritten += byteCount;
            speedTestEventListener.onUploadChanged(speedMbps, elapsedTime);
        }
    };


    private void createClient(){
        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override public Response intercept(@NonNull Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), downloadProgressListener, speedTools))
                                .build();
                    }
                })
                .cache(null)
                .retryOnConnectionFailure(false)
                .socketFactory(new CustomSocketFactory())
                .build();
    }

    private class InitialDownload extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urlString) {
            try {
                URL url = new URL(urlString[0]);
                Request request = new Request.Builder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
                responses.add(client.newCall(request).execute());
                responses.get(responses.size()-1).body().bytes();
                responses.get(responses.size()-1).close();
            } catch (IllegalStateException e){
                Log.v("Download", "Socket closed on timeout");
            } catch (Exception e) {
                Log.e("Download", "Exception", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            callsToInitialDownloadPostExecute++;
            if(callsToInitialDownloadPostExecute < threads){
                return;
            }

            closeResponses();
            numInitialDownloads--;
            if(numInitialDownloads > 0){
                startInitialDownload();
            } else {
                startDownload();
            }
        }
    }

    private class DownloadTest extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callsToDownloadPreExecute++;
            if(callsToDownloadPreExecute < threads){
                return;
            }
            speedTestEventListener.onDownloadPreExecute();
        }

        @Override
        protected Void doInBackground(String... urlString) {
            try {
                URL url = new URL(urlString[0]);
                Request request = new Request.Builder()
                        .url(url)
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
                responses.add(client.newCall(request).execute());
                responses.get(responses.size()-1).body().bytes();
                responses.get(responses.size()-1).close();
            } catch (IllegalStateException e){
                Log.v("Download", "Socket closed on timeout");
            } catch (Exception e) {
                Log.e("Download", "Exception", e);
                synchronized(this){
                    if(!downloadErrorPresented){
                        downloadErrorPresented = true;
                        speedTestEventListener.downloadError();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            callsToDownloadPostExecute++;
            if(callsToDownloadPostExecute < threads){
                return;
            }

            speedTestEventListener.onDownloadComplete(lastDownloadSpeed, totalBytesRead);
            //speedTestEventListener.testComplete();
            //closeResponses();
            startUpload();
        }
    }

    private class UploadTest extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            callsToUploadPreExecute++;
            if(callsToUploadPreExecute < threads){
                return;
            }
            speedTestEventListener.onUploadPreExecute();
        }

        @Override
        protected Void doInBackground(String... urlString) {
            try {
                URL url = new URL(urlString[0]);

                RequestBody body = RequestBody.create(BinType, uploadFile);
                ProgressRequestBody pBody = new ProgressRequestBody(body, uploadProgressListener, speedTools);
                Request request = new Request.Builder()
                        .url(url)
                        .post(pBody)
                        .build();
                responses.add(client.newCall(request).execute());
                responses.get(responses.size() - 1).close();
            } catch (IllegalStateException e) {
                Log.v("Upload", "Socket closed on timeout");
            } catch (ProtocolException e){
                Log.v("Upload", "Socket closed on timeout");
            } catch (Exception e) {
                Log.e("Upload", "Exception", e);
                synchronized(this){
                    if(!uploadErrorPresented){
                        uploadErrorPresented = true;
                        speedTestEventListener.uploadError();
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            callsToUploadPostExecute++;
            if(callsToUploadPostExecute < threads){
                return;
            }

            closeResponses();
            speedTestEventListener.onUploadComplete(lastUploadSpeed, totalBytesWritten);
            speedTestEventListener.testComplete();
        }
    }

    private void closeResponses(){
        for(Response r : responses){
            if(r != null) {
                r.close();
            }
        }
        responses.removeAll(responses);

        client.dispatcher().cancelAll();
    }
}
