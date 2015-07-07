package com.wisape.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity {
    private static final String START_URL = "file:///android_asset/www/index.html";

    public static void launch(Fragment fragment, int requestCode){
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), StoryTemplateActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadUrl(START_URL);
    }
}
