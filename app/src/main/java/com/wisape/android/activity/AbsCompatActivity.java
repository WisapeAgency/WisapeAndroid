package com.wisape.android.activity;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.wisape.android.util.Utils;

/**
 * @author LeiGuoting
 */
public abstract class AbsCompatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Message>, AsyncTaskLoaderCallback<Message>{

    private static final String TAG = AbsCompatActivity.class.getSimpleName();

    private static final int DEFAULT_LOADER_ID = Integer.MAX_VALUE;
    private static final String EXTRA_WHAT = "loader_what";
    protected static final int STATUS_EXCEPTION = Integer.MIN_VALUE;
    protected static final int STATUS_USER_EXCEPTION = Integer.MIN_VALUE + 1;
    protected static final int STATUS_SUCCESS = 1;
    private boolean destroyed;
    @Override
    protected void onDestroy() {
        if(17 > Build.VERSION.SDK_INT){
            destroyed = true;
        }
        super.onDestroy();
    }

    public boolean isDestroyed() {
        if(17 > Build.VERSION.SDK_INT){
            return destroyed;
        }else{
            return super.isDestroyed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home == item.getItemId()){
            return onBackNavigation();
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onBackNavigation(){
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    /**
     * begin load data with async task
     * @param what message
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
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, inputArgs, this);
    }

    /**
     * @param data
     */
    protected void onLoadCompleted(Message data){
    }

    @Override
    public final Loader<Message> onCreateLoader(int id, Bundle args) {
        Loader<Message> loader = null;
        if(DEFAULT_LOADER_ID == id){
            loader = new AsyncTaskLoaderImpl(getApplicationContext(), args, this);
        }
        return loader;
    }

    @Override
    public final void onLoadFinished(Loader<Message> loader, Message data) {
        if(isDestroyed() || null == data){
            Log.e(TAG,"loadFinished,no data return");
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

    public static class AsyncTaskLoaderImpl extends AsyncTaskLoader<Message>{
        private AsyncTaskLoaderCallback<Message> callback;
        private Bundle args;
        private boolean used;

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
            if(args == null){
                return  null;
            }
            int what = args.getInt(EXTRA_WHAT, 0);
            Message msg;
            try{
                msg = callback.onAsyncLoad(what, args);
                if(null != msg){
                    msg.what = what;
                }
            }catch (AsyncLoaderError error){
                msg = Message.obtain();
                msg.arg1 = STATUS_USER_EXCEPTION;
                msg.obj = error;
            }catch (Throwable e){
                msg = Message.obtain();
                msg.arg1 = STATUS_EXCEPTION;
                msg.obj = e;
            }finally {
                if(null != args){
                    args.clear();
                    args = null;
                }
                callback = null;
                used = true;
            }
            return msg;
        }

        @Override
        protected void onStartLoading() {
            if(used){
                return;
            }
            forceLoad();
        }
    }
}
