package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 用于显示about界面点击进入后的显示界面
 * Created by lenovo on 2015/8/12.
 */
public class AboutWebViewActivity extends BaseActivity {

    private static final String WEB_URL= "url";
    private static final String ACTIVITY_TITLE = "title";

    @InjectView(R.id.about_web_view)
    @SuppressWarnings("unused")
    protected WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_web_view);
        ButterKnife.inject(this);
        getSupportActionBar().setTitle(getIntent().getStringExtra(ACTIVITY_TITLE));
        mWebView.loadUrl(getIntent().getStringExtra(WEB_URL));
    }

    public static void launch(Activity activity,String url,String title){
        Intent intent = new Intent(activity.getApplicationContext(),AboutWebViewActivity.class);
        intent.putExtra(WEB_URL,url);
        intent.putExtra(ACTIVITY_TITLE,title);
        activity.startActivity(intent);
    }
}
