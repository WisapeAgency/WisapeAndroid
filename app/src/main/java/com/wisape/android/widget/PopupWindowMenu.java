package com.wisape.android.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.logic.StoryLogic;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 弹出菜单
 * Created by huangmeng on 15/8/17.
 */
public class PopupWindowMenu extends PopupWindow {

    private static final String TAG = PopupWindowMenu.class.getSimpleName();

    private static long storyId;

    private BaseActivity activity;

    public PopupWindowMenu(BaseActivity activity){
        View view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.popup_window_layout,null);
        this.activity = activity;
        setContentView(view);
        ButterKnife.inject(this,view);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(000000));
    }

    public static void setStoryId(long id){
        storyId = id;
    }

    @OnClick(R.id.text_edit)
    public void onEditClicked(){
        Log.e(TAG,"#onEdit:" + storyId);
        dismiss();
    }
    @OnClick(R.id.text_result)
    public void onResultClicked(){
        dismiss();
    }
    @OnClick(R.id.text_preview)
    public void onPreviewClicked(){
        dismiss();
    }
    @OnClick(R.id.text_publish)
    public void onPublishClicked(){
        dismiss();
    }
    @OnClick(R.id.text_delete)
    public void onDeleteClicked(){
        dismiss();
//        activity.showProgressDialog(R.string.loading_user_story);
//        StoryLogic.instance().deleteStory(storyId, WisapeApplication.getInstance().getUserInfo().access_token);
    }
}
