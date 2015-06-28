package com.wisape.android.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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


    public boolean onBackPressed() {
        return false;
    }
}
