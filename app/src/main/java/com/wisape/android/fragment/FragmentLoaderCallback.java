package com.wisape.android.fragment;

import android.os.Bundle;
import android.os.Message;

/**
 * Created by huangmeng on 15/9/4.
 */
public interface FragmentLoaderCallback {

    Message loadingInbackground(int what, Bundle args);
}
