package com.wisape.android.activity;

import android.os.Bundle;

import com.wisape.android.network.Requester;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public abstract class VolleyActivity extends AbsCompatActivity{
    private Object cancelableTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelableTag = new Object();
    }

    protected Object getCancelableTag(){
        return cancelableTag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Requester.instance().cancelAll(cancelableTag);
        cancelableTag = null;
    }
}
