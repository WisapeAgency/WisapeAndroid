package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.wisape.android.R;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;

/**
 * 启动界面
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends BaseActivity {

    private static final int LOADER_SIGN_IN = 1;
    private static final long LAUNCH_TIME_MILLS = 1000;

    private long startTimeInMills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        startLoad(LOADER_SIGN_IN,null);
        startTimeInMills = SystemClock.uptimeMillis();
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        UserInfo userInfo = UserLogic.instance().loaderUserFromLocal();
        Message message = Message.obtain();
        message.what = what;
        message.obj = userInfo;
        return message;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        UserInfo userInfo = (UserInfo)data.obj;
        redirect(userInfo);
    }

    private void redirect(final UserInfo user) {
        long costMills = SystemClock.uptimeMillis() - startTimeInMills;
        long diffMills = LAUNCH_TIME_MILLS - costMills;
        if (0 < diffMills) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(user);
                }
            }, diffMills);
        } else {
            startActivity(user);
        }
    }

    private void startActivity(UserInfo userInfo) {
        if (null == userInfo) {
            SignUpActivity.launch(LauncherActivity.this);
            finish();
        } else {
            wisapeApplication.setUserInfo(userInfo);
            MainActivity.launch(LauncherActivity.this);
            finish();
        }
    }
}