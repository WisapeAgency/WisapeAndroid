package com.wisape.android.activity;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Duke
 */
public class BaseCompatActivity extends AppCompatActivity {
    private boolean destroyed;

    @Override
    protected void onDestroy() {
        if(17 > Build.VERSION.SDK_INT){
            destroyed = true;
        }
        super.onDestroy();
    }

    public boolean isDestroyed() {
        if(17 > Build.VERSION.SDK_INT){
            return destroyed;
        }else{
            return super.isDestroyed();
        }
    }
}
