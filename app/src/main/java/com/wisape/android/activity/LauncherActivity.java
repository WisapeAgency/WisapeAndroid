package com.wisape.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends AbsCompatActivity {

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
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
