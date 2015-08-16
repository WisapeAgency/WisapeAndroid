package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;

/**
 * 单条消息详细信息
 * Created by hm on 2015/8/15.
 */
public class MessageCenterDetailActivity extends BaseActivity {

    public static void launch(Activity activity){
        Intent intent = new Intent(activity.getApplicationContext(),MessageCenterDetailActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center_detail);
    }
}
