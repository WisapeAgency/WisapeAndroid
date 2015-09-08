package com.wisape.android.fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.freshdesk.mobihelp.Mobihelp;
import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.activity.AboutActivity;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MessageCenterActivity;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.content.BroadCastReciverListener;
import com.wisape.android.content.MessageCenterReceiver;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.view.CircleTransform;
import com.wisape.android.widget.ComfirmDialog;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment implements BroadCastReciverListener{

    @InjectView(R.id.sdv_user_head_image)
    ImageView userHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;
    @InjectView(R.id.message_count)
    TextView tvMsgAccount;

    private MessageCenterReceiver messageCenterReceiver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ButterKnife.inject(this, rootView);
        setUserInfodata();
        registerReciver();
        return rootView;
    }

    private void registerReciver(){
        messageCenterReceiver = new MessageCenterReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wisape.android.content.MessageCenterReceiver");
        getActivity().registerReceiver(messageCenterReceiver,intentFilter);
    }


    /**
     * 设置用户信息
     */
    private void setUserInfodata() {
        tvName.setText(wisapeApplication.getUserInfo().nick_name);
        tvMail.setText(wisapeApplication.getUserInfo().user_email);
        String iconUrl = wisapeApplication.getUserInfo().user_ico_n;
        if (null != iconUrl && 0 < iconUrl.length()) {
            Picasso.with(getActivity()).load(iconUrl)
                    .resize(150, 150)
                    .transform(new CircleTransform())
                    .centerCrop()
                    .into(userHeadImage);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        messageCenterReceiver.destroy();
        getActivity().unregisterReceiver(messageCenterReceiver);

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

    @OnClick(R.id.message_center)
    @SuppressWarnings("unused")
    protected void onMessageCenterClicked() {
        MessageCenterActivity.launch(getActivity());
        clearMsgCount();
    }

    /**
     * 清除消息数量
     */
    private void clearMsgCount() {
        tvMsgAccount.setText("0");
        tvMsgAccount.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (BaseActivity.RESULT_OK == resultCode) {
            setUserInfodata();
        }
    }

    @Override
    public void updateMsgCount() {
        if (tvMsgAccount.getVisibility() == View.GONE) {
            tvMsgAccount.setVisibility(View.VISIBLE);
        }
        tvMsgAccount.setText(Integer.parseInt(tvMsgAccount.getText().toString()) + 1 + "");
    }
}
