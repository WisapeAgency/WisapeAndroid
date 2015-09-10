package com.wisape.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.WebSettings;

import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CordovaPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity{
    private static final String START_URL = "file:///android_asset/www/views/editor_index.html";
    private static final String TEMPLATE_NAME = "stage.html";
    private static final String DOWNLOAD_PROGRESS = "progress";
    private static final String EXTRA_STORY_ID = "story_id";
    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "temp_id";
    private static final String EXTRA_TEMPLATE_NAME = "temp_name";
    private static final String EXTRA_TEMPLATE_PATH = "temp_path";
    private static final String EXTRA_TEMPLATE_URL = "temp_url";
    private static final String EXTRA_FONT_NAME = "font_name";
    private static final String FONT_FAMILY = "font-family";
    private static final String FONT_FILE_NAME = "fonts.css";

    private static final int WHAT_DOWNLOAD_TEMPLATE = 0x01;
    private static final int WHAT_DOWNLOAD_FONT = 0x02;
    private static final int WHAT_DOWNLOAD_PROGRESS = 0x03;
    private static final int WHAT_DOWNLOAD_COMPLETED = 0x04;
    private static final int WHAT_DOWNLOAD_ERROR = 0x05;
    private static final int WHAT_DOWNLOAD_FONT_COMPLETED = 0x06;

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), StoryTemplateActivity.class);
//        intent.putExtra(EXTRA_STORY_ID,storyId);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Fragment fragment, int requestCode){
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), StoryTemplateActivity.class);
//        intent.putExtra(EXTRA_STORY_ID,storyId);
        fragment.startActivityForResult(intent, requestCode);
    }
    private Queue<String> fontQueue = new LinkedBlockingQueue<>();
    private Handler downloadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case WHAT_DOWNLOAD_PROGRESS:{
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID,0);
                    int categoryId = msg.getData().getInt(EXTRA_CATEGORY_ID,0);
                    double progress = msg.getData().getDouble(DOWNLOAD_PROGRESS, 0);
                    try{
                        JSONObject json = new JSONObject();
                        json.put("id",id);
                        json.put("category_id",categoryId);
                        json.put("progress",progress);
                        loadUrl("javascript:onDownloading(" + json.toString() + ")");
                    }catch(JSONException e){

                    }
                    break;
                }
                case WHAT_DOWNLOAD_COMPLETED:{
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID,0);
                    int categoryId = msg.getData().getInt(EXTRA_CATEGORY_ID,0);
                    String name = msg.getData().getString(EXTRA_TEMPLATE_NAME);
                    String path = msg.getData().getString(EXTRA_TEMPLATE_PATH);
                    File template = getTemplateUnzipDirectory(name);
                    try{
                        JSONObject json = new JSONObject();
                        json.put("id",id);
                        json.put("category_id",categoryId);
                        json.put("path", template);
                        System.out.println("Path:" + template);
                        loadUrl("javascript:onCompleted(" + json.toString() + ")");
                    } catch (JSONException e){

                    }
                    unzipTemplate(Uri.fromFile(new File(path)), template);
                    downloadFont(template);
                    break;
                }
                case WHAT_DOWNLOAD_ERROR:{
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID,0);
                    loadUrl("javascript:onError('"  +id + "!')");
                    break;
                }

            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_FILE)
                .encodedPath(EnvironmentUtils.getAppDataDirectory().getPath())
                .appendEncodedPath("template_light/index.html").build();
        String url = uri.toString();
        Log.d(TAG, "#onCreate url:" + url);
        loadUrl(START_URL);
    }

    public void downloadTemplate(String data, int id,int categoryId) throws JSONException{
        JSONObject json = new JSONObject(data);
        String name = json.getString(EXTRA_TEMPLATE_NAME);
        String url = json.getString(EXTRA_TEMPLATE_URL);

        Bundle args = new Bundle();
        args.putInt(EXTRA_TEMPLATE_ID, id);
        args.putInt(EXTRA_CATEGORY_ID, categoryId);
        args.putString(EXTRA_TEMPLATE_NAME, name);
        args.putString(EXTRA_TEMPLATE_URL, url);
        startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
    }

    public void downloadFont(File template){
        Set<String> fontSet = parseFont(template);
        if (fontSet.size() == 0){
            return;
        }
        File fontDirectory = StoryManager.getStoryFontDirectory();
        for(File file : fontDirectory.listFiles()){
            if(file.isDirectory() && !fontSet.contains(file.getName())){
                fontSet.remove(file.getName());
            }
        }
        fontQueue.addAll(fontSet);
        Bundle args = new Bundle();
        args.putString(EXTRA_FONT_NAME, fontQueue.poll());
        startLoad(WHAT_DOWNLOAD_FONT, args);
    }

    private Set<String> parseFont(File template){
        Set<String> fontSet = new HashSet<>();
        File file = new File(template, TEMPLATE_NAME);
        if (!file.exists()){
            return fontSet;
        }
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(FONT_FAMILY)) {
                    line = line.substring(line.indexOf(FONT_FAMILY));
                    line = line.substring(0, line.indexOf(";"));
                    String font = line.split(":")[1].trim();
                    fontSet.add(font);
                }
            }
            reader.close();
        }catch (IOException e){
            Log.d("StoryTemplate", "Error", e);
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){

                }
            }
        }
        return fontSet;
    }

    public void invokeJavascriptTest(){
        loadUrl("javascript:test2()");
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        final Message msg = Message.obtain();
        msg.what = what;
        switch (what){
            default :
                return null;
            case WHAT_DOWNLOAD_TEMPLATE:{
                final int id = args.getInt(EXTRA_TEMPLATE_ID, 0);
                final int categoryId = args.getInt(EXTRA_CATEGORY_ID,0);
                final String name = args.getString(EXTRA_TEMPLATE_NAME);
                final String url = args.getString(EXTRA_TEMPLATE_URL);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryTemplateDirectory(), name));
                Downloader.download(Uri.parse(url),dest, new Downloader.DownloaderCallback(){
                    public void onDownloading(double progress){
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        bundle.putInt(EXTRA_CATEGORY_ID,categoryId);
                        bundle.putDouble(DOWNLOAD_PROGRESS, progress);
                        msg.what = WHAT_DOWNLOAD_PROGRESS;
                        msg.setData(bundle);
                        downloadHandler.sendMessage(msg);
                    }
                    public void onCompleted(Uri downUri){
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_COMPLETED;
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        bundle.putInt(EXTRA_CATEGORY_ID,categoryId);
                        bundle.putString(EXTRA_TEMPLATE_NAME, name);
                        bundle.putString(EXTRA_TEMPLATE_PATH,downUri.getPath());
                        msg.setData(bundle);
                        downloadHandler.sendMessage(msg);
                    }

                    public void onError(Uri uri){
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_ERROR;
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        msg.setData(bundle);
                        downloadHandler.sendMessage(msg);
                    }
                });
                break;
            }
            case WHAT_DOWNLOAD_FONT:{
                final String name = args.getString(EXTRA_FONT_NAME);
//                final String name = "Trebuc";
                Uri uri = WWWConfig.acquireUri(getString(R.string.uri_font_download));
                String url = String.format("%s?name=%s", uri.toString(), name);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryFontDirectory(), name + ".zip"));
                Downloader.download(Uri.parse(url),dest, new Downloader.DownloaderCallback(){
                    public void onDownloading(double progress){

                    }
                    public void onCompleted(Uri downUri){
//                        File font = getFontUnzipDirectory(name);
                        File font = StoryManager.getStoryFontDirectory();
                        unzipFont(downUri, font);
                        appendFont(name);

                        if(fontQueue.size() != 0){
                            Bundle args = new Bundle();
                            args.putString(EXTRA_FONT_NAME, fontQueue.poll());
                            startLoad(WHAT_DOWNLOAD_FONT, args);
                        }
                    }

                    public void onError(Uri uri){

                    }
                });
                break;
            }
        }
        return msg;
    }

    private synchronized void appendFont(String fontName){
        File fonts = new File(StoryManager.getStoryFontDirectory(),FONT_FILE_NAME);
        PrintWriter writer = null;
        try {
            if (!fonts.exists()) {
                fonts.createNewFile();
            }
            writer = new PrintWriter(new FileWriter(fonts, true));
            writer.println("@font-face {");
            writer.println(String.format("    font-family: '%s';",fontName));
            writer.println(String.format("    src: url('%s/%s.eot');",fontName,fontName));
            writer.println(String.format("    src: url('%s/%s.eot?#iefix') format('embedded-opentype'),",fontName,fontName));
            writer.println(String.format("    url('%s/%s.woff') format('woff'),",fontName,fontName));
            writer.println(String.format("    url('%s/%s.ttf') format('truetype'),",fontName,fontName));
            writer.println(String.format("    url('%s/%s.svg') format('svg');",fontName,fontName));
            writer.println("    font-weight: normal;");
            writer.println("    font-style: normal;");
            writer.println("}");
        }catch (Exception e){

        }finally {
            if (writer != null){
                writer.close();
            }
        }

    }

    private File getTemplateUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if(0 < index){
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryTemplateDirectory(), templateDir);
    }

    private File getFontUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if(0 < index){
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryFontDirectory(), templateDir);
    }

    private void unzipTemplate(Uri downUri, File template) {
        try {
            if(template.isFile()){
                FileUtils.forceDelete(template);
            } else {
                FileUtils.deleteDirectory(template);
            }
            ZipUtils.unzip(downUri, template);
        }catch (IOException e){
            Log.e(TAG, "", e);
            loadUrl("javascript:onError('unzip error!')");
        }
    }

    private void unzipFont(Uri downUri, File font) {
        try {
            ZipUtils.unzip(downUri, font);
        }catch (IOException e){
            Log.e(TAG, "", e);
            loadUrl("javascript:onError('unzip error!')");
        }
    }

    @Override
    protected void onLoadCompleted(Message args) {
        if(isDestroyed() || null == args){
            return;
        }
        switch (args.what){
            case WHAT_DOWNLOAD_TEMPLATE:{

                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
