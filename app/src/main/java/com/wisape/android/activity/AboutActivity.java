package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * about界面
 * Created by LeiGuoting on 10/7/15.
 */
public class AboutActivity extends BaseActivity {

    @InjectView(R.id.about_version_text)
    @SuppressWarnings("unused")
    protected TextView mVersionText;

    public static void launch(Fragment fragment) {
        Intent intent = new Intent(fragment.getActivity(), AboutActivity.class);
        fragment.startActivity(intent);
    }

    @OnClick(R.id.text_privacy)
    public void onivacyClick(){
        AboutWebViewActivity.launch(this, "http://106.75.196.252/custom/privacy_policy.html", getResources().getString(R.string.about_privacy_policy));
    }

    @OnClick(R.id.text_terms)
    public void onTermsClick(){
        AboutWebViewActivity.launch(this,"http://106.75.196.252/custom/term_service.html",getResources().getString(R.string.about_terms_of_service));
    }

    @OnClick(R.id.text_rate)
    public void onRateClick(){
        AboutWebViewActivity.launch(this,"http://106.75.196.252/custom/term_service.html",getResources().getString(R.string.about_rate));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);
        mVersionText.setText(getResources().getString(R.string.about_version_text) + getPackageInfo(this).versionCode);
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
        if(item.getItemId() == R.id.share_about){
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            intent.setType("text/plain"); // 分享发送的数据类型
            intent.putExtra(Intent.EXTRA_TEXT, "http://106.75.196.252/uploads/app/app-release.apk"); // 分享的内容
            startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
