package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.network.VolleyHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * about界面
 * Created by LeiGuoting on 10/7/15.
 */
public class AboutActivity extends BaseActivity {

    private static final String ABOUT_PRIVACY_POLICY = "custom/about/wap/privacy_policy.html";
    private static final String ABOUT_TEAM_SERVICE = "custom/about/wap/term_service.html";
    private static final String ABOUT_CONTENT_SPECIFICATION = "custom/about/wap/content_specification.html";
    private static final String SHARE_URL = "uploads/app/app-release.apk";
    @InjectView(R.id.about_version_text)
    @SuppressWarnings("unused")
    protected TextView mVersionText;

    public static void launch(Fragment fragment) {
        Intent intent = new Intent(fragment.getActivity(), AboutActivity.class);
        fragment.startActivity(intent);
    }

    @OnClick(R.id.text_privacy)
    public void onivacyClick(){
        String url = acquireUri(ABOUT_PRIVACY_POLICY);
        AboutWebViewActivity.launch(this, url, getResources().getString(R.string.about_privacy_policy));
    }

    @OnClick(R.id.text_terms)
    public void onTermsClick(){
        String url = acquireUri(ABOUT_TEAM_SERVICE);
        AboutWebViewActivity.launch(this, url, getResources().getString(R.string.about_terms_of_service));
    }

    @OnClick(R.id.text_content)
    public void onContentClick(){
        String url = acquireUri(ABOUT_CONTENT_SPECIFICATION);
        AboutWebViewActivity.launch(this,url,getResources().getString(R.string.about_content));
    }

    @OnClick(R.id.text_rate)
    public void onRateClick(){
    }

    public String acquireUri(String path) {
        Resources res = getResources();
        String schema = res.getString(R.string.www_schema);
        String host = res.getString(R.string.www_host);
        String port = res.getString(R.string.www_port);
        String authority = String.format("%1$s:%2$s", host, port);

//        String authority = host;
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(schema);
        builder.encodedAuthority(authority);
        builder.encodedPath(path);
        return builder.build().toString();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);
        mVersionText.setText(getResources().getString(R.string.about_version_text) + getPackageInfo(this).versionName);
    }

    private PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),PackageManager.GET_CONFIGURATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String url = acquireUri(SHARE_URL);
        if(item.getItemId() == R.id.share_about){
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            intent.setType("text/plain"); // 分享发送的数据类型
            intent.putExtra(Intent.EXTRA_TEXT, url); // 分享的内容
            startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
