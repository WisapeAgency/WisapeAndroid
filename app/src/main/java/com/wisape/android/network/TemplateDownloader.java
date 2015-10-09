package com.wisape.android.network;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/9/24.
 */
public class TemplateDownloader implements Runnable {
    private BlockingQueue<StoryTemplateInfo> downloadQueue;

    public TemplateDownloader(BlockingQueue<StoryTemplateInfo> downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        try {
            while (isRunning) {
                StoryTemplateInfo templateInfo = downloadQueue.poll(30, TimeUnit.SECONDS);
                if (null != templateInfo) {
                    download(templateInfo);
                } else {
                    isRunning = false;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void download(StoryTemplateInfo templateInfo) {
        if (!EnvironmentUtils.isMounted()) {
            return;
        }
        if (DataSynchronizer.getInstance().getFirstTemplate() == null){
            DataSynchronizer.getInstance().setFirstTemplate(templateInfo);
        }
        File destFile = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + ".zip");
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
        Request request = new Request.Builder().url(templateInfo.temp_url)
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
        String name = templateInfo.temp_name;
        String path = destFile.getAbsolutePath();
        File template = getTemplateUnzipDirectory(name);
        unzipTemplate(Uri.fromFile(new File(path)), template,templateInfo);
    }

    private File getTemplateUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if (0 < index) {
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryTemplateDirectory(), templateDir);
    }

    private void unzipTemplate(Uri downUri, File template,StoryTemplateInfo templateInfo) {
        try {
            if (template.isFile()) {
                FileUtils.forceDelete(template);
            } else {
                FileUtils.deleteDirectory(template);
            }
            ZipUtils.unzip(downUri, template);
        } catch (IOException e) {
            restartDownload(downUri, template, templateInfo);
        }
    }

    private void restartDownload(Uri downUri, File template,StoryTemplateInfo templateInfo){
        try {
            if (template.isFile()) {
                FileUtils.forceDelete(template);
            } else {
                FileUtils.deleteDirectory(template);
            }
            FileUtils.forceDelete(new File(downUri.getPath()));
        }catch (Exception e1){

        }
        download(templateInfo);
    }
}
