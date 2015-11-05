package com.wisape.android.network;

import android.net.Uri;
import android.util.Log;

import com.parse.codec.digest.DigestUtils;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/9/24.
 */
public class TemplateDownloader implements Runnable {
    private static final String TEMPLATE_NAME = "stage.html";
    private static final String FONT_FAMILY = "font-family";

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
            try{
                InputStream is = new FileInputStream(destFile);
                String md5 = DigestUtils.md5Hex(is);
                if (md5.equals(templateInfo.hash_code)){
                    return;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
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
        downloadFont(template);
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
            e.printStackTrace();
            File destFile = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + ".zip");
            if (destFile.exists()){
                try{
                    InputStream is = new FileInputStream(destFile);
                    String md5 = DigestUtils.md5Hex(is);
                    if (md5.equals(templateInfo.hash_code)){
                        unzipTemplate(downUri, template, templateInfo);
                    } else {
                        restartDownload(downUri, template, templateInfo);
                    }
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            } else {
                restartDownload(downUri, template, templateInfo);
            }
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

    public void downloadFont(File template){
        BlockingQueue<String> fontQueue = new LinkedBlockingQueue<>();
        Set<String> fontSet = parseFont(template);
        if (fontSet.size() == 0){
            return;
        }
        File fontDirectory = StoryManager.getStoryFontDirectory();
        for(String fontName : fontSet){
            File font = new File(fontDirectory, fontName);
            if(!font.exists() || font.list().length <= 1){
//                Bundle args = new Bundle();
//                args.putString(EXTRA_FONT_NAME, fontName);
//                startLoad(WHAT_DOWNLOAD_FONT, args);
                fontQueue.offer(fontName);
            }
        }
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new FontDownloader(fontQueue));
        service.shutdown();
    }

    private Set<String> parseFont(File template) {
        Set<String> fontSet = new HashSet<>();
        File file = new File(template, TEMPLATE_NAME);
        if (!file.exists()) {
            return fontSet;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(FONT_FAMILY)) {
                    line = line.substring(line.indexOf(FONT_FAMILY));
                    line = line.substring(0, line.indexOf(";"));
                    String font = line.split(":")[1].trim().replace("'","");
                    fontSet.add(font);
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.d("StoryTemplate", "Error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
        return fontSet;
    }
}
