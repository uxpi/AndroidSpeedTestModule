package com.android.luas.networkspeedtest;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class ProgressRequestBody extends RequestBody {

    private RequestBody requestBody;
    private ProgressListener progressListener;
    private SpeedTestTools speedTools;

    ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener, SpeedTestTools speedTools) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
        this.speedTools = speedTools;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return requestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        CountingSink mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {

        CountingSink(Sink delegate) {
            super(delegate);
        }
        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            double speedMbps = speedTools.speedTestCalc(byteCount);

            if(speedMbps <= 0) {
                return;
            }

            if(speedTools.getElapsedTime() >= SpeedTest.getCurrentTestLength() || SpeedTest.getTestComplete()){
                delegate().close();
                source.close();
                return;
            }

            progressListener.onProgress(speedMbps, speedTools.getElapsedTime(), byteCount);
        }
    }

    public interface ProgressListener {
        void onProgress(double mbps, double elapsedTime, long byteCount);
    }
}