package com.oauth.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.SinaWeiboApi20;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * OAuth
 * Created by malei on 14/12/2.
 */
public class OAuthActivity extends AppCompatActivity {

    public static final String EXTEA_TOKEN = "token";
    public static final String EXTEA_SECRET = "secret";
    public static final String EXTEA_RESPONSE = "response";
    private final String tag = OAuthActivity.class.getSimpleName();
    //
    private WebView mWebView;
    private ProgressBar mProgressBar;
    //
    private OAuthParams mOAuthParams;
    private OAuthService mOAuthService;
    private Token mRequestToken;

    public static void start(Activity activity, OAuthParams params, int requestCode) {
        Intent intent = new Intent(activity, OAuthActivity.class);
        intent.putExtra("params", params);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOAuthParams = getIntent().getParcelableExtra("params");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mOAuthParams.getName());
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_oauth);
        mWebView = (WebView) findViewById(R.id.webWiew);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        //
        Class<? extends Api> clazz = null;
        switch (mOAuthParams.getOauth()) {
            case OAuthParams.OAUTH_FACEBOOK:
                clazz = FacebookApi.class;
                break;

            case OAuthParams.OAUTH_TWITTER:
                clazz = TwitterApi.class;
                break;

            case OAuthParams.OAUTH_SOUNDCLOUD:
                clazz = SoundCloudApi.class;
                break;

            case OAuthParams.OAUTH_SINA:
                clazz = SinaWeiboApi20.class;
                break;

            case OAuthParams.OAUTH_GOOGLEPLUS:
                clazz = GoogleApi20.class;
                break;

            case OAuthParams.OAUTH_WECHAT:
                clazz = WechatApi.class;
                break;

            default:
                break;
        }
        if (mOAuthParams.getOauth() == OAuthParams.OAUTH_TWITTER) {
            mOAuthService = new ServiceBuilder()
                    .provider(clazz)
                    .apiKey(mOAuthParams.getApiKey())
                    .apiSecret(mOAuthParams.getApiSecret())
                    .callback(mOAuthParams.getCallback())
                    .build();
        } else {
            mOAuthService = new ServiceBuilder()
                    .provider(clazz)
                    .apiKey(mOAuthParams.getApiKey())
                    .apiSecret(mOAuthParams.getApiSecret())
                    .callback(mOAuthParams.getCallback())
                    .scope(mOAuthParams.getScope())
                    .build();
        }
        //
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(tag, "---> shouldOverrideUrlLoading url = " + url);
                if (!url.startsWith(mOAuthParams.getCallback()))
                    return super.shouldOverrideUrlLoading(view, url);
                Uri uri = Uri.parse(url);
                String key = mOAuthParams.getVersion() == OAuthParams.VERSION_OAUTH_1 ? "oauth_verifier" : "code";
                String oauthVerifier = uri.getQueryParameter(key);
                Log.d(tag, "---> shouldOverrideUrlLoading oauthVerifier = " + oauthVerifier);
                if (oauthVerifier != null) loadAccessToken(oauthVerifier);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mWebView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        loadAuthorizationUrl();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadAuthorizationUrl() {
        new AsyncTask<Void, Void, Message>() {
            @Override
            protected Message doInBackground(Void... params) {
                Message message = new Message();
                try {
                    if (mOAuthParams.getVersion() == OAuthParams.VERSION_OAUTH_1)
                        mRequestToken = mOAuthService.getRequestToken();
                    String authorizationUrl = mOAuthService.getAuthorizationUrl(mRequestToken);
                    message.what = 1;
                    message.obj = authorizationUrl;
                } catch (Exception e) {
                    e.printStackTrace();
                    message.what = -1;
                    message.obj = "network error";
                }
                return message;
            }

            @Override
            protected void onPostExecute(Message message) {
                super.onPostExecute(message);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    if (isDestroyed()) return;
                }
                switch (message.what) {
                    case -1:
                        Toast.makeText(getApplicationContext(), message.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        mWebView.loadUrl(message.obj.toString());
                        break;
                }
            }
        }.execute();
    }

    private void loadAccessToken(String verifier) {
        new AsyncTask<String, Void, Message>() {
            private ProgressDialog mDialog;

            @Override
            protected void onPreExecute() {
                mDialog = new ProgressDialog(OAuthActivity.this);
                mDialog.show();
            }

            @Override
            protected Message doInBackground(String... params) {
                Message message = new Message();
                Verifier verifier = new Verifier(params[0]);
                try {
                    Token accessToken = mOAuthService.getAccessToken(mRequestToken, verifier);
                    message.what = 1;
                    message.obj = accessToken;
                } catch (Exception e) {
                    e.printStackTrace();
                    message.what = -1;
                    message.obj = "network error";
                }
                return message;
            }

            @Override
            protected void onPostExecute(Message message) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    if (isDestroyed()) return;
                }
                mDialog.dismiss();
                switch (message.what) {
                    case -1:
                        Toast.makeText(getApplicationContext(), message.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        Token accessToken = (Token) message.obj;
                        setResult(RESULT_OK, new Intent()
                                .putExtra(EXTEA_TOKEN, accessToken.getToken())
                                .putExtra(EXTEA_SECRET, accessToken.getSecret())
                                .putExtra(EXTEA_RESPONSE, accessToken.getRawResponse()));
                        finish();
                        break;
                }
            }
        }.execute(verifier);
    }
}
