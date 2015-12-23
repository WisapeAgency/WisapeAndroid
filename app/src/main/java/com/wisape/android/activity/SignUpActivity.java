package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import org.w3c.dom.Text;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;

/**
 * 登录界面
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends BaseActivity implements SignUpEditText.OnActionListener,PlatformActionListener {

    private static final String EXTRA_LOG_OUT = "log_out";

    private static final String ARG_USER_EMIAL = "user_email";
    private static final String ARG_USER_PWD = "user_pwd";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_USER_ICON = "user_icon";

    private static final int LOADER_SIGN_UP = 1;
    private static final int LOADER_SIGN_UP_WITH_FACEBOOK = 2;
    private static final int LOADER_SIGN_UP_WITH_TWITTER = 3;
    private static final int LOADER_SIGN_UP_WITH_GOOGLE_PLUS = 4;

    public static final String SIGN_UP_WITH_EMAIL = "1";


    @InjectView(R.id.sign_up_email)
    protected SignUpEditText emailEdit;
    @InjectView(R.id.sign_up_password)
    protected SignUpEditText passwordEdit;
    @InjectView(R.id.sign_up_forget_password)
    protected TextView forgetPassword;
    @InjectView(R.id.btn_goto_regist)
    protected TextView textGotoRegister;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), SignUpActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void launch(Context context,String message) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_LOG_OUT, message);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.inject(this);
        emailEdit.setOnActionListener(this);
        passwordEdit.setOnActionListener(this);
        SpannableString string = new SpannableString(forgetPassword.getText());
        string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
        forgetPassword.setText(string);

        SpannableStringBuilder style=new SpannableStringBuilder("Don't have an Account? Sign Up");
        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.app_sixth_transparent_50p)), 0, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textGotoRegister.setText(style);
    }


    @OnClick(R.id.sign_up_btn)
    @SuppressWarnings("unused")
    protected void doSignUp() {
        String email = emailEdit.getText().toLowerCase();
        String password = passwordEdit.getText();
        if (verifyEMail(email) && verifyPassword(password)) {
            email = email.trim();
            password = password.trim();

            //参数传递
            Bundle args = new Bundle();
            args.putString(ARG_USER_EMIAL, email);
            args.putString(ARG_USER_PWD, password);
            startLoadWithProgress(LOADER_SIGN_UP, args);
        }
    }


    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        UserLogic logic = UserLogic.instance();
        switch (what) {
            case LOADER_SIGN_UP:
                msg = logic.signUp(SIGN_UP_WITH_EMAIL,
                        args.getString(ARG_USER_EMIAL), args.getString(ARG_USER_PWD), "123");
                break;
            default:
                break;
        }
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if (STATUS_SUCCESS == data.arg1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        DataSynchronizer.getInstance().synchronous(WisapeApplication.getInstance().getApplicationContext());//
                    }catch (Exception e){
                        LogUtil.e("数据同步失败:",e);
                    }
                }
            }).start();
            MainActivity.launch(this);
        } else {
            if(data.obj instanceof  com.alibaba.fastjson.JSONException){
                com.alibaba.fastjson.JSONException exception =(com.alibaba.fastjson.JSONException) data.obj;
                LogUtil.e("登录数据转换:",exception);
                showToast(exception.getMessage());
            }else{
                showToast((String)data.obj);
            }
        }
    }


    private boolean verifyEMail(String email) {
        if (null == email) {
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        email = email.trim();
        if (0 == email.length()) {
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        if (!Utils.isEmail(email)) {
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }

    private boolean verifyPassword(String password) {
        if (null == password) {
            passwordEdit.setError(getString(R.string.sign_up_password_can_not_null));
            return false;
        }

        password = password.trim();
        if (0 == password.length()) {
            passwordEdit.setError(getString(R.string.sign_up_password_can_not_null));
            return false;
        }
        return true;
    }


    @Override
    public void onActionClicked(View view) {
        switch (view.getId()) {
            default:
                return;
            case R.id.sign_up_email:
                emailEdit.setText("");
                break;
            case R.id.sign_up_password:
                if (passwordEdit.isPasswordText()) {
                    passwordEdit.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else if (passwordEdit.isVisiblePasswordInputType()) {
                    passwordEdit.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
        }
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {

        String plateName = platform.getName();

        Bundle args = new Bundle();
        args.putString(ARG_USER_EMIAL,(String)hashMap.get("email"));
        args.putString(ARG_USER_NAME,(String)hashMap.get("name"));
        args.putString(ARG_USER_ICON,(String)hashMap.get("icon"));
        if("twitter".equals(plateName)){
            startLoad(LOADER_SIGN_UP_WITH_TWITTER,args);
        }
        if("facebook".equals(plateName)){
            startLoad(LOADER_SIGN_UP_WITH_FACEBOOK,args);
        }
        if("google".equals(plateName)){
            startLoad(LOADER_SIGN_UP_WITH_GOOGLE_PLUS,args);
        }

    }

    @Override
    public void onCancel(Platform platform, int i) {
        showToast(platform.getName() + "授权登录取消");
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        showToast(platform.getName() + "授权登录失败!");
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.sign_up_forget_password)
    @SuppressWarnings("unused")
    protected void doForgetPassword() {
        PasswordResetActivity.launch(this);
    }

    @OnClick(R.id.btn_goto_regist)
    protected void gotoRegister(){
        RegisterActivity.luanch(this);
    }
}
