package com.wisape.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wisape.android.R;

/**
 * Created by LeiGuoting on 10/7/15.
 */
public class AboutActivity extends BaseActivity{

    public static void launch(Fragment fragment){
        Intent intent = new Intent(fragment.getActivity(), AboutActivity.class);
        fragment.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
