package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
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
import com.wisape.android.R;
import com.wisape.android.common.ProfileRequester;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.SignUpEditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.wisape.android.common.ProfileForFacebookRequester;
import com.wisape.android.common.ProfileForGooglePlusRequester;
import com.wisape.android.common.ProfileForTwitterRequester;

/**
 * 登录界面
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends BaseActivity implements SignUpEditText.OnActionListener {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    private static final String EXTRA_PROFILE_PARAM = "profile_param";

    private static final String ARG_USER_EMIAL = "user_email";
    private static final String ARG_USER_PWD = "user_pwd";

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

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), SignUpActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
    }


    @OnClick(R.id.sign_up_btn)
    @SuppressWarnings("unused")
    protected void doSignUp() {
        String email = emailEdit.getText();
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
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_2,
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
    protected void doSignUPWithFacebook() {
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_2,
                OAuthParams.OAUTH_FACEBOOK,
                "Facebook",
                "public_profile,email",
                resources.getString(R.string.facebook_api_key),
                resources.getString(R.string.facebook_api_secret_key),
                resources.getString(R.string.facebook_api_callback_uri));
        OAuthActivity.start(this, params, REQUEST_CODE_FACEBOOK_LOGIN);
    }

    @OnClick(R.id.sign_up_with_google_plus)
    @SuppressWarnings("unused")
    protected void doSignUPWithGooglePlus() {
        Resources resources = getResources();
        OAuthParams params = new OAuthParams(
                OAuthParams.VERSION_OAUTH_2,
                OAuthParams.OAUTH_GOOGLEPLUS,
                "Google+",
                "openid,email,profile",
                resources.getString(R.string.googleplus_api_key),
                resources.getString(R.string.googleplus_api_secret_key),
                resources.getString(R.string.googleplus_api_callback_uri));
        OAuthActivity.start(this, params, REQUEST_CODE_GOOGLE_PLUS_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case REQUEST_CODE_FACEBOOK_LOGIN:
                if (RESULT_OK == resultCode) {
                    String facebookToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    Log.d(TAG, "#onActivityResult facebookToken:" + facebookToken);
                    ProfileRequester.Param param = new ProfileRequester.Param();
                    param.token = facebookToken;
                    param.screen = data.getStringExtra(OAuthActivity.EXTEA_SECRET);

                   String response =  data.getStringExtra(OAuthActivity.EXTEA_RESPONSE);

                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, param);
                    startLoad(LOADER_SIGN_UP_WITH_FACEBOOK, args);
                } else {
                    showToast("登录失败");
                }
                break;

            case REQUEST_CODE_TWITTER_LOGIN:
                if (RESULT_OK == resultCode) {
                    String twitterToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    String twitterSecret = data.getStringExtra(OAuthActivity.EXTEA_SECRET);
                    String twitterRefresh = data.getStringExtra(OAuthActivity.EXTEA_RESPONSE);
                    Log.d(TAG, "#onActivityResult twitterToken:" + twitterToken + ", twitterSecret:" + twitterSecret + ", twitterRefresh:" + twitterRefresh);

                    Resources resources = getResources();
                    String apiKey = resources.getString(R.string.twitter_api_key);
                    String apiSecret = resources.getString(R.string.twitter_api_secret_key);
                    String apiCallback = resources.getString(R.string.twitter_api_callback_uri);

                    ProfileForTwitterRequester.TwitterParams twParam = new ProfileForTwitterRequester.TwitterParams();
                    twParam.token = twitterToken;
                    twParam.refreshResponse = twitterRefresh;
                    twParam.screen = twitterSecret;
                    twParam.apiKey = apiKey;
                    twParam.apiSecret = apiSecret;
                    twParam.apiCallback = apiCallback;
                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, twParam);
                    startLoad(LOADER_SIGN_UP_WITH_TWITTER, args);
                } else {
                    showToast("登录失败");
                }
                break;

            case REQUEST_CODE_GOOGLE_PLUS_LOGIN:
                if (RESULT_OK == resultCode) {
                    String googlePlusToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    Log.d(TAG, "#onActivityResult googlePlusToken:" + googlePlusToken);
                    ProfileRequester.Param param = new ProfileRequester.Param();
                    param.token = googlePlusToken;

                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_PROFILE_PARAM, param);
                    startLoad(LOADER_SIGN_UP_WITH_GOOGLE_PLUS, args);
                } else {
                    showToast("登录失败");
                }
                break;
        }
    }


    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        UserLogic logic = UserLogic.instance();
        switch (what) {
            case LOADER_SIGN_UP:
                String installId = wisapeApplication.getInstallId();
                msg = logic.signUp(SIGN_UP_WITH_EMAIL,
                        args.getString(ARG_USER_EMIAL), args.getString(ARG_USER_PWD), installId);
                break;

            case LOADER_SIGN_UP_WITH_FACEBOOK:
                ProfileRequester.Param param = args.getParcelable(EXTRA_PROFILE_PARAM);
                ProfileRequester profileRequester = new ProfileForFacebookRequester();
                ProfileRequester.ProfileInfo profile = profileRequester.request(param);
                msg = logic.signUpWith(SIGN_UP_WITH_FACE_BOOK, profile.email, profile.icon, profile.nickName,
                        profile.uniqueStr, wisapeApplication.getInstallId());

                break;

            case LOADER_SIGN_UP_WITH_GOOGLE_PLUS:
                param = args.getParcelable(EXTRA_PROFILE_PARAM);
                profileRequester = new ProfileForGooglePlusRequester();
                profile = profileRequester.request(param);
                msg = logic.signUpWith(SIGN_UP_WITH_GOOGLE_PLUS, profile.email, profile.icon, profile.nickName,
                        profile.uniqueStr, wisapeApplication.getInstallId());
                break;

            case LOADER_SIGN_UP_WITH_TWITTER:
                ProfileForTwitterRequester.TwitterParams twParams = args.getParcelable(EXTRA_PROFILE_PARAM);
                profileRequester = new ProfileForTwitterRequester();
                profile = profileRequester.request(twParams);
                msg = logic.signUpWith(SIGN_UP_WITH_TWITTER, profile.email, profile.icon, profile.nickName,
                        profile.uniqueStr, wisapeApplication.getInstallId());
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
            wisapeApplication.setUserInfo(user);
            MainActivity.launch(this);

        } else {
            showToast("登录失败!");
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

    @OnClick(R.id.sign_up_forget_password)
    @SuppressWarnings("unused")
    protected void doForgetPassword() {
        PasswordResetActivity.launch(this);
    }
}
