package com.wisape.android.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.oauth.android.OAuthActivity;
import com.oauth.android.OAuthParams;
import com.wisape.android.BuildConfig;
import com.wisape.android.R;
import com.wisape.android.common.UserManager;
import com.wisape.android.logic.UserAuthorityLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUserAuthority;
import com.wisape.android.network.ServerAPI;
import com.wisape.android.view.EditText;

import org.json.JSONArray;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Login Activity
 * Created by Xugm on 15/6/10.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, ServerAPI.APICallback{
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static final int REQUEST_CODE_FACEBOOK_LOGIN = 1;
    public static final int REQUEST_CODE_TWITTER_LOGIN = 2;
    public static final int REQUEST_CODE_GOOGLEPLUS_LOGIN = 3;
    public static final int REQUEST_CODE_WECHAT_LOGIN = 4;

    private static final String EXTRA_ATTR_SIGNUP = "attr_signup_info";

    @InjectView(R.id.join)
    Button joinBtn;
    @InjectView(R.id.connect_with_facebook)
    ImageView facebook;
    @InjectView(R.id.connect_with_twitter)
    ImageView twitter;
    @InjectView(R.id.connect_with_googleplus)
    ImageView googlePlus;
    @InjectView(R.id.username)
    EditText userNameEdt;
    @InjectView(R.id.password)
    EditText passwordEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        ApiUserAuthority.AttrSignUpInfo signupInfo = args.getParcelable(EXTRA_ATTR_SIGNUP);
        UserInfo user = UserAuthorityLogic.instance().signUp(getApplicationContext(), signupInfo, this);
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {

    }

    @Override
    public void onClick(View v) {
        Resources resources = getResources();
        switch (v.getId()) {
            case R.id.join:
                ApiUserAuthority.AttrSignUpInfo signupInfo = new ApiUserAuthority.AttrSignUpInfo();
                Bundle args = new Bundle();
                args.putParcelable(EXTRA_ATTR_SIGNUP, signupInfo);
                startLoad(0, args);
                break;
            case R.id.connect_with_facebook:
                OAuthParams paramsFB = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_2,
                        OAuthParams.OAUTH_FACEBOOK,
                        "Facebook",
                        "public_profile  email",
                        resources.getString(R.string.facebook_api_key),
                        resources.getString(R.string.facebook_api_secret_key),
                        resources.getString(R.string.facebook_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsFB, REQUEST_CODE_FACEBOOK_LOGIN);
                break;
            case R.id.connect_with_twitter:
                OAuthParams paramsTw = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_1,
                        OAuthParams.OAUTH_TWITTER,
                        "Twitter",
                        "",
                        resources.getString(R.string.twitter_api_key),
                        resources.getString(R.string.twitter_api_secret_key),
                        resources.getString(R.string.twitter_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsTw, REQUEST_CODE_TWITTER_LOGIN);
                break;
            case R.id.connect_with_googleplus:
                OAuthParams paramsGp = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_2,
                        OAuthParams.OAUTH_GOOGLEPLUS,
                        "Google+",
                        "openid email profile",
                        resources.getString(BuildConfig.DEBUG ? R.string.googleplus_api_key_debug : R.string.googleplus_api_key),
                        resources.getString(R.string.googleplus_api_secret_key),
                        resources.getString(R.string.googleplus_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsGp, REQUEST_CODE_GOOGLEPLUS_LOGIN);
                break;
        }
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
                case REQUEST_CODE_GOOGLEPLUS_LOGIN:
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
                case REQUEST_CODE_WECHAT_LOGIN:
                    String wechatToken = data.getStringExtra(OAuthActivity.EXTEA_TOKEN);
                    String wechatSecret = data.getStringExtra(OAuthActivity.EXTEA_SECRET);
                    String wechatRefresh = data.getStringExtra(OAuthActivity.EXTEA_RESPONSE);
                    //TODO to get userinfo and connect
                    System.out.println("wechatToken:" + wechatToken);
                    break;
            }
        }
    }

    @Override
    public void onSucces(Object result) {
        JSONArray array = (JSONArray) result;
        System.out.println(array.toString());
    }

    @Override
    public void onFail(int errorCode, String errorMessage) {
        System.out.println("errorCode:" + errorCode + "|" + "errorMessage:" + errorMessage);
    }
}
