package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.wisape.android.R;
import com.wisape.android.widget.SignUpEditText;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends VolleyActivity implements SignUpEditText.OnActionListener, View.OnClickListener{
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private SignUpEditText emailEdit;
    private SignUpEditText passwordEdit;

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), SignUpActivity.class);
        if(-1 == requestCode){
            activity.startActivity(intent);
            activity.finish();
        }else{
            activity.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SignUpEditText emailEdit = (SignUpEditText) findViewById(R.id.sign_up_email);
        emailEdit.setOnActionListener(this);
        this.emailEdit = emailEdit;

        SignUpEditText passwordEdit = (SignUpEditText) findViewById(R.id.sign_up_password);
        passwordEdit.setOnActionListener(this);
        this.passwordEdit = passwordEdit;

        findViewById(R.id.sign_up_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        passwordEdit.setError("密码错误");
    }

    @Override
    public void onActionClicked(View view) {
        switch (view.getId()){
            default :
                return;

            case R.id.sign_up_email :
                emailEdit.setText("");
                break;

            case R.id.sign_up_password :
                if(passwordEdit.isPasswordText()){
                    passwordEdit.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else if(passwordEdit.isVisiblePasswordInputType()){
                    passwordEdit.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
        }
    }
}
