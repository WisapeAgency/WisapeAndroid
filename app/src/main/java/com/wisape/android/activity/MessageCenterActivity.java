package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;

/**
 * 消息列表
 * Created by hm on 2015/8/15.
 */
public class MessageCenterActivity extends BaseActivity {

    public static void launch(Activity activity){
        Intent intent = new Intent(activity.getApplicationContext(),MessageCenterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
    }
}
