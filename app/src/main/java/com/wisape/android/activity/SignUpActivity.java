package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import org.json.JSONException;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.google.GooglePlus;
import cn.sharesdk.twitter.Twitter;

/**
 * 登录界面
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends BaseActivity implements SignUpEditText.OnActionListener,PlatformActionListener {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private static final String EXTRA_PROFILE_PARAM = "profile_param";

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
    public static final String SIGN_UP_WITH_FACE_BOOK = "2";
    public static final String SIGN_UP_WITH_TWITTER = "3";
    public static final String SIGN_UP_WITH_GOOGLE_PLUS = "4";

    public static final int REQUEST_CODE_FACEBOOK_LOGIN = 1;
    public static final int REQUEST_CODE_TWITTER_LOGIN = 2;
    public static final int REQUEST_CODE_GOOGLE_PLUS_LOGIN = 3;

    @InjectView(R.id.sign_up_email)
    protected SignUpEditText emailEdit;
    @InjectView(R.id.sign_up_password)
    protected SignUpEditText passwordEdit;
    @InjectView(R.id.sign_up_forget_password)
    protected TextView forgetPassword;

    private String message;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), SignUpActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void launch(Context context,String message) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_LOG_OUT,message);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
//        ShareSDK.initSDK(this);
        ButterKnife.inject(this);
        emailEdit.setOnActionListener(this);
        passwordEdit.setOnActionListener(this);
        SpannableString string = new SpannableString(forgetPassword.getText());
        string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
        forgetPassword.setText(string);
        Bundle bundle = getIntent().getExtras();
        if(null != bundle){
            message = getIntent().getExtras().getString(EXTRA_LOG_OUT);
            if(!Utils.isEmpty(message)){
                showToast(message);
            }
        }
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

    @OnClick(R.id.sign_up_with_twitter)
    @SuppressWarnings("unused")
    protected void doSignUPWithTwitter() {
        authorize(new Twitter(this));
//        Resources resources = getResources();
//        OAuthParams params = new OAuthParams(
//                OAuthParams.VERSION_OAUTH_2,
//                OAuthParams.OAUTH_TWITTER,
//                "Twitter",
//                "",
//                resources.getString(R.string.twitter_api_key),
//                resources.getString(R.string.twitter_api_secret_key),
//                resources.getString(R.string.twitter_api_callback_uri));
//        OAuthActivity.start(this, params, REQUEST_CODE_TWITTER_LOGIN);
    }

    @OnClick(R.id.sign_up_with_facebook)
    @SuppressWarnings("unused")
    protected void doSignUPWithFacebook() {
        authorize(new Facebook(this));
//        Resources resources = getResources();
//        OAuthParams params = new OAuthParams(
//                OAuthParams.VERSION_OAUTH_2,
//                OAuthParams.OAUTH_FACEBOOK,
//                "Facebook",
//                "public_profile,email",
//                resources.getString(R.string.facebook_api_key),
//                resources.getString(R.string.facebook_api_secret_key),
//                resources.getString(R.string.facebook_api_callback_uri));
//        OAuthActivity.start(this, params, REQUEST_CODE_FACEBOOK_LOGIN);
    }

    @OnClick(R.id.sign_up_with_google_plus)
    @SuppressWarnings("unused")
    protected void doSignUPWithGooglePlus() {
        authorize(new GooglePlus(this));
//        Resources resources = getResources();
//        OAuthParams params = new OAuthParams(
//                OAuthParams.VERSION_OAUTH_2,
//                OAuthParams.OAUTH_GOOGLEPLUS,
//                "Google+",
//                "openid,email,profile",
//                resources.getString(R.string.googleplus_api_key),
//                resources.getString(R.string.googleplus_api_secret_key),
//                resources.getString(R.string.googleplus_api_callback_uri));
//        OAuthActivity.start(this, params, REQUEST_CODE_GOOGLE_PLUS_LOGIN);
    }

    /**
     * 用于授权
     *
     * @param plat
     */
    private void authorize(Platform plat) {
        /**** 下面2个步骤是用于授权时可以切换用户 ******/
        // tip1:清除缓存-这样可以切换账户，即每次都可以进入授权登录界面
        CookieSyncManager.createInstance(plat.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager.getInstance().sync();
        // tip2:判断本地是否有平台授权信息文件,如果有就删除账户信息，否则无法进入授权相关界面，而是直接引用本地授权信息文件
        if (plat.isValid()) {
            plat.removeAccount();
        }
        // 授权监听
        plat.setPlatformActionListener(this);
        plat.SSOSetting(true);
        plat.showUser(null);
    }


    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        UserLogic logic = UserLogic.instance();
        switch (what) {
            case LOADER_SIGN_UP:
//                String installId = wisapeApplication.getInstallId();
                msg = logic.signUp(SIGN_UP_WITH_EMAIL,
                        args.getString(ARG_USER_EMIAL), args.getString(ARG_USER_PWD), "123");
                break;

            case LOADER_SIGN_UP_WITH_FACEBOOK:
//                ProfileRequester.Param param = args.getParcelable(EXTRA_PROFILE_PARAM);
//                ProfileRequester profileRequester = new ProfileForFacebookRequester();
//                ProfileRequester.ProfileInfo profile = profileRequester.request(param);
                msg = logic.signUpWith(SIGN_UP_WITH_FACE_BOOK, args.getString(ARG_USER_EMIAL),
                        args.getString(ARG_USER_ICON), args.getString(ARG_USER_NAME),
                        "", "123");

                break;

            case LOADER_SIGN_UP_WITH_GOOGLE_PLUS:
//                param = args.getParcelable(EXTRA_PROFILE_PARAM);
//                profileRequester = new ProfileForGooglePlusRequester();
//                profile = profileRequester.request(param);
                msg = logic.signUpWith(SIGN_UP_WITH_GOOGLE_PLUS, args.getString(ARG_USER_EMIAL),
                        args.getString(ARG_USER_ICON), args.getString(ARG_USER_NAME),
                        "", "123");
                break;

            case LOADER_SIGN_UP_WITH_TWITTER:
//                ProfileForTwitterRequester.TwitterParams twParams = args.getParcelable(EXTRA_PROFILE_PARAM);
//                profileRequester = new ProfileForTwitterRequester();
//                profile = profileRequester.request(twParams);
                msg = logic.signUpWith(SIGN_UP_WITH_TWITTER, args.getString(ARG_USER_EMIAL),
                        args.getString(ARG_USER_ICON), args.getString(ARG_USER_NAME),
                        "", "123");
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
            UserInfo user = (UserInfo) data.obj;
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
//        ShareSDK.stopSDK();
    }

    @OnClick(R.id.sign_up_forget_password)
    @SuppressWarnings("unused")
    protected void doForgetPassword() {
        PasswordResetActivity.launch(this);
    }
}
