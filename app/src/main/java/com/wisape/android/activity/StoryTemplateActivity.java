package com.wisape.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity{
    private static final String START_URL = "file:///android_asset/www/views/editor_index.html";
    private static final String TEMPLATE_NAME = "stage.html";
    private static final String EXTRA_TEMPLATE_ID = "temp_id";
    private static final String EXTRA_TEMPLATE_NAME = "temp_name";
    private static final String EXTRA_TEMPLATE_URL = "temp_url";
    private static final String EXTRA_FONT_NAME = "font_name";

    private static final int WHAT_DOWNLOAD_TEMPLATE = 0x01;
    private static final int WHAT_DOWNLOAD_FONT = 0x02;

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), StoryTemplateActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Fragment fragment, int requestCode){
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), StoryTemplateActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri uri = new Uri.Builder().scheme(ContentResolver.SCHEME_FILE).encodedPath(EnvironmentUtils.getAppDataDirectory().getPath()).appendEncodedPath("template_light/index.html").build();
        String url = uri.toString();
        Log.d(TAG, "#onCreate url:" + url);
        loadUrl(START_URL);
    }

    /**
     * 下载模板
     * @param data json
     */
    public void downloadTemplate(String data,int id) throws JSONException{
        JSONObject json = new JSONObject(data);
        String name = json.getString(EXTRA_TEMPLATE_NAME);
        String url = json.getString(EXTRA_TEMPLATE_URL);

        Bundle args = new Bundle();
        args.putInt(EXTRA_TEMPLATE_ID, id);
        args.putString(EXTRA_TEMPLATE_NAME, name);
        args.putString(EXTRA_TEMPLATE_URL, url);
        startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
    }

    /**
     * 下载字体
     * @param template 模板路径
     */
    public void downloadFont(File template){
        List<String> fontList = parseFont(template);
        File fontDirectory = StoryManager.getStoryFontDirectory();
        for(File file : fontDirectory.listFiles()){
            if(!fontList.contains(file.getName())){
                Bundle args = new Bundle();
                args.putString(EXTRA_FONT_NAME, file.getName());
                startLoad(WHAT_DOWNLOAD_FONT, args);
            }
        }
    }

    private List<String> parseFont(File template){
        List<String> fontList = new ArrayList<String>();
        File file = new File(template, TEMPLATE_NAME);
        if (!file.exists()){
            return fontList;
        }
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(file));
            String header = reader.readLine();
            header = header.replace("<!--", "").trim();
            header = header.replace("-->", "").trim();
            String[] segments = header.split(":");
            if(segments.length == 2 && segments[0].equalsIgnoreCase("font")){
                String[] fonts = segments[1].split(",");
                for(String font : fonts){
                    fontList.add(font);
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
        return fontList;
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
                int id = args.getInt(EXTRA_TEMPLATE_ID, 0);
                final String name = args.getString(EXTRA_TEMPLATE_NAME);
                final String url = args.getString(EXTRA_TEMPLATE_URL);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryTemplateDirectory(), name));
                Downloader.download(Uri.parse(url),dest, new Downloader.DownloaderCallback(){
                    public void onDownloading(double progress){
                        loadUrl("javascript:onDownloading(" + progress + ")");
                    }
                    public void onCompleted(Uri downUri){
                        loadUrl("javascript:onCompleted('" + downUri.toString() + "')");
                        File template = getUnzipDirectory(name);
                        unzipTemplate(downUri, template);
                        downloadFont(template);
                    }

                    public void onError(Uri uri){
                        loadUrl("javascript:onError('"+uri.toString()+"!')");
                    }
                });
                break;
            }
            case WHAT_DOWNLOAD_FONT:{
                final String name = args.getString(EXTRA_FONT_NAME);
                Uri uri = WWWConfig.acquireUri(getString(R.string.uri_font_download));
                String url = String.format("%s?name=%s", uri.toString(), name);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryFontDirectory(), name));
                Downloader.download(Uri.parse(url),dest, new Downloader.DownloaderCallback(){
                    public void onDownloading(double progress){

                    }
                    public void onCompleted(Uri downUri){

                    }

                    public void onError(Uri uri){

                    }
                });
                break;
            }
        }
        return msg;
    }

    private File getUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if(0 < index){
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryTemplateDirectory(), templateDir);
    }

    private void unzipTemplate(Uri downUri, File template) {
        try {
            FileUtils.deleteDirectory(template);
            ZipUtils.unzip(downUri, template);
        }catch (IOException e){
            Log.e(TAG, "", e);
            loadUrl("javascript:onError('unzip error!')");
        }
    }

    @Override
    protected void onLoadCompleted(Message data) {
        if(isDestroyed() || null == data){
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
