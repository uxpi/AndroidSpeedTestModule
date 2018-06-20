package com.android.luas.networkspeedtest;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;
    private SpeedTestTools speedTools;

    ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener, SpeedTestTools speedTools) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        this.speedTools = speedTools;
    }

    @Override public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override public long contentLength() {
        return responseBody.contentLength();
    }

    @Override public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }


    private Source source(final Source source) {

        return new ForwardingSource(source) {

            @Override public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                double speedMbps = speedTools.speedTestCalc(bytesRead);

                if(speedMbps <= 0) {
                    return bytesRead;
                }

                if(speedTools.getElapsedTime() >= SpeedTest.getCurrentTestLength() || SpeedTest.getTestComplete()){
                    source.close();
                    return bytesRead;
                }

                progressListener.onProgress(speedMbps, speedTools.getElapsedTime(), bytesRead);

                return bytesRead;
            }
        };
    }

    public interface ProgressListener {
        void onProgress(double mbps, double elapsedTime, long byteCount);
    }
}