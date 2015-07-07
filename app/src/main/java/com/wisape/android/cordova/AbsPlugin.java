package com.wisape.android.cordova;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public abstract class AbsPlugin extends CordovaPlugin{
    private CordovaInterface cordova;
    private volatile boolean destroyed;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
    }

    protected Activity getCurrentActivity(){
        return cordova.getActivity();
    }

    protected void startActivityForResult(Intent intent, int requestCode){
        cordova.startActivityForResult(this, intent, requestCode);
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
        cordova = null;
    }
}
