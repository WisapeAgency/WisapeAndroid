package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Handler;

/**
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                redirect();
            }
        }, 1000);
    }

    private void redirect(){
        SignUpActivity.launch(this, -1);
    }
}
