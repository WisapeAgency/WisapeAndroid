package com.wisape.android.network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.LogUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class Downloader{
    private static final String TAG = "Downloader";
    private static final String ACTION_DOWNLOAD_DEFAULT = "com.wisape.android.action.DOWNLOADER";
    public static final String EXTRA_TOTAL_SIZE = "extra_total_size";
    public static final String EXTRA_TRANSFERRED_BYTES = "extra_transferred_bytes";
    public static final String EXTRA_PROGRESS = "extra_progress";
    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_TAG = "extra_tag";

    private static ConcurrentLinkedQueue<Uri> queue = new ConcurrentLinkedQueue();

    public static boolean containsDownloader(Uri source){
        return queue.contains(source);
    }

    public static boolean removeDownloader(Uri source){
        return queue.remove(source);
    }

    public interface DownloaderCallback{
        void onDownloading(double progress);
        void onCompleted(Uri uri);
        void onError(Uri uri);
    }

    public static void download(Context context, Uri source, Uri dest, String broadcastAction, Bundle tag) throws IOException {
        queue.add(source);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(source.toString())
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Accept-Encoding", "identity").build();
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

            File parent = destFile.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            if(destFile.exists()){
                destFile.delete();
            }
            destFile.createNewFile();

            input = response.body().byteStream();
            output = new BufferedOutputStream(new FileOutputStream(destFile));
            final boolean hasBroadcast = null != broadcastAction && 0 < broadcastAction.length();
            String action = ACTION_DOWNLOAD_DEFAULT;
            if(hasBroadcast){
                broadcast = LocalBroadcastManager.getInstance(context);
                action = broadcastAction;
            }
            Log.d(TAG, "#download action:" + action + ", hasBroadcast:" + hasBroadcast);

            int count;
            byte[] buffer = new byte[1024 * 5];
            double progress;
            double preProgress = 0d;
            for(;0 < (count = input.read(buffer));){
                download += count;
                output.write(buffer, 0, count);

                if(hasBroadcast){
                    progress = ((double) download / (double) length);
                    //1%
                    if(0.01d <= (progress - preProgress)){
                        preProgress = progress;
                        Intent intent = new Intent(action);
                        intent.putExtra(EXTRA_SOURCE, source);
                        intent.putExtra(EXTRA_TOTAL_SIZE, length);
                        intent.putExtra(EXTRA_TRANSFERRED_BYTES, download);
                        if(null != tag){
                            intent.putExtra(EXTRA_TAG, tag);
                        }
                        intent.putExtra(EXTRA_PROGRESS, progress);
                        broadcast.sendBroadcast(intent);
                    }
                }
                //Log.d(TAG, "#download download:" + download + ", length:" + length);
            }
            output.flush();
            Log.d(TAG, "#download end !!!");
        } finally {
            if(null != input){
                input.close();
            }

            if(null != output){
                output.close();
            }
        }
        removeDownloader(source);
    }

    public static void download(Uri source, Uri dest, DownloaderCallback callback) {
        if(!EnvironmentUtils.isMounted()){
            callback.onError(source);
        }
        queue.add(source);
        File destFile = new File(dest.getPath());
        File parent = destFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            destFile.createNewFile();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(source.toString())
                    .addHeader("Content-Type", "application/octet-stream")
                    .addHeader("Accept-Encoding", "identity").build();
            Response response = client.newCall(request).execute();

            long length, download = 0;
            length = Long.parseLong(response.header("Content-Length", "0"));
            input = response.body().byteStream();
            output = new BufferedOutputStream(new FileOutputStream(destFile));

            int count;
            byte[] buffer = new byte[1024 * 5];
            double progress, preProgress = 0d;
            for (; 0 < (count = input.read(buffer)); ) {
                download += count;
                output.write(buffer, 0, count);
                progress = ((double) download / (double) length);
                //1%
                if (0.01d <= (progress - preProgress)) {
                    preProgress = progress;
                    callback.onDownloading(progress);
                }
            }
            callback.onCompleted(dest);
            LogUtil.d("download end!");
        }catch (IOException e){
            LogUtil.d("download error!" + e.getMessage());
            callback.onError(source);
        } finally {
            if (null != input) {
                try {
                    input.close();
                }catch (Exception e){

                }
            }
            if (null != output) {
                try {
                    output.close();
                }catch (Exception e){

                }
            }
        }
        removeDownloader(source);
    }
}
