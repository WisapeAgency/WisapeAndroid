package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.widget.TextView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

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
}
