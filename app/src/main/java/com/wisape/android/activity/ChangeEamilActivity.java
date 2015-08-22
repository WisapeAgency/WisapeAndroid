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

    public static void Launche(Activity activity){
        activity.startActivity(new Intent(activity.getApplicationContext(),ChangeEamilActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.btn_change_email_address)
    public void onChangeEmailAddressOnClicked(){
        ChangeEmailDetailActivity.launch(this);
        finish();
    }

    @OnClick(R.id.btn_logout_email_account)
    public void onLogoutEmaiAccountOnClicked(){
        final ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.logout_email_account),
                getString(R.string.logout_emial_account_content));
        comfirmDialog.show(getSupportFragmentManager(),"logoutEmail");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                comfirmDialog.dismiss();
                ChangeEamilActivity.this.finish();
            }
        });
    }
}
