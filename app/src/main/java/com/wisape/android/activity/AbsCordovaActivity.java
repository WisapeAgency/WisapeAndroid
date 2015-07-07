package com.wisape.android.activity;

import android.app.Activity;
import android.view.View;

import com.wisape.android.R;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;

/**
 *
 * Created by LeiGuoting on 3/7/15.
 */
public abstract class AbsCordovaActivity extends CordovaActivity {

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
        CordovaWebView appView = this.appView;
        appView.getView().setTag(CordovaWebViewTag.TAG_KEY, new CordovaWebViewTag(this));
    }

    @Override
    public void onDestroy() {
        CordovaWebView appView = this.appView;
        View webView = appView.getView();
        CordovaWebViewTag tag = (CordovaWebViewTag)webView.getTag(CordovaWebViewTag.TAG_KEY);
        if(null != tag){
            tag.destory();
        }
        super.onDestroy();
    }

    public static class CordovaWebViewTag{
        public static final int TAG_KEY = R.integer.cordova_tag_key;
        public Activity activity;

        public CordovaWebViewTag(Activity activity){
            this.activity = activity;
        }
        public void destory(){
            activity = null;
        }
    }
}
