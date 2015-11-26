package com.wisape.android.fragment;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bugtags.library.ui.rounded.CircleImageView;
import com.bumptech.glide.Glide;
import com.freshdesk.mobihelp.Mobihelp;
import com.wisape.android.R;
import com.wisape.android.activity.AboutActivity;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.content.UpdateUserInfoBroadcastReciver;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.widget.ComfirmDialog;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

//import com.wisape.android.content.MessageCenterReceiver;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment implements UpdateUserInfoBroadcastReciver.UpdateUserInfoBoradcastReciverListener {

    private static final int CLEAR_CACHE = 1;

    @InjectView(R.id.sdv_user_head_image)
    CircleImageView userHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;
    @InjectView(R.id.message_count)
    TextView tvMsgAccount;
    private double totleSize;
    private UpdateUserInfoBroadcastReciver userInfoBoradcastReciver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ButterKnife.inject(this, rootView);
        setUserInfodata();
        registerReciver();
        return rootView;
    }

    private void registerReciver() {
        userInfoBoradcastReciver = new UpdateUserInfoBroadcastReciver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UpdateUserInfoBroadcastReciver.ACTION);
        getActivity().registerReceiver(userInfoBoradcastReciver, filter);
    }


    /**
     * 设置用户信息
     */
    private void setUserInfodata() {
        tvName.setText(UserLogic.instance().getUserInfoFromLocal().nick_name);
        tvMail.setText(UserLogic.instance().getUserInfoFromLocal().user_email);
        String iconUrl = UserLogic.instance().getUserInfoFromLocal().user_ico_n;
        if (null != iconUrl && 0 < iconUrl.length()) {
            Glide.with(getActivity()).load(iconUrl)
                    .override(150, 150)
                    .centerCrop()
                    .crossFade()
                    .into(userHeadImage);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        userInfoBoradcastReciver.destory();
        getActivity().unregisterReceiver(userInfoBoradcastReciver);
    }


    @OnClick(R.id.help_center)
    @SuppressWarnings("unused")
    protected void onHelpCenterClick(View view) {
        Mobihelp.showSupport(getActivity());
    }

    @OnClick(R.id.linear_head)
    @SuppressWarnings("unused")
    protected void onLinearHeadClicked() {
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
                startLoadWithProgress(CLEAR_CACHE, null);
                comfirmDialog.dismiss();
            }
        });
    }

    /**
     * 清除缓存
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
                UserLogic.instance().clearUserInfo();
                SignUpActivity.launch(getActivity());
                getActivity().finish();
            }
        });
    }

    @OnClick(R.id.about)
    @SuppressWarnings("unused")
    protected void doAboutClicked() {
        AboutActivity.launch(this);
    }

    @Override
    public void updateUserInfo() {
        setUserInfodata();
    }


    @Override
    public Message loadingInbackground(int what, Bundle args) {
        Message message = Message.obtain();
        switch (what) {
            case CLEAR_CACHE:
                totleSize = getDirSize(EnvironmentUtils.getAppCacheDirectory());
                clearCache();
                break;
        }
        message.what = what;
        return message;
    }

    @Override
    protected void onLoadComplete(Message data) {
        closeProgressDialog();
        super.onLoadComplete(data);
        int size = (int) totleSize;
        switch (data.what) {
            case CLEAR_CACHE:
                if (size > 0) {
                    showToast("Clear cache successful,total size :" + size + "MB");
                } else {
                    showToast("Clear cache successful,total size :" + totleSize * 1024 + "KB");
                }
                break;
        }
    }

    public double getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“兆”为单位
                return (float) file.length() / 1024 / 1024;
            }
        }
        return 0.0;
    }
}
