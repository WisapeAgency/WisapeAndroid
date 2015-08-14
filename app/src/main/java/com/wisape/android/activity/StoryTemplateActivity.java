package com.wisape.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.common.file.FileUtils;
import com.google.gson.Gson;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.DynamicBroadcastReceiver;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.Requester;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity{
    private static final String START_URL = "file:///android_asset/www/views/editor_index.html";
    private static final String EXTRA_TEMPLATE_ID = "temp_id";
    private static final String EXTRA_TEMPLATE_NAME = "temp_name";
    private static final String EXTRA_TEMPLATE_URL = "temp_url";
    private static final int WHAT_DOWNLOAD_TEMPLATE = 0x01;
    private static final String EXTRA_ACTION_DOWNLOAD_TEMPLATE = "_action_download_template";
    public static final String ACTION_DOWNLOAD_TEMPLATE = "com.wisape.android.action.DOWNLOAD_TEMPLATE";

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
        args.putString(EXTRA_ACTION_DOWNLOAD_TEMPLATE, ACTION_DOWNLOAD_TEMPLATE);
        startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
    }

    /**
     * 下载字体
     * @param fontName 字体名称
     */
    public void downloadFont(String fontName){

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
                        int index = name.lastIndexOf('.');
                        String templateDir = name;
                        if(0 < index){
                            templateDir = name.substring(0, index);
                        }
                        File template = new File(StoryManager.getStoryTemplateDirectory(), templateDir);
                        try {
                            org.apache.commons.io.FileUtils.deleteDirectory(template);
                            ZipUtils.unzip(downUri, template);
                        }catch (IOException e){
                            Log.e(TAG, "", e);
                            loadUrl("javascript:onError('unzip error!')");
                        }
                    }
                    public void onError(Uri uri){
                        loadUrl("javascript:onError('"+uri.toString()+"!')");
                    }
                });
                break;
            }
        }
        return msg;
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
