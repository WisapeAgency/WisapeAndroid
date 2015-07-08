package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.oauth.android.OAuthActivity;
import com.oauth.android.OAuthParams;
import com.wisape.android.BuildConfig;
import com.wisape.android.R;
import com.wisape.android.common.FacebookProfileRequester;
import com.wisape.android.common.GooglePlusProfileRequester;
import com.wisape.android.common.ProfileRequester;
import com.wisape.android.common.TwitterProfileRequester;
import com.wisape.android.common.UserManager;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUser;
import com.wisape.android.network.Requester;
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
    private static final String EXTRA_PROFILE_PARAM = "profile_param";
    private static final int LOADER_SIGN_UP = 1;
    private static final int LOADER_SIGN_UP_WITH_FACEBOOK = 2;
    private static final int LOADER_SIGN_UP_WITH_TWITTER = 3;
    private static final int LOADER_SIGN_UP_WITH_GOOGLE_PLUS = 4;

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
        switch(requestCode){
            default :
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case REQUEST_CODE_FACEBOOK_LOGIN :
                if(RESULT_OK == resultCode){
                    String facebookToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    Log.d(TAG, "#onActivityResult facebookToken:" + facebookToken);
                    ProfileRequester.Param param = new ProfileRequester.Param();
                    param.token = facebookToken;
                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, param);
                    startLoad(LOADER_SIGN_UP_WITH_FACEBOOK, args);
                }else{
                    //TODO OAuth failed
                }
                break;

            case REQUEST_CODE_TWITTER_LOGIN :
                if(RESULT_OK == resultCode){
                    String twitterToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    String twitterSecret = data.getStringExtra(OAuthActivity.EXTEA_SECRET);
                    String twitterRefresh = data.getStringExtra(OAuthActivity.EXTEA_RESPONSE);
                    Log.d(TAG, "#onActivityResult twitterToken:" + twitterToken + ", twitterSecret:" + twitterSecret + ", twitterRefresh:" + twitterRefresh);

                    Resources resources = getResources();
                    String apiKey = resources.getString(R.string.twitter_api_key);
                    String apiSecret = resources.getString(R.string.twitter_api_secret_key);
                    String apiCallback = resources.getString(R.string.twitter_api_callback_uri);

                    TwitterProfileRequester.TwitterParams twParam = new TwitterProfileRequester.TwitterParams();
                    twParam.token = twitterToken;
                    twParam.refreshResponse = twitterRefresh;
                    twParam.screen = twitterSecret;
                    twParam.apiKey = apiKey;
                    twParam.apiSecret = apiSecret;
                    twParam.apiCallback = apiCallback;
                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, twParam);
                    startLoad(LOADER_SIGN_UP_WITH_TWITTER, args);
                }else{
                    //TODO OAuth failed
                }
                break;

            case REQUEST_CODE_GOOGLE_PLUS_LOGIN :
                if(RESULT_OK == resultCode){
                    String googlePlusToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    Log.d(TAG, "#onActivityResult googlePlusToken:" + googlePlusToken);
                    ProfileRequester.Param param = new ProfileRequester.Param();
                    param.token = googlePlusToken;

                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, param);
                    startLoad(LOADER_SIGN_UP_WITH_GOOGLE_PLUS, args);
                }else{
                    //TODO OAuth failed
                }
                break;
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
                UserLogic logic = UserLogic.instance();
                ApiUser.AttrSignUpInfo attr = new ApiUser.AttrSignUpInfo();
                attr.email = args.getString(EXTRA_EMAIL, "");
                String password = args.getString(EXTRA_PASSWORD, "");
                attr.password = SecurityUtils.md5(password);
                attr.type = UserManager.SIGN_UP_WITH_EMAIL;
                args.clear();

                UserInfo user = logic.signUp(getApplicationContext(), attr, getCancelableTag());
                msg.obj = user;
                msg.arg1 = STATUS_SUCCESS;
                break;

            case LOADER_SIGN_UP_WITH_FACEBOOK :
                ProfileRequester.Param param = args.getParcelable(EXTRA_PROFILE_PARAM);
                ProfileRequester profileRequester = new FacebookProfileRequester();
                ProfileRequester.ProfileInfo profile = profileRequester.request(param);

                logic = UserLogic.instance();
                user = logic.signUpWith(getApplicationContext(), profile, getCancelableTag());
                msg.obj = user;
                msg.arg1 = STATUS_SUCCESS;
                break;

            case LOADER_SIGN_UP_WITH_GOOGLE_PLUS :
                param = args.getParcelable(EXTRA_PROFILE_PARAM);
                profileRequester = new GooglePlusProfileRequester();
                profile = profileRequester.request(param);

                logic = UserLogic.instance();
                user = logic.signUpWith(getApplicationContext(), profile, getCancelableTag());
                msg.obj = user;
                msg.arg1 = STATUS_SUCCESS;
                break;

            case LOADER_SIGN_UP_WITH_TWITTER :
                TwitterProfileRequester.TwitterParams twParams = args.getParcelable(EXTRA_PROFILE_PARAM);
                profileRequester = new TwitterProfileRequester();
                profile = profileRequester.request(twParams);

                logic = UserLogic.instance();
                user = logic.signUpWith(getApplicationContext(), profile, getCancelableTag());
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

            case LOADER_SIGN_UP_WITH_TWITTER :
            case LOADER_SIGN_UP_WITH_GOOGLE_PLUS :
            case LOADER_SIGN_UP_WITH_FACEBOOK :
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
