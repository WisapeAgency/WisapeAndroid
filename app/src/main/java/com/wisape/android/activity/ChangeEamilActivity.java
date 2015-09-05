package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;
import com.wisape.android.widget.ComfirmDialog;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 修改邮箱
 * Created by huangmeng on 15/8/20.
 */
public class ChangeEamilActivity extends BaseActivity{

    public static final int REQUEST_CODE_CHANGE_EMAIL = 0x03;

    public static final String EXTRA_EMAIL_ACCOUNT = "email_account";

    public static void Launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(),ChangeEamilActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_change_email_address)
    @SuppressWarnings("unused")
    public void onChangeEmailAddressOnClicked(){
        ChangeEmailDetailActivity.launch(this, ChangeEmailDetailActivity.REQEUST_CODE_CHANGE_EMAIL_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(RESULT_OK == resultCode){
            Bundle extras = data.getExtras();
            if(ChangeEmailDetailActivity.REQEUST_CODE_CHANGE_EMAIL_DETAIL == requestCode){
                String email = extras.getString(ChangeEmailDetailActivity.EXTRA_EMAIL_ACCOUNT);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_EMAIL_ACCOUNT,email);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @OnClick(R.id.btn_logout_email_account)
    @SuppressWarnings("unused")
    public void onLogoutEmaiAccountOnClicked(){
        final ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.logout_email_account),
                getString(R.string.logout_emial_account_content));
        comfirmDialog.show(getSupportFragmentManager(),"logoutEmail");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                comfirmDialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_EMAIL_ACCOUNT,"");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
