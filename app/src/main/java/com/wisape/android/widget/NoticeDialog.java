package com.wisape.android.widget;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 确认对话框
 * Created by hm on 2015/8/12.
 */
public class NoticeDialog extends DialogFragment {

    private static final String COMFIRM_DIALOG_TITLE = "title";
    private static final String COMFIRM_DIALOG_CONTENT = "content";

    @InjectView(R.id.confirm_dialog_title)
    protected TextView mConfirmTtileText;
    @InjectView(R.id.confirm_dialog_content)
    protected TextView mConfirmContentText;

    @InjectView(R.id.confirm_dialog_btn_cancle)
    protected Button btnCancle;
    @InjectView(R.id.confirm_dialog_btn_confirm)
    protected Button btnClose;

    private String title;
    private String content;





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
        btnCancle.setVisibility(View.GONE);
        btnClose.setText("Close");
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


    @OnClick(R.id.confirm_dialog_btn_confirm)
    @SuppressWarnings("unused")
    protected void onConfirmClicked(){
       this.dismiss();
    }
    /**
     * 实例化对象 并且传递参数
     * @param title  对话框标题
     * @param content 对话框内容
     * @return  返回对话框实例对象
     */
    public static NoticeDialog getInstance(String title,String content){
        Bundle bundle = new Bundle();
        bundle.putString(COMFIRM_DIALOG_TITLE,title);
        bundle.putString(COMFIRM_DIALOG_CONTENT,content);
        NoticeDialog comfirmDialog = new NoticeDialog();
        comfirmDialog.setArguments(bundle);
        return comfirmDialog;
    }

    public interface OnComfirmClickListener{
        void onConfirmClicked();
    }
}
