package com.wisape.android.widget;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 确认对话框
 * Created by hm on 2015/8/12.
 */
public class ComfirmDialog extends DialogFragment {

    private static final String COMFIRM_DIALOG_TITLE = "title";
    private static final String COMFIRM_DIALOG_CONTENT = "content";

    @InjectView(R.id.confirm_dialog_title)
    protected TextView mConfirmTtileText;
    @InjectView(R.id.confirm_dialog_content)
    protected TextView mConfirmContentText;

    private String title;
    private String content;

    private OnComfirmClickListener mOnConfirmClickListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        Window dialogWindow = getDialog().getWindow();
//        dialogWindow.setGravity(Gravity.CENTER);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View rootView = inflater.inflate(R.layout.confirm_dialog, container, false);
        ButterKnife.inject(this,rootView);
        mConfirmTtileText.setText(title);
        mConfirmContentText.setText(content);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            title = bundle.getString(COMFIRM_DIALOG_TITLE);
            content = bundle.getString(COMFIRM_DIALOG_CONTENT);
        }
    }

    public void setOnConfirmClickListener(OnComfirmClickListener onConfirmClickListener){
        mOnConfirmClickListener = onConfirmClickListener;
    }

    @OnClick(R.id.confirm_dialog_btn_cancle)
    @SuppressWarnings("unused")
    protected void onCancleClicked(){
        this.dismiss();
    }

    @OnClick(R.id.confirm_dialog_btn_confirm)
    @SuppressWarnings("unused")
    protected void onConfirmClicked(){
       if(mOnConfirmClickListener != null ){
           mOnConfirmClickListener.onConfirmClicked();
       }
    }
    /**
     * 实例化对象 并且传递参数
     * @param title  对话框标题
     * @param content 对话框内容
     * @return  返回对话框实例对象
     */
    public static ComfirmDialog getInstance(String title,String content){
        Bundle bundle = new Bundle();
        bundle.putString(COMFIRM_DIALOG_TITLE,title);
        bundle.putString(COMFIRM_DIALOG_CONTENT,content);
        ComfirmDialog comfirmDialog = new ComfirmDialog();
        comfirmDialog.setArguments(bundle);
        return comfirmDialog;
    }

    public interface OnComfirmClickListener{
        void onConfirmClicked();
    }
}
