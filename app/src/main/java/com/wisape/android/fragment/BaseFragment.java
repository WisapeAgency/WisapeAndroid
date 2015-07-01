package com.wisape.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

/**
 * @author Duke
 */
public abstract class BaseFragment extends Fragment {
    public DisplayMetrics mDisplayMetrics;
    public Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayMetrics = getResources().getDisplayMetrics();
    }

    public void setTitle(CharSequence title){
        AppCompatActivity compatActivity = (AppCompatActivity) getActivity();
        if(null != compatActivity){
            ActionBar actionBar = compatActivity.getSupportActionBar();
            if(null != actionBar){
                actionBar.setTitle(title);
            }
        }
    }

    public void setTitle(int resId){
        Activity activity = getActivity();
        if(null != activity){
            setTitle(activity.getString(resId));
        }
    }


    public boolean onBackPressed() {
        return false;
    }
}
