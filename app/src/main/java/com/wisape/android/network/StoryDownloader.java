package com.wisape.android.network;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/10/10.
 */
public class StoryDownloader implements Runnable {
    private BlockingQueue<StoryEntity> downloadQueue = new LinkedBlockingQueue<>();

    public StoryDownloader(List<StoryEntity> downloadQueue) {
        this.downloadQueue.addAll(downloadQueue);
    }

    @Override
    public void run() {
        boolean isRunning = true;
        try {
            while (isRunning) {
                StoryEntity story = downloadQueue.poll(30, TimeUnit.SECONDS);
                if (null != story && !story.status.equals(ApiStory.AttrStoryInfo.STORY_DEFAULT)) {
                    download(story);
                } else {
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void download(StoryEntity story) {
        if (!EnvironmentUtils.isMounted()) {
            return;
        }
        File destFile = new File(StoryManager.getStoryDirectory(), story.storyLocal + ".zip");
        if (destFile.exists()){
            return;
        }
        File parent = destFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(story.storyPath)
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
        String path = destFile.getAbsolutePath();
        File storyDirectory = new File(destFile.getParent(),story.storyLocal);
        unzipTemplate(Uri.fromFile(new File(path)), storyDirectory, story);
    }

    private void unzipTemplate(Uri downUri, File storyDirectory,StoryEntity story) {
        try {
            if (storyDirectory.isFile()) {
                FileUtils.forceDelete(storyDirectory);
            } else {
                FileUtils.deleteDirectory(storyDirectory);
            }
            ZipUtils.unzip(downUri, storyDirectory);
        } catch (IOException e) {
            restartDownload(downUri, storyDirectory, story);
        }
    }

    private void restartDownload(Uri downUri, File template,StoryEntity story){
        try {
            if (template.isFile()) {
                FileUtils.forceDelete(template);
            } else {
                FileUtils.deleteDirectory(template);
            }
            FileUtils.forceDelete(new File(downUri.getPath()));
        }catch (Exception e1){

        }
        download(story);
    }
}
