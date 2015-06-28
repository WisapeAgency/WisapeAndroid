package com.wisape.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wisape.android.R;
import com.wisape.android.common.UserManager;
import com.wisape.android.network.ServerAPI;
import com.wisape.android.view.EditText;
import com.oauth.android.OAuthActivity;
import com.oauth.android.OAuthParams;

import org.json.JSONArray;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Login Activity
 * Created by Xugm on 15/6/10.
 */
public class LoginActivity extends BaseCompatActivity implements View.OnClickListener, ServerAPI.APICallback, IWXAPIEventHandler {

    public static final int REQUEST_CODE_FACEBOOK_LOGIN = 1;
    public static final int REQUEST_CODE_TWITTER_LOGIN = 2;
    public static final int REQUEST_CODE_GOOGLEPLUS_LOGIN = 3;
    public static final int REQUEST_CODE_WECHAT_LOGIN = 4;

    @InjectView(R.id.join)
    Button mJoinBtn;
    @InjectView(R.id.connect_with_facebook)
    ImageView mFacebook;
    @InjectView(R.id.connect_with_twitter)
    ImageView mTwitter;
    @InjectView(R.id.connect_with_googleplus)
    ImageView mGoogleplus;
    @InjectView(R.id.connect_with_wechat)
    ImageView mWechat;
    @InjectView(R.id.username)
    EditText mUsernameEdt;
    @InjectView(R.id.password)
    EditText mPasswordEdt;

    private IWXAPI mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        mJoinBtn.setOnClickListener(this);

        mFacebook.setOnClickListener(this);
        mTwitter.setOnClickListener(this);
        mGoogleplus.setOnClickListener(this);
        mWechat.setOnClickListener(this);

        initWechat();
    }

    private void initWechat() {
        String wechatkey = getResources().getString(R.string.wechat_api_key);
        mApi = WXAPIFactory.createWXAPI(this, wechatkey, true);
        mApi.registerApp(wechatkey);
        mApi.handleIntent(getIntent(), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.join:
                ServerAPI.getAPI(this).loginOrRegWithEmail(
                        mUsernameEdt.getText().toString().trim(),
                        mPasswordEdt.getText().toString().trim(),
                        new ServerAPI.APICallback() {
                            @Override
                            public void onSucces(Object result) {
                                System.out.println((String)result);
                            }

                            @Override
                            public void onFail(int errorCode, String errorMessage) {

                            }
                        });
                break;
            case R.id.connect_with_facebook:
                OAuthParams paramsFB = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_2,
                        OAuthParams.OAUTH_FACEBOOK,
                        "Facebook",
                        "public_profile  email",
                        getResources().getString(R.string.facebook_api_key),
                        getResources().getString(R.string.facebook_api_secret_key),
                        getResources().getString(R.string.facebook_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsFB, REQUEST_CODE_FACEBOOK_LOGIN);
                break;
            case R.id.connect_with_twitter:
                OAuthParams paramsTw = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_1,
                        OAuthParams.OAUTH_TWITTER,
                        "Twitter",
                        "",
                        getResources().getString(R.string.twitter_api_key),
                        getResources().getString(R.string.twitter_api_secret_key),
                        getResources().getString(R.string.twitter_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsTw, REQUEST_CODE_TWITTER_LOGIN);
                break;
            case R.id.connect_with_googleplus:
                OAuthParams paramsGp = new OAuthParams(
                        OAuthParams.VERSION_OAUTH_2,
                        OAuthParams.OAUTH_GOOGLEPLUS,
                        "Google+",
                        "openid email profile",
                        getResources().getString(R.string.googleplus_api_key),
                        getResources().getString(R.string.googleplus_api_secret_key),
                        getResources().getString(R.string.googleplus_api_callback_uri));
                OAuthActivity.start(LoginActivity.this, paramsGp, REQUEST_CODE_GOOGLEPLUS_LOGIN);
                break;
            case R.id.connect_with_wechat:
                // to pull wechat
                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_login";
                req.state = "STATE";
                mApi.sendReq(req);
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

    @Override
    public void onReq(BaseReq baseReq) {
        System.out.println(baseReq.toString());
    }

    @Override
    public void onResp(BaseResp baseResp) {
        System.out.println(baseResp.toString());
    }

}
