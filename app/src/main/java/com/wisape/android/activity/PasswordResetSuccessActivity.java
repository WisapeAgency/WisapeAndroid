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
        String email = getIntent().getExtras().getString(USER_EMAIL);
        mPasswordResetSuccessText.append(Html.fromHtml("<html><body>&nbsp;<font color=\"#FF8800\">"+ email + "</body></html>"));
        mPasswordResetSuccessText.append(getResources().getString(R.string.password_reset_success_text_end));

        mPasswordResetSuccessText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] email = {"3802**92@qq.com"}; // 需要注意，email必须以数组形式传入
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, email); // 接收人
                intent.putExtra(Intent.EXTRA_CC, email); // 抄送人
                startActivity(Intent.createChooser(intent, "请选择邮件打开方式"));
            }
        });
    }

    public static void launch(Activity activity,String email){
        Intent intent = new Intent(activity.getApplicationContext(),PasswordResetSuccessActivity.class);
        intent.putExtra(USER_EMAIL,email);
        activity.startActivity(intent);
    }
}
