package com.wisape.android.activity;

import android.app.ProgressDialog;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public abstract class BaseActivity extends VolleyActivity{

    private ProgressDialog mProgressDialog;


    /**
     * 显示进度对话框
     * @param resId  显示的字符串资源ID
     */
    protected void showProgressDialog(@Nullable int resId){
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
    protected void closeProgressDialog(){
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

    protected void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

}
