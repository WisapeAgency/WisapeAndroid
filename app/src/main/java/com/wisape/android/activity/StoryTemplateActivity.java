package com.wisape.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.wisape.android.util.EnvironmentUtils;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity {
    private static final String START_URL = "file:///android_asset/www/views/editor_index.html";

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

    public void invodeJavascriptTest(){
        appView.loadUrl("javascript:test2()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
