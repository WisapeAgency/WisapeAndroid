package com.wisape.android.fragment;

import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.freshdesk.mobihelp.Mobihelp;
import com.wisape.android.msg.Message;
import com.wisape.android.msg.OperateMessage;
import com.wisape.android.msg.SystemMessage;
import com.wisape.android.msg.UserProfileErrorMessage;
import com.wisape.android.msg.UserProfileMessage;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.AboutActivity;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MessageCenterActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.common.UserManager;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.FrescoFactory;
import com.wisape.android.widget.ComfirmDialog;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment{
    private static final String TAG = MainMenuFragment.class.getSimpleName();

    @InjectView(R.id.sdv_user_head_image)
    SimpleDraweeView sdvUserHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;
    @InjectView(R.id.message_count)
    TextView tvMsgAccount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, null, false);
        ButterKnife.inject(this, rootView);
        EventBus.getDefault().register(this);
        setUserInfodata();
        return rootView;
    }

    private void setUserInfodata(){
        tvName.setText(WisapeApplication.getInstance().getUserInfo().nick_name);
        tvMail.setText(WisapeApplication.getInstance().getUserInfo().user_email);
        String iconUrl = WisapeApplication.getInstance().getUserInfo().user_ico_n;
        if(null != iconUrl && 0 < iconUrl.length()){
            Log.e(TAG,"iconUrl:" + iconUrl);
            sdvUserHeadImage.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
            FrescoFactory.bindImageFromUri(sdvUserHeadImage, iconUrl);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.help_center)
    @SuppressWarnings("unused")
    protected void onHelpCenterClick(View view) {
        Mobihelp.showSupport(getActivity());
    }

    @OnClick(R.id.tv_name)
    @SuppressWarnings("unused")
    protected void onNameClicked() {
        UserProfileActivity.launch(this, UserProfileActivity.REQUEST_CODE_PROFILE);
    }

    /**
     * 清除缓存
     */
    @OnClick(R.id.clear_buffer)
    @SuppressWarnings("unused")
    protected void onClearBufferClicked() {
        final ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.clear_buffer)
                , getString(R.string.clear_cache_notice_text));
        comfirmDialog.show(getFragmentManager(), "clear");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                clearCache();
                comfirmDialog.dismiss();
            }
        });
    }

    /**
     * 清楚缓存
     */
    private void clearCache() {
        Mobihelp.clearUserData(getActivity());
        File file = EnvironmentUtils.getAppCacheDirectory();
        FileUtils.deleteFileInDir(file);
    }

    @OnClick(R.id.exit)
    @SuppressWarnings("unused")
    protected void onLogoutClicked() {
        ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.exit_login), getString(R.string.exit_login_notice_text));
        comfirmDialog.show(getFragmentManager(), "logout");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                clearCache();
                UserManager.instance().clearUser(getActivity());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    @OnClick(R.id.about)
    @SuppressWarnings("unused")
    protected void doAboutClicked() {
        AboutActivity.launch(this);
    }

    @OnClick(R.id.message_center)
    @SuppressWarnings("unused")
    protected void onMessageCenterClicked() {
        MessageCenterActivity.launch(getActivity());
        clearMsgCount();
    }

    public interface UserCallback {
        UserInfo getUserInfo();
    }

    public void onEventMainThread(Message message) {
        if(message instanceof OperateMessage || message instanceof SystemMessage){
            updataMsgCount();
        }
        if(message instanceof UserProfileMessage){
            ((BaseActivity)getActivity()).showToast("信息修改成功!");
            setUserInfodata();
        }
        if(message instanceof UserProfileErrorMessage){
            ((BaseActivity)getActivity()).showToast("信息修改失败!");
        }
    }

    private void updataMsgCount() {
        Log.e(TAG, "更新消息数量");
        if(tvMsgAccount.getVisibility() == View.GONE){
            tvMsgAccount.setVisibility(View.VISIBLE);
        }
        tvMsgAccount.setText(Integer.parseInt(tvMsgAccount.getText().toString()) + 1 + "");
    }

    private void clearMsgCount(){
        tvMsgAccount.setText("0");
        tvMsgAccount.setVisibility(View.GONE);
    }
}
