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
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SpeedTest {

    final MediaType BinType = MediaType.parse("application/x-binary; charset=utf-8");
    private static String uploadUrl = "";
    private static String downloadUrl = "";
    private static String pingURL = "nodespeed.forb.luas.ml";

    private static int initialTestLength = 2;
    private static int testLength = 8;

    private SpeedTestEventListener speedTestEventListener;

    private ArrayList<Response> responses;
    private DownloadTest[] downloadTasks;
    private UploadTest[] uploadTasks;

    private OkHttpClient client;
    private SpeedTestTools speedTools;
    private File uploadFile;
    private SpeedResults speedResults;
    private static SQLiteDatabaseHandler db;
    private static int currentTestLength = initialTestLength;
    private static boolean testComplete = true;
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
    private long lastUpdateGraphTime;

    private boolean initialDownload = true;

    public SpeedTest(Context context) {
        speedTools = new SpeedTestTools();
        responses = new ArrayList<>(threads);
        downloadTasks = new DownloadTest[threads];
        uploadTasks = new UploadTest[threads];

        lastUpdateGraphTime = System.currentTimeMillis();
        uploadFile = SpeedTestTools.getFile(context);
        speedResults = new SpeedResults();
        db = new SQLiteDatabaseHandler(context);
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

    public void stopTest(){
        testComplete = true;
    }

    public static int getCurrentTestLength(){
        return currentTestLength;
    }

    public static boolean getTestComplete(){
        return testComplete;
    }

    public void startTest(boolean sslOn){
        downloadErrorPresented = false;
        uploadErrorPresented = false;
        totalBytesRead = 0;
        totalBytesWritten = 0;
        lastDownloadSpeed = 0;
        lastUploadSpeed = 0;
        testComplete = false;
        speedResults.setSslOn(sslOn);
        speedResults.setPingTime(SpeedTestTools.pingUrl(pingURL));
        speedTestEventListener.onPingResult(speedResults.getPingTime());
        startInitialDownload();
    }

    public void speedTestEventListener(SpeedTestEventListener eventListener){
        speedTestEventListener = eventListener;
    }

    public static List<SpeedResults> getAllResults(){
        return db.allSpeedResults();
    }

    private void startInitialDownload(){
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
        speedTools.setDelayTime(0.8);
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

    private final ProgressResponseBody.ProgressListener downloadProgressListener = new ProgressResponseBody.ProgressListener() {
        @Override
        public void onProgress(final double speedMbps, final double elapsedTime, long byteCount) {
            if(initialDownload) { return; }
            lastDownloadSpeed = speedMbps;
            totalBytesRead += byteCount;

            boolean updateGraph = false;
            if(System.currentTimeMillis() - lastUpdateGraphTime > 400){
                updateGraph = true;
                lastUpdateGraphTime = System.currentTimeMillis();
            }

            speedTestEventListener.onDownloadChanged(speedMbps, elapsedTime, updateGraph);
        }
    };

    private final ProgressRequestBody.ProgressListener uploadProgressListener = new ProgressRequestBody.ProgressListener() {
        @Override
        public void onProgress(final double speedMbps, final double elapsedTime, long byteCount) {
            lastUploadSpeed = speedMbps;
            totalBytesWritten += byteCount;

            boolean updateGraph = false;
            if(System.currentTimeMillis() - lastUpdateGraphTime > 400){
                updateGraph = true;
                lastUpdateGraphTime = System.currentTimeMillis();
            }

            speedTestEventListener.onUploadChanged(speedMbps, elapsedTime, updateGraph);
        }
    };

    private void createClient(){
        List<Protocol> protocols = new ArrayList<>();
        protocols.add(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);

        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override public Response intercept(@NonNull Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), downloadProgressListener, speedTools))
                                .build();
                    }
                })
                .protocols(protocols)
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

            new CloseResponses().execute();
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
            long totalMB = totalBytesRead / (1000 * 1000);
            speedResults.setDownloadSpeed(lastDownloadSpeed);
            speedResults.setTotalDownloadMB(totalMB);
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

            new CloseResponses().execute();
            speedTestEventListener.onUploadComplete(lastUploadSpeed, totalBytesWritten);
            speedTestEventListener.testComplete();
            long totalMB = totalBytesWritten / (1000 * 1000);
            speedResults.setUploadSpeed(lastUploadSpeed);
            speedResults.setTotalUploadMB(totalMB);
            db.addSpeedResult(speedResults);
            testComplete = true;
        }
    }

    private class CloseResponses extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urlString) {
            try {
                for(Response r : responses){
                    if(r != null) {
                        r.close();
                    }
                }
                responses.removeAll(responses);
                client.dispatcher().cancelAll();
            } catch (IllegalStateException e) {
                Log.v("Close Response", "Socket closed on timeout");
            } catch (Exception e) {
                Log.e("Close Response", "Exception", e);
            }

            return null;
        }
    }
}
