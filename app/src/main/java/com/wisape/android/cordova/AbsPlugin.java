package com.wisape.android.cordova;

import android.app.Activity;

import com.wisape.android.activity.AbsCordovaActivity;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class AbsPlugin extends CordovaPlugin{
    private AbsCordovaActivity.CordovaWebViewTag tag;
    private volatile boolean destroyed;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        tag = (AbsCordovaActivity.CordovaWebViewTag) webView.getView().getTag(AbsCordovaActivity.CordovaWebViewTag.TAG_KEY);
    }

    protected Activity getCurrentActivity(){
        return tag.activity;
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
        tag = null;
    }
}
