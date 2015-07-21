package com.wisape.android.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class Downloader{
    private static final String TAG = "Downloader";
    private static final String ACTION_DOWNLOAD_DEFAULT = "com.wisape.android.action.DOWNLOADER";
    public static final String EXTRA_TOTAL_SIZE = "_total_size";
    public static final String EXTRA_TRANSFERRED_BYTES = "_transferred_bytes";
    public static final String EXTRA_PROGRESS = "_progress";

    public static void download(Context context, Uri source, Uri dest, String broadcastAction) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(source.toString()).addHeader("Content-Type", "application/octet-stream").build();
        Response response = client.newCall(request).execute();

        File destFile = new File(dest.getPath());
        InputStream input = null;
        OutputStream output = null;
        long download = 0;
        LocalBroadcastManager broadcast = null;
        try{
            Log.d(TAG, "#download read and write; response:" + response.toString());
            String contentLength = response.header("Content-Length", "0");
            Log.d(TAG, "#download contentLength:" + contentLength);
            long length;
            try{
                length = Long.parseLong(contentLength);
            }catch (Throwable e){
                Log.e(TAG, "", e);
                length = 1024 * 1024 * 1024;
            }

            input = response.body().byteStream();
            output = new BufferedOutputStream(new FileOutputStream(destFile));
            final boolean hasBroadcast = null != broadcastAction && 0 < broadcastAction.length();
            String action = ACTION_DOWNLOAD_DEFAULT;
            if(hasBroadcast){
                broadcast = LocalBroadcastManager.getInstance(context);
                action = broadcastAction;
            }

            int count;
            byte[] buffer = new byte[1024 * 5];
            for(;0 < (count = input.read(buffer));){
                download += count;
                output.write(buffer, 0, count);

                if(hasBroadcast){
                    Intent intent = new Intent();
                    intent.setAction(action);
                    intent.setData(source);
                    intent.putExtra(EXTRA_TOTAL_SIZE, length);
                    intent.putExtra(EXTRA_TRANSFERRED_BYTES, download);
                    intent.putExtra(EXTRA_PROGRESS, ((double) download / (double) length));
                    broadcast.sendBroadcast(intent);
                }
                //Log.d(TAG, "#download download:" + download + ", length:" + length);
            }
            output.flush();
        }finally {
            if(null != input){
                input.close();
            }

            if(null != output){
                output.close();
            }
        }
    }
}
