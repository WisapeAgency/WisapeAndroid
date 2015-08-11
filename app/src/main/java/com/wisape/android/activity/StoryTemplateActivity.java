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
     * JS���ýӿڣ����ڱ���Story
     * @param storyId ����id
     * @param html story��html����
     * @param paths �ļ���ʵ·�������·��(����)
     */
    @JavascriptInterface
    public boolean save(int storyId, String html, String[] paths){
        return false;
    }

    /**
     * ��ȡ��ӦID�Ĺ���Ŀ¼·��
     * @param storyId Storyģ��id
     * @return ����Ŀ¼·��
     */
    public String getStoryPath(int storyId){
        return null;
    }

    /**
     * ��ȡ��ӦID��ģ��Ŀ¼·��
     * @param stageId ģ��id
     * @return ģ��Ŀ¼·��
     */
    public String getStagePath(int stageId){
        return null;
    }

    /**
     * ��ȡHTML�ļ�
     * @param path �ļ�·��
     * @return �ļ�����
     */
    public String read(String path){
        return null;
    }

    /**
     *�û�������Դ�ļ���Ӳ��·�������滻���ļ�·��
     * @param sourcePath Դ·��
     * @param destPath ���滻·��
     */
    public boolean replaceFile(String sourcePath,String destPath){
        return false;
    }

    /**
     * JS���ýӿڣ���������
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
