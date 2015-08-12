package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wisape.android.R;
import com.wisape.android.api.ApiUser;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.network.Requester;
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

    private static final String TAG = PasswordResetActivity.class.getSimpleName();

    /**已经向邮箱发送验证信息*/
    private static final int STATUS_OK = 1;
    /**服务器端发生错误*/
    private static final int STATUS_SERVER_ERROR=500;
    /**需要验证的邮箱不存在*/
    private static final int STATUS_EMAIL_NOT_HAVE=401;

    private static final String USER_EMAIL = "user_email";
    private static final int RESET_USER_EMAIL = 1;

    @InjectView(R.id.password_reset_email_edit)
    protected SignUpEditText mPasswordRestEmailEdit;
    @InjectView(R.id.password_reset_btn)
    @SuppressWarnings("unused")
    protected Button mPasswordRestSubmitBtn;

    public static void launch(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), PasswordResetActivity.class);
        activity.startActivityForResult(intent, requestCode);
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
        String email = mPasswordRestEmailEdit.getText().toString();
        if (verifyEMail(email)) {
            showProgressDialog(R.string.progress_dialog_reset_password);
            Bundle args = new Bundle();
            args.putString(USER_EMAIL, email.trim());
            startLoad(RESET_USER_EMAIL, args);
        }
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message message = Message.obtain();
        message.what = what;
        if (RESET_USER_EMAIL == what) {
            UserLogic userLogic = UserLogic.instance();
            ApiUser.AttrResetPasswordInfo attr = new ApiUser.AttrResetPasswordInfo();
            attr.email = args.getString(USER_EMAIL, "");
            args.clear();

            Requester.ServerMessage serverMessage = userLogic.resetPassword(getApplicationContext(), attr, getCancelableTag());
            message.obj = serverMessage;
            message.arg1 = serverMessage.status;
        }

       return message;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        if(RESET_USER_EMAIL == data.what){
            if(STATUS_OK == data.arg1){
                PasswordResetSuccessActivity.launch(this,mPasswordRestEmailEdit.getText().toString());
            }else{
               showToast("请求错误");
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
