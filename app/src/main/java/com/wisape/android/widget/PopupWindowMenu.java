package com.wisape.android.widget;

import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 弹出菜单
 * Created by huangmeng on 15/8/17.
 */
public class PopupWindowMenu extends PopupWindow {

    private OnPuupWindowItemClickListener itemClickListener;

    public PopupWindowMenu(BaseActivity activity,OnPuupWindowItemClickListener onPuupWindowItemClickListener){
        View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.popup_window_layout,null);
        itemClickListener = onPuupWindowItemClickListener;
        setContentView(view);
        ButterKnife.inject(this,view);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(000000));
    }

    @OnClick(R.id.text_edit)
    @SuppressWarnings("unused")
    public void onEditClicked(){
        dismiss();
        itemClickListener.onEditClick();
    }
    @OnClick(R.id.text_result)
    @SuppressWarnings("unused")
    public void onResultClicked(){
        dismiss();
    }
    @OnClick(R.id.text_preview)
    @SuppressWarnings("unused")
    public void onPreviewClicked(){
        dismiss();
        itemClickListener.onPrevidewClick();
    }
    @OnClick(R.id.text_publish)
    @SuppressWarnings("unused")
    public void onPublishClicked(){
        dismiss();
        itemClickListener.onPublishClick();
    }
    @OnClick(R.id.text_delete)
    @SuppressWarnings("unused")
    public void onDeleteClicked(){
        dismiss();
        itemClickListener.onDeleteClick();
    }

   public interface OnPuupWindowItemClickListener{
        void onEditClick();
        void onPrevidewClick();
        void onPublishClick();
        void onDeleteClick();
    }

}
