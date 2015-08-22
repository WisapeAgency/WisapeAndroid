package com.wisape.android.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.wisape.android.WisapeApplication;

import butterknife.ButterKnife;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public abstract class BaseActivity extends VolleyActivity{

    private ProgressDialog mProgressDialog;
    protected WisapeApplication wisapeApplication;

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
        if(mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
        }
        if(mProgressDialog.isShowing()){
            mProgressDialog.setMessage(getResources().getString(resId));
            return;
        }
        mProgressDialog.setMessage(getResources().getString(resId));
        mProgressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    public void closeProgressDialog(){
        if(mProgressDialog != null && mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
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
