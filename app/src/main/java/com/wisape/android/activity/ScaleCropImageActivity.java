package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;

/**
 * Created by LeiGuoting on 11/7/15.
 */
public class ScaleCropImageActivity extends BaseActivity{
    public static final int REQUEST_CODE_CROP = 0x103;

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), ScaleCropImageActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, ScaleCropImageActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_crop_image);

    }
}
