package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 增加邮箱
 * Created by huangmeng on 15/8/19.
 */
public class AddEmailAccoutActivity extends BaseActivity{

    public static final String EMAIL_ACCOUNT = "email_account";
    public static final int REQEUST_CODE = 1;

    @InjectView(R.id.add_emai_edit)
    protected SignUpEditText editEamil;


    public static void launch(Activity activity,int reqesutCode){
        Intent intent = new Intent(activity.getApplicationContext(),AddEmailAccoutActivity.class);
        activity.startActivityForResult(intent, reqesutCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_email);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.add_emai_btn)
    @SuppressWarnings("unused")
    public void onConfirmClicked(){
        String email = editEamil.getText().toString();
        if(verifyEMail(email)){
            Intent intent = new Intent();
            intent.putExtra(EMAIL_ACCOUNT,email);
            setResult(RESULT_OK,intent);
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
            editEamil.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }
}
