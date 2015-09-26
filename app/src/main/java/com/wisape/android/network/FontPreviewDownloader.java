package com.wisape.android.network;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.util.EnvironmentUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/8/16.
 */
public class FontPreviewDownloader implements Runnable {
    private BlockingQueue<StoryFontInfo> downloadQueue;

    public FontPreviewDownloader(BlockingQueue<StoryFontInfo> downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        try {
            while (isRunning) {
                StoryFontInfo fontInfo = downloadQueue.poll(30, TimeUnit.SECONDS);
                if (null != fontInfo) {
                    download(fontInfo);
                } else {
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void download(StoryFontInfo fontInfo) {
        if (!EnvironmentUtils.isMounted()) {
            return;
        }

        File parent = new File(StoryManager.getStoryFontDirectory(), fontInfo.name);
        File destFile = new File(parent, "preview.jpg");

        if (!parent.exists() || !parent.isDirectory()) {
            parent.mkdirs();
        }
        if (destFile.exists()) {
            return;
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fontInfo.preview_img)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Accept-Encoding", "identity").build();
        InputStream input = null;
        OutputStream output = null;
        try {
            destFile.createNewFile();
            Response response = client.newCall(request).execute();
            input = response.body().byteStream();
            output = new BufferedOutputStream(new FileOutputStream(destFile));

            int count;
            byte[] buffer = new byte[1024 * 5];
            while (0 < (count = input.read(buffer))) {
                output.write(buffer, 0, count);
            }
        } catch (IOException e) {
            Log.e("DataSynchronizer", "", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (Exception e) {

                }
            }
            if (null != output) {
                try {
                    output.close();
                } catch (Exception e) {

                }
            }
        }
    }

}
