package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.wisape.android.common.UserManager;
import com.wisape.android.model.UserInfo;

/**
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends BaseActivity {
    private static final int LOADER_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startLoad(LOADER_SIGN_IN, null);
            }
        }, 1000);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = UserManager.instance().signIn(getApplicationContext());
        msg.arg1 = STATUS_SUCCESS;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        UserInfo user = (UserInfo)data.obj;
        if(null == user){
            SignUpActivity.launch(this, -1);
        }else{
            MainActivity.launch(this, user, -1);
        }
    }
}
