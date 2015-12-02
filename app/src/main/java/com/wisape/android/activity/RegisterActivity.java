package com.wisape.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.wisape.android.R;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 用户注册
 * Created by lenovo on 2015/11/30.
 */
public class RegisterActivity extends BaseActivity {

    private static final int LOADER_REGISTER = 1;

    @InjectView(R.id.regist_email)
    protected SignUpEditText textEmail;
    @InjectView(R.id.regist_pwd)
    protected SignUpEditText textPwd;
    @InjectView(R.id.regist_user_name)
    protected SignUpEditText textRepeatPwd;

    public static void luanch(BaseActivity activity) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.sign_up_btn)
    @SuppressWarnings("unused")
    protected void onCreatAccountClick() {
        String email = textEmail.getText();
        if (verifyEMail(email)) {
            if(verifyPwd()){
                startLoadWithProgress(LOADER_REGISTER, null);
            }
        }
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        return UserLogic.instance().register(textEmail.getText(), textPwd.getText(),textRepeatPwd.getText(), "");
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if (STATUS_SUCCESS == data.arg1) {
           MainActivity.launch(this);
        }else{
            showToast((String)data.obj);
        }
    }

    private boolean verifyEMail(String email) {
        if (null == email) {
            textEmail.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        email = email.trim();
        if (0 == email.length()) {
            textEmail.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        if (!Utils.isEmail(email)) {
            textEmail.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }

    private boolean verifyPwd() {
        String pwd = textPwd.getText();
        if (Utils.isEmpty(pwd)) {
            textPwd.setError("password not be null");
            return false;
        }

        String pwdRe = textRepeatPwd.getText();
        if (Utils.isEmpty(pwdRe)) {
            textRepeatPwd.setError("userName not be null");
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    protected boolean onBackNavigation() {
        SignUpActivity.launch(this);
        return true;
    }

    @Override
    public void onBackPressed() {
        onBackNavigation();
    }
}
