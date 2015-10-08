package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.DataSynchronizer;

/**
 * 启动界面
 * Created by LeiGuoting on 29/6/15.
 */
public class LauncherActivity extends BaseActivity {

    private static final String TAG = LauncherActivity.class.getSimpleName();

    private static final int LOADER_SIGN_UP = 1;
    private static final String ARG_USER_EMIAL = "user_email";
    private static final String ARG_USER_PWD = "user_pwd";
    public static final String SIGN_UP_WITH_EMAIL = "1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DataSynchronizer.getInstance().synchronous(WisapeApplication.getInstance().getApplicationContext());//
                }catch (Exception e){
                    Log.e(TAG,"同步数据失败:" + e.getMessage());
                }
            }
        }).start();



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(UserLogic.instance().getUserInfoFromLocal());
            }
        }, 2000);

    }

    private void startActivity(UserInfo userInfo) {
        if (null == userInfo) {
            SignUpActivity.launch(LauncherActivity.this);
        } else {
            //参数传递
            Bundle args = new Bundle();
            args.putString(ARG_USER_EMIAL, userInfo.user_email);
            args.putString(ARG_USER_PWD, userInfo.user_pwd);
            startLoad(LOADER_SIGN_UP, args);
        }
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        UserLogic logic = UserLogic.instance();
        switch (what) {
            case LOADER_SIGN_UP:
                String installId = wisapeApplication.getInstallId();
                msg = logic.signUp(SIGN_UP_WITH_EMAIL,
                        args.getString(ARG_USER_EMIAL), args.getString(ARG_USER_PWD), installId);
                break;
        }
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if (STATUS_SUCCESS == data.arg1) {
            MainActivity.launch(this);
        } else {
            showToast((String) data.obj);
            SignUpActivity.launch(LauncherActivity.this);
        }
    }
}