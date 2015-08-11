package com.wisape.android.cordova;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Message;

import com.wisape.android.activity.AsyncTaskLoaderCallback;
import com.wisape.android.network.Requester;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import java.util.HashMap;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public abstract class AbsPlugin extends CordovaPlugin  implements LoaderManager.LoaderCallbacks<Message>, AsyncTaskLoaderCallback<Message> {
    private static final int DEFAULT_LOADER_ID = Integer.MAX_VALUE;
    private static final String EXTRA_WHAT = "loader_what";
    protected static final int STATUS_EXCEPTION = Integer.MIN_VALUE;
    protected static final int STATUS_SUCCESS = 1;

    protected CordovaInterface cordova;
    private volatile boolean destroyed;

    private Object cancelableTag;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        cancelableTag = new Object();
    }

    protected Activity getCurrentActivity(){
        return cordova.getActivity();
    }

    protected void startActivityForResult(Intent intent, int requestCode){
        cordova.startActivityForResult(this, intent, requestCode);
    }

    /**
     * begin load data with async task
     * @param what
     * @param args can was null
     */
    protected void startLoad(int what, Bundle args){
        Bundle inputArgs;
        if(null == args){
            inputArgs = new Bundle();
        }else{
            inputArgs = args;
        }
        inputArgs.putInt(EXTRA_WHAT, what);
        onPreLoad(what);
        getCurrentActivity().getLoaderManager().restartLoader(DEFAULT_LOADER_ID, inputArgs, this);
    }

    /**
     * @param data
     */
    protected void onLoadCompleted(Message data){}

    protected void onPreLoad(int what){}

    @Override
    public final Loader<Message> onCreateLoader(int id, Bundle args) {
        Loader<Message> loader = null;
        if(DEFAULT_LOADER_ID == id){
            loader = new AsyncTaskLoaderImpl(getCurrentActivity().getApplicationContext(), args, this);
        }
        return loader;
    }

    @Override
    public final void onLoadFinished(Loader<Message> loader, Message data) {
        if(isDestroyed() || null == data){
            return;
        }
        try{
            onLoadCompleted(data);
        }finally {
            data.recycle();
        }
    }

    @Override
    public final void onLoaderReset(Loader<Message> loader) {
        loader.reset();
    }

    @Override
    public final Message onAsyncLoad(int what, Bundle args) throws AsyncLoaderError{
        return onLoadBackgroundRunning(what, args);
    }

    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError{
        return null;
    }

    private static class AsyncTaskLoaderImpl extends AsyncTaskLoader<Message> {
        private AsyncTaskLoaderCallback<Message> callback;
        private Bundle args;

        public AsyncTaskLoaderImpl(Context context, Bundle args, AsyncTaskLoaderCallback<Message> callback) {
            super(context);
            if(null == callback){
                throw new IllegalArgumentException("AsyncTaskLoaderCallback can not be null.");
            }

            if(null == args){
                throw new IllegalArgumentException("args can not be null.");
            }
            this.callback = callback;
            this.args = args;
        }

        @Override
        public Message loadInBackground() {
            int what = args.getInt(EXTRA_WHAT, 0);
            Message msg;
            try{
                msg = callback.onAsyncLoad(what, args);
                if(null != msg){
                    msg.what = what;
                    msg.arg1 = STATUS_SUCCESS;
                }
            }catch (AsyncLoaderError error){
                msg = Message.obtain();
                msg.arg1 = STATUS_EXCEPTION;
                msg.obj = error;
            }finally {
                if(null != args){
                    args.clear();
                    args = null;
                }
                callback = null;
            }
            return msg;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
        cordova = null;

        if(needCancelNetworkRequest()){
            Requester.instance().cancelAll(cancelableTag);
        }
        cancelableTag = null;
    }

    protected boolean needCancelNetworkRequest(){
        return false;
    }
}
