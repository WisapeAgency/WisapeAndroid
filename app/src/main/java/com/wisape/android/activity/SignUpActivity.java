package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.oauth.android.OAuthActivity;
import com.oauth.android.OAuthParams;
import com.wisape.android.BuildConfig;
import com.wisape.android.R;
import com.wisape.android.common.UserManager;
import com.wisape.android.logic.UserAuthorityLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUserAuthority;
import com.wisape.android.network.Requester;
import com.wisape.android.network.ServerAPI;
import com.wisape.android.util.SecurityUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends BaseActivity implements SignUpEditText.OnActionListener{
    private static final String TAG = SignUpActivity.class.getSimpleName();
    private static final String EXTRA_EMAIL = "extra_email";
    private static final String EXTRA_PASSWORD = "extra_password";
    private static final int LOADER_SIGN_UP = 1;

    public static final int REQUEST_CODE_FACEBOOK_LOGIN = 1;
    public static final int REQUEST_CODE_TWITTER_LOGIN = 2;
    public static final int REQUEST_CODE_GOOGLE_PLUS_LOGIN = 3;

    @InjectView(R.id.sign_up_email)
    protected SignUpEditText emailEdit;
    @InjectView(R.id.sign_up_password)
    protected SignUpEditText passwordEdit;

    @InjectView(R.id.sign_up_forget_password)
    protected TextView forgetPassword;

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
        ButterKnife.inject(this);
        emailEdit.setOnActionListener(this);
        passwordEdit.setOnActionListener(this);

        SpannableString string = new SpannableString(forgetPassword.getText());
        string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
        forgetPassword.setText(string);
    }

    @OnClick(R.id.sign_up_with_twitter)
    @SuppressWarnings("unused")
    protected void doSignUPWithTwitter(){
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_1,
                OAuthParams.OAUTH_TWITTER,
                "Twitter",
                "",
                resources.getString(R.string.twitter_api_key),
                resources.getString(R.string.twitter_api_secret_key),
                resources.getString(R.string.twitter_api_callback_uri));
        OAuthActivity.start(this, params, REQUEST_CODE_TWITTER_LOGIN);
    }

    @OnClick(R.id.sign_up_with_facebook)
    @SuppressWarnings("unused")
    protected void doSignUPWithFacebook(){
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_2,
                OAuthParams.OAUTH_FACEBOOK,
                "Facebook",
                "public_profile  email",
                resources.getString(R.string.facebook_api_key),
                resources.getString(R.string.facebook_api_secret_key),
                resources.getString(R.string.facebook_api_callback_uri));
        OAuthActivity.start(this, params, REQUEST_CODE_FACEBOOK_LOGIN);
    }

    @OnClick(R.id.sign_up_with_google_plus)
    @SuppressWarnings("unused")
    protected void doSignUPWithGooglePlus(){
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_2,
                OAuthParams.OAUTH_GOOGLEPLUS,
                "Google+",
                "openid email profile",
                resources.getString(BuildConfig.DEBUG ? R.string.googleplus_api_key_debug : R.string.googleplus_api_key),
                resources.getString(R.string.googleplus_api_secret_key),
                resources.getString(R.string.googleplus_api_callback_uri));
        OAuthActivity.start(this, params, REQUEST_CODE_GOOGLE_PLUS_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FACEBOOK_LOGIN:
                    String facebookToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    //TODO to get userinfo and connect
                    System.out.println("facebookToken:" + facebookToken);
                    UserManager.getInstance(getApplicationContext()).connectWithFacebook(facebookToken, this, new ServerAPI.APICallback() {
                        @Override
                        public void onSucces(Object result) {

                        }

                        @Override
                        public void onFail(int errorCode, String errorMessage) {

                        }
                    });
                    break;
                case REQUEST_CODE_TWITTER_LOGIN:
                    String twitterToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    String twitterSecret = data.getStringExtra(OAuthActivity.EXTEA_SECRET);
                    String twitterRefresh = data.getStringExtra(OAuthActivity.EXTEA_RESPONSE);
                    System.out.println("twitterToken:" + twitterToken);
                    UserManager.getInstance(getApplicationContext()).connectWithTwitter(twitterToken, twitterSecret, twitterRefresh, this, new ServerAPI.APICallback() {
                        @Override
                        public void onSucces(Object result) {

                        }

                        @Override
                        public void onFail(int errorCode, String errorMessage) {

                        }
                    });
                    break;
                case REQUEST_CODE_GOOGLE_PLUS_LOGIN:
                    String googleplusToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    System.out.println("googleplusToken:" + googleplusToken);
                    UserManager.getInstance(getApplicationContext()).connectWithGoogleplus(googleplusToken, this, new ServerAPI.APICallback() {
                        @Override
                        public void onSucces(Object result) {

                        }

                        @Override
                        public void onFail(int errorCode, String errorMessage) {

                        }
                    });
                    break;
            }
        }
    }


    @OnClick(R.id.sign_up_btn)
    @SuppressWarnings("unused")
    protected void doSignUp(){
        String email = emailEdit.getText();
        String password = passwordEdit.getText();
        if(verifyEMail(email) && verifyPassword(password)){
            email = email.trim();
            password = password.trim();

            Bundle args = new Bundle();
            args.putString(EXTRA_EMAIL, email);
            args.putString(EXTRA_PASSWORD, password);
            startLoad(LOADER_SIGN_UP, args);
        }
        //MainActivity.launch(this, null, -1);
    }

    private boolean verifyEMail(String email){
        if(null == email){
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }

        email = email.trim();
        if(0 == email.length()){
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }

        if(!Utils.isEmail(email)){
            emailEdit.setError(getString(R.string.sign_up_email_not_right));
            return false;
        }
        return true;
    }

    private boolean verifyPassword(String password){
        if(null == password){
            passwordEdit.setError(getString(R.string.sign_up_password_can_not_null));
            return false;
        }

        password = password.trim();
        if(0 == password.length()){
            passwordEdit.setError(getString(R.string.sign_up_password_can_not_null));
            return false;
        }
        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        msg.what = what;
        switch (what){
            default :
                break;

            case LOADER_SIGN_UP :
                UserAuthorityLogic logic = UserAuthorityLogic.instance();
                ApiUserAuthority.AttrSignUpInfo attr = new ApiUserAuthority.AttrSignUpInfo();
                attr.email = args.getString(EXTRA_EMAIL, "");
                String password = args.getString(EXTRA_PASSWORD, "");
                attr.password = SecurityUtils.md5(password);
                attr.type = UserManager.SIGN_UP_WITH_EMAIL;
                args.clear();

                UserInfo user = logic.signUp(getApplicationContext(), attr, getCancelableTag());
                msg.obj = user;
                msg.arg1 = STATUS_SUCCESS;
                break;
        }
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        if(isDestroyed() || null == data){
            return;
        }

        switch (data.what){
            default :
                return;

            case LOADER_SIGN_UP :
                if(STATUS_SUCCESS == data.arg1){
                    UserInfo user = (UserInfo) data.obj;
                    if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
                        MainActivity.launch(this, user, -1);
                    }else{
                        //TODO 注册登录失败
                    }
                }
                break;
        }
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

    @OnClick(R.id.sign_up_forget_password)
    @SuppressWarnings("unused")
    protected void doForgetPassword(){
        PasswordResetActivity.launch(this, 0);
    }
}
