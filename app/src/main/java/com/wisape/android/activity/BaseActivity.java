package com.wisape.android.activity;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.widget.Toast;

import com.bugtags.library.Bugtags;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.CustomProgress;

import butterknife.ButterKnife;

/**
 * 基本activity
 * Created by LeiGuoting on 3/7/15.
 */
public abstract class BaseActivity extends VolleyActivity{

    private CustomProgress customProgress;
    protected WisapeApplication wisapeApplication;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Bugtags.onDispatchTouchEvent(this,ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wisapeApplication = WisapeApplication.getInstance();
        wisapeApplication.getTracker(WisapeApplication.TrackerName.APP_TRACKER);
    }


    /**
     * 显示进度对话框
     * @param resId  显示的字符串资源ID
     */
    public void showProgressDialog(@Nullable int resId){

        if(customProgress == null){
            customProgress = CustomProgress.show(this,getResources().getString(resId),true);
        }
        if(customProgress.isShowing()){
            customProgress.setMessage(getResources().getString(resId));
            return;
        }
        customProgress.setMessage(getResources().getString(resId));
        customProgress.show();
    }

    /**
     * 关闭进度对话框
     */
    public void closeProgressDialog(){
        if(customProgress != null && customProgress.isShowing()){
            customProgress.dismiss();
        }
    }


    protected void startLoadWithProgress(int what,Bundle args){
        showProgressDialog(R.string.progress_loading_data);
        startLoad(what, args);
    }


    @Override
    protected void onLoadCompleted(Message data) {
        closeProgressDialog();
        if(isDestroyed() || null == data){
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showToast(String msg){
        Utils.showToast(this,msg);
    }

}