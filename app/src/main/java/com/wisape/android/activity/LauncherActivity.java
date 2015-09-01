package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.common.UserManager;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.DataSynchronizer;

/**
 * 启动界面
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends BaseActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    private static final int LOADER_SIGN_IN = 1;
    private static final long LAUNCH_TIME_MILLS = 1000;

    private long startTimeInMills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        startLoad(LOADER_SIGN_IN, null);
        startTimeInMills = SystemClock.uptimeMillis();
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        DataSynchronizer.getInstance().synchronous(getApplicationContext());//
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = UserManager.instance().signIn(getApplicationContext());
        msg.arg1 = STATUS_SUCCESS;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        long costMills = SystemClock.uptimeMillis() - startTimeInMills;
        long diffMills = LAUNCH_TIME_MILLS - costMills;
        if(!(data.obj instanceof UserInfo)){
            redirect(null);
            return;
        }
        final UserInfo user = (UserInfo)data.obj;
        Log.e(TAG, "#onLoadCompleted diffMills:" + diffMills);
        if(0 < diffMills){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    redirect(user);
                }
            }, diffMills);
        }else{
            redirect(user);
        }
    }

    private void redirect(UserInfo user){
        if(null == user){
            SignUpActivity.launch(this, -1);
        }else{
            MainActivity.launch(this, user, -1);
        }
    }
}
