package com.wisape.android.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.wisape.android.util.EnvironmentUtils;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity {
    private static final String START_URL = "file:///android_asset/www/views/editor_index.html";

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

    public void invodeJavascriptTest(){
        appView.loadUrl("javascript:test2()");
    }

    /**
     * JS调用接口，用于保存Story
     * @param storyId 故事id
     * @param html story的html代码
     * @param paths 文件真实路径和相对路径(数组)
     */
    @JavascriptInterface
    public boolean save(int storyId, String html, String[] paths){
        return false;
    }

    /**
     * 获取对应ID的故事目录路径
     * @param storyId Story模板id
     * @return 故事目录路径
     */
    public String getStoryPath(int storyId){
        return null;
    }

    /**
     * 获取对应ID的模版目录路径
     * @param stageId 模板id
     * @return 模板目录路径
     */
    public String getStagePath(int stageId){
        return null;
    }

    /**
     * 读取HTML文件
     * @param path 文件路径
     * @return 文件内容
     */
    public String read(String path){
        return null;
    }

    /**
     *用户新增资源文件的硬盘路径，被替换的文件路径
     * @param sourcePath 源路径
     * @param destPath 被替换路径
     */
    public boolean replaceFile(String sourcePath,String destPath){
        return false;
    }

    /**
     * JS调用接口，发起下载
     * @param type
     * @param id
     * @return
     */
    public Object start(int type, int id){
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
