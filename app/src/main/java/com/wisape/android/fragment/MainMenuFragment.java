package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.R;
import com.wisape.android.activity.AboutActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.content.DynamicBroadcastReceiver;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment implements DynamicBroadcastReceiver.OnDynamicBroadcastReceiverListener{
    private static final String TAG = MainMenuFragment.class.getSimpleName();

    @InjectView(R.id.sdv_user_head_image)
    SimpleDraweeView sdvUserHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;

    private UserCallback callback;
    private LocalBroadcastManager localBroadcastManager;
    private DynamicBroadcastReceiver localReceiver;

    @Override
    public void onReceiveBroadcast(Context context, Intent intent) {
        if(isDetached()){
            return;
        }

        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return;
        }

        if(UserProfileActivity.ACTION_PROFILE_UPDATED.equals(action)){
            UserInfo newUser = intent.getParcelableExtra(MainActivity.EXTRA_USER_INFO);
            refreshUI(newUser);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof UserCallback){
            callback = (UserCallback) activity;
        }

        if(null == callback){
            throw new IllegalStateException("The UserCallback can not be null.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mobihelp.init(getActivity(), new MobihelpConfig(getString(R.string.freshdesk_domain), getString(R.string.freshdesk_key), getString(R.string.freshdesk_secret)));
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localReceiver = new DynamicBroadcastReceiver(this);
        localBroadcastManager.registerReceiver(localReceiver, new IntentFilter(UserProfileActivity.ACTION_PROFILE_UPDATED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, null, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        UserInfo user = callback.getUserInfo();
        refreshUI(user);
    }

    private void refreshUI(UserInfo user){
        String icon = user.user_ico_n;
        Log.d(TAG, "#onViewCreated icon:" + icon);
        if(null != icon && 0 < icon.length()){
            FrescoFactory.bindImageFromUri(sdvUserHeadImage, icon);
        }

        tvName.setText(user.nick_name);
        tvMail.setText(user.user_email);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != localBroadcastManager){
            localBroadcastManager.unregisterReceiver(localReceiver);
            localReceiver.destroy();
            localReceiver = null;
            localBroadcastManager = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(null != callback){
            callback = null;
        }
    }

    @OnClick(R.id.help_center)
    @SuppressWarnings("unused")
    protected void onHelpCenterClick(View view){
        Mobihelp.showSupport(getActivity());
    }

    @OnClick(R.id.tv_name)
    @SuppressWarnings("unused")
    protected void onNameClicked(){
        UserProfileActivity.launch(this, callback.getUserInfo(), UserProfileActivity.REQUEST_CODE_PROFILE);
    }

    @OnClick(R.id.about)
    @SuppressWarnings("unused")
    protected void doAboutClicked(){
        AboutActivity.launch(this);
    }


    public interface UserCallback{
        UserInfo getUserInfo();
    }
}
