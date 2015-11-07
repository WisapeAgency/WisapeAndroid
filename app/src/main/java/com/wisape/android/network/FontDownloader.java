package com.wisape.android.network;

import android.net.Uri;
import android.util.Log;

//import com.parse.codec.digest.DigestUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/8/16.
 */
public class FontDownloader implements Runnable {
    private BlockingQueue<?> downloadQueue;

    public FontDownloader(BlockingQueue<?> downloadQueue) {
        this.downloadQueue = downloadQueue;
    }

    @Override
    public void run() {
        boolean isRunning = true;
        try {
            while (isRunning) {
                Object object = downloadQueue.poll(30, TimeUnit.SECONDS);
                if (object == null){
                    isRunning = false;
                }else{
                    if (object instanceof StoryFontInfo){
                        download((StoryFontInfo) object);//字体数据同步
                    } else {
                        download((String) object);//下载字体
                    }
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
        Uri uri = WWWConfig.acquireUri(WisapeApplication.getInstance().getString(R.string.uri_font_download));
        String fontUrl = String.format("%s?name=%s", uri.toString(), fontInfo.name);
        File destFile = new File(StoryManager.getStoryFontDirectory(), fontInfo.name + ".zip");
        if (!destFile.exists()){//不存在则没有下载过这个字体文件，不用比较md5判断是否重新下载
            return;
        }
        try{
            InputStream is = new FileInputStream(destFile);
//            String md5 = DigestUtils.md5Hex(is);
//            if (md5.equals(fontInfo.hash_code)){
//                return;
//            }
        }catch (IOException e){
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fontUrl)
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
        File font = StoryManager.getStoryFontDirectory();
        unzipFont(Uri.fromFile(destFile), font);
    }

    private void download(String fontName) {
        if (!EnvironmentUtils.isMounted()) {
            return;
        }
        Uri uri = WWWConfig.acquireUri(WisapeApplication.getInstance().getString(R.string.uri_font_download));
        String fontUrl = String.format("%s?name=%s", uri.toString(), fontName);
        File destFile = new File(StoryManager.getStoryFontDirectory(), fontName + ".zip");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(fontUrl)
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
        File font = StoryManager.getStoryFontDirectory();
        unzipFont(Uri.fromFile(destFile), font);
    }

    private void unzipFont(Uri downUri, File font) {
        try {
            ZipUtils.unzip(downUri, font);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
