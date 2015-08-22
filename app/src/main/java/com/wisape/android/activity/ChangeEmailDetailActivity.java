package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.Message.Message;
import com.wisape.android.Message.UserProfileMessage;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.http.DefaultHttpRequestListener;
import com.wisape.android.http.HttpRequest;
import com.wisape.android.http.HttpRequestListener;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 修改邮箱
 * Created by huangmeng on 15/8/20.
 */
public class ChangeEmailDetailActivity extends BaseActivity{

    private static final String USER_EMAIL = "email";

    public static void launch(Activity activity){
        activity.startActivity(new Intent(activity.getApplicationContext(), ChangeEmailDetailActivity.class));
    }

    @InjectView(R.id.add_emai_edit)
    SignUpEditText editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email_detail);

        ButterKnife.inject(this);
        editEmail.setText(wisapeApplication.getUserInfo().user_email);
    }

    @OnClick(R.id.add_emai_btn)
    public void onChangeEmailOnclicked(){
        String email = editEmail.getText().toString();
        if(verifyEMail(email)){
            UserProfileMessage userProfileMessage = new UserProfileMessage();
            userProfileMessage.setUserEmail(email);
            EventBus.getDefault().post(userProfileMessage);
            ChangeEmailDetailActivity.this.finish();
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
            editEmail.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }
}
