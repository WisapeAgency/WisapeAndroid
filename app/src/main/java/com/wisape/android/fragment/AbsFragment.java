package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.AbsCompatActivity;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.util.Utils;

/**
 * @author Duke
 */
public abstract class AbsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Message> ,FragmentLoaderCallback{

    private static final int DEFAULT_LOADER_ID = Integer.MAX_VALUE;
    private static final String TAG = AbsCompatActivity.class.getSimpleName();
    private static final String EXTRA_WHAT = "loader_what";

    public DisplayMetrics mDisplayMetrics;
    protected WisapeApplication wisapeApplication;

    private boolean destroyed;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDisplayMetrics = getResources().getDisplayMetrics();
        wisapeApplication = WisapeApplication.getInstance();
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


    protected void handleEventCross(View view){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    protected void showProgressDialog(int resId){
        ((BaseActivity) getActivity()).showProgressDialog(resId);
    }

    protected void closeProgressDialog(){
        ((BaseActivity) getActivity()).closeProgressDialog();
    }

    protected void showToast(String message){
        ((BaseActivity) getActivity()).showToast(message);
    }

    @Override
    public FragmentLoader onCreateLoader(int id, Bundle args) {
        FragmentLoader loader = null;
        if(DEFAULT_LOADER_ID == id){
            loader = new FragmentLoader(getActivity().getApplicationContext(), args, this);
        }
        return loader;
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Message> loader) {
        loader.reset();
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Message> loader, Message data) {
        if(isDestroyed() || null == data){
            Log.e(TAG, "loadFinished,no data return");
            return;
        }
        try{
            onLoadComplete(data);
        }finally {
            data.recycle();
        }
    }

    public static class FragmentLoader extends AsyncTaskLoader<Message> {

        private Bundle args;
        private FragmentLoaderCallback callBack;
        private boolean used;

        public FragmentLoader(Context context,Bundle args,FragmentLoaderCallback callBack){
            super(context);
            this.args = args;
            this.callBack = callBack;
        }

        @Override
        protected void onStartLoading() {
            if(used){
                return;
            }
            forceLoad();
        }


        @Override
        public Message loadInBackground() {
            int what = args.getInt(EXTRA_WHAT, 0);
            used = true;
            return callBack.loadingInbackground(what, args);
        }
    }

    @Override
    public Message loadingInbackground(int what, Bundle args) {
        return null;
    }


    protected void onLoadComplete(Message data) {
        closeProgressDialog();
    }

    @Override
    public void onDetach() {
        if(17 > Build.VERSION.SDK_INT){
            destroyed = true;
        }
        super.onDetach();
    }

    public boolean isDestroyed() {
        if(17 > Build.VERSION.SDK_INT){
            return destroyed;
        }else{
            return super.isDetached();
        }
    }

    /**
     * begin load data with async task
     * @param what message
     * @param args can was null
     */
    protected void startLoad(int what, Bundle args){

        if(!Utils.isNetworkAvailable(getActivity())){
            closeProgressDialog();
            showToast("Network Error");
            return;
        }

        Bundle inputArgs;
        if(null == args){
            inputArgs = new Bundle();
        }else{
            inputArgs = args;
        }
        inputArgs.putInt(EXTRA_WHAT, what);
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, inputArgs, this);
    }

    protected void startLoadWithProgress(int what,Bundle args){
        showProgressDialog(R.string.progress_loading_data);
        startLoad(what,args);
    }
}
