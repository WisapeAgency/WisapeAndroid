package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 修改邮箱
 * Created by huangmeng on 15/8/20.
 */
public class ChangeEmailDetailActivity extends BaseActivity{

    public static final int REQEUST_CODE_CHANGE_EMAIL_DETAIL = 0x05;
    public static final String EXTRA_EMAIL_ACCOUNT = "email_account";

    public static void launch(Activity activity,int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), ChangeEmailDetailActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @InjectView(R.id.add_emai_edit)
    SignUpEditText editEmail;

    @InjectView(R.id.password_reset_notice_text)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email_detail);

        ButterKnife.inject(this);

        editEmail.setText(UserLogic.instance().getUserInfoFromLocal().user_email);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    @OnClick(R.id.add_emai_btn)
    @SuppressWarnings("unused")
    public void onChangeEmailOnclicked(){
        String email = editEmail.getText().toString();
        if(verifyEMail(email)){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_EMAIL_ACCOUNT, email);
            setResult(RESULT_OK, intent);
            finish();
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

    @Override
    protected boolean onBackNavigation() {
        return super.onBackNavigation();
    }
}
