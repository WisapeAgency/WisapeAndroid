package com.wisape.android.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.widget.CustomProgress;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

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
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wisapeApplication = WisapeApplication.getInstance();
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

    @Override
    protected void startLoad(int what, Bundle args) {
        showProgressDialog(R.string.loading_user_story);
        super.startLoad(what, args);
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
        ButterKnife.reset(this);
    }

    public void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

}