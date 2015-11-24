package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by William on 9/15/15.
 */
public class StoryPreviewActivity extends AbsCordovaActivity {

    private String url;

    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, StoryPreviewActivity.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (savedInstanceState != null) {
//            url = savedInstanceState.getString("url");
//        } else {
//            url = getIntent().getStringExtra("url");
//        }
        String url = getIntent().getStringExtra("url");
        loadUrl("file://" + url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appView = null;
    }
}
