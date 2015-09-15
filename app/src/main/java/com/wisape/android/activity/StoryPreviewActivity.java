package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by William on 9/15/15.
 */
public class StoryPreviewActivity extends AbsCordovaActivity{

    public static void launch(Activity activity,String url, int requestCode){
        Intent intent = new Intent(activity, StoryPreviewActivity.class);
        intent.putExtra("url",url);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String url = getIntent().getStringExtra("url");
        loadUrl(url);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
