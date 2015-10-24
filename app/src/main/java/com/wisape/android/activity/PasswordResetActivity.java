package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.wisape.android.R;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 密码重置界面
 * Created by LeiGuoting on 6/7/15.
 * Update by hm on 10/8/15
 */
public class PasswordResetActivity extends BaseActivity implements SignUpEditText.OnActionListener {

    private static final int LOADER_PASSWORD_RESET = 1;

    private static final String EXTARS_EMAIL_ACCOUNT = "email_account";

    @InjectView(R.id.password_reset_email_edit)
    protected SignUpEditText mPasswordRestEmailEdit;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), PasswordResetActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        ButterKnife.inject(this);
        mPasswordRestEmailEdit.setOnActionListener(this);
    }

    @OnClick(R.id.password_reset_btn)
    @SuppressWarnings("unused")
    protected void doResetPassword() {
        String email = mPasswordRestEmailEdit.getText().toString().toLowerCase();
        if (verifyEMail(email)) {
            Bundle args = new Bundle();
            args.putString(EXTARS_EMAIL_ACCOUNT, email.trim());
            startLoadWithProgress(LOADER_PASSWORD_RESET, args);
        }
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        return UserLogic.instance().passwordRest(args.getString(EXTARS_EMAIL_ACCOUNT));
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if(STATUS_SUCCESS == data.arg1){
            PasswordResetSuccessActivity.launch(this,mPasswordRestEmailEdit.getText().toString());
            finish();
        }else{
            if(data.obj instanceof String){
                showToast((String)data.obj);
            }else{
                showToast("Network Error");
            }
        }
    }

    /***
     * 校验邮箱格式
     *
     * @param email 传入的邮箱
     * @return 正确 true
     * 错误 false
     */
    private boolean verifyEMail(String email) {
        if (null == email || 0 == email.trim().length() || !Utils.isEmail(email)) {
            mPasswordRestEmailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }

    @Override
    public void onActionClicked(View view) {
        if (R.id.password_reset_email_edit == view.getId()) {
            mPasswordRestEmailEdit.setText("");
        }
    }
}
