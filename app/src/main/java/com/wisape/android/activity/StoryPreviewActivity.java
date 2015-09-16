package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by William on 9/15/15.
 */
public class StoryPreviewActivity extends AbsCordovaActivity{

    public static void launch(Activity activity,String url){
        Intent intent = new Intent(activity, StoryPreviewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
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
