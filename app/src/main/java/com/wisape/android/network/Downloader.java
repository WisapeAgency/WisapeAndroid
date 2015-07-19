package com.wisape.android.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class Downloader{
    public static final String ACTION_DOWNLOAD_DEFAULT = "com.wisape.android.action.DOWNLOADER";
    public static final String EXTRA_TOTAL_SIZE = "_total_size";
    public static final String EXTRA_TRANSFERRED_BYTES = "_transferred_bytes";
    public static final String EXTRA_PROGRESS = "_progress";

    public static void download(Context context, Uri source, Uri dest, String broadcastAction) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(source.toString()).addHeader("Content-Type", "application/json").build();
        Response response = client.newCall(request).execute();

        File destFile = new File(dest.getPath());
        Sink sink = new ForwardingSinkImpl(Okio.buffer(Okio.sink(destFile)), new ProgressListenerImpl(context, source, broadcastAction));
        try{
            BufferedSource bufferedSource = response.body().source();
            Buffer buffer = bufferedSource.buffer();
            for(;null != buffer && 0 < buffer.size();){
                sink.write(buffer, buffer.size());
                buffer.flush();
                sink.flush();
                buffer.close();
                buffer = bufferedSource.buffer();
            }
        }finally {
            sink.close();
        }
    }

    private static class ProgressListenerImpl implements com.android.volley.Response.ProgressListener {
        private Uri source;
        private LocalBroadcastManager broadcast;
        private String action;
        private volatile boolean ended;

        ProgressListenerImpl(Context context, Uri source, String broadcastAction){
            this.source = source;
            this.action = broadcastAction;
            this.broadcast = LocalBroadcastManager.getInstance(context);
        }

        @Override
        public void onProgress(long transferredBytes, long totalSize) {
            if(ended){
                return;
            }

            Intent intent = new Intent();
            String action;
            if(null == this.action || 0 == this.action.length()){
                action = ACTION_DOWNLOAD_DEFAULT;
            }else{
                action = this.action;
            }
            intent.setAction(action);
            intent.setData(source);
            intent.putExtra(EXTRA_TOTAL_SIZE, totalSize);
            intent.putExtra(EXTRA_TRANSFERRED_BYTES, transferredBytes);
            intent.putExtra(EXTRA_PROGRESS, ((double) transferredBytes / (double) totalSize));

            broadcast.sendBroadcast(intent);
            if(transferredBytes == totalSize){
                ended = true;
                broadcast = null;
                source = null;
            }
        }
    }

    private static class ForwardingSinkImpl extends ForwardingSink {
        private com.android.volley.Response.ProgressListener progressListener;
        private long totalBytesWritten = 0L;

        ForwardingSinkImpl(Sink delegate, com.android.volley.Response.ProgressListener progressListener){
            super(delegate);
            this.progressListener = progressListener;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            totalBytesWritten += byteCount;
            if(null != progressListener){
                progressListener.onProgress(byteCount, totalBytesWritten);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            if(null != progressListener){
                progressListener.onProgress(totalBytesWritten, totalBytesWritten);
                progressListener = null;
            }
        }
    }
}
