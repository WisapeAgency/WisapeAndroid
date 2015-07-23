package com.wisape.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Duke
 */
public abstract class AbsFragment extends Fragment {
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


    protected FragmentManager getWisapeFragmentManager(){
        return  getActivity().getSupportFragmentManager();
    }


    public boolean onBackPressed() {
        return false;
    }

    protected void handleEventCross(View view){
        view.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
}
