package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 重置信息已经发送到邮箱界面
 * Created by hm on 2015/8/11.
 */
public class PasswordResetSuccessActivity extends BaseActivity{

    private static final String USER_EMAIL = "user_mail";

    @InjectView(R.id.password_reset_success_text)
    @SuppressWarnings("unused")
    protected TextView mPasswordResetSuccessText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset_success);
        ButterKnife.inject(this);
        final String email = getIntent().getExtras().getString(USER_EMAIL);
        mPasswordResetSuccessText.append(Html.fromHtml("<html><body>&nbsp;<font color=\"#FF8800\">"+ email + "</body></html>"));
        mPasswordResetSuccessText.append(getResources().getString(R.string.password_reset_success_text_end));
    }

    public static void launch(Activity activity,String email){
        Intent intent = new Intent(activity.getApplicationContext(),PasswordResetSuccessActivity.class);
        intent.putExtra(USER_EMAIL,email);
        activity.startActivity(intent);
    }
}
