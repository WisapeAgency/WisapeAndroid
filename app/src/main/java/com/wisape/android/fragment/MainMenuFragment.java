package com.wisape.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.R;
import com.wisape.android.activity.TestActivity;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment {
    private static final String TAG = MainMenuFragment.class.getSimpleName();

    @InjectView(R.id.sdv_user_head_image)
    SimpleDraweeView sdvUserHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;

    private UserCallback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof UserCallback){
            callback = (UserCallback) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mobihelp.init(getActivity(), new MobihelpConfig(getString(R.string.freshdesk_domain), getString(R.string.freshdesk_key), getString(R.string.freshdesk_secret)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, null, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if(null != callback){
            UserInfo user = callback.getUserInfo();
            String icon = user.user_ico_normal;
            if(null != icon && 0 < icon.length()){
                FrescoFactory.bindImageFromUri(sdvUserHeadImage, icon);
            }

            tvName.setText(user.nick_name);
            tvMail.setText(user.user_email);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        TestActivity.launch(getActivity(), 0);
    }


    public interface UserCallback{
        UserInfo getUserInfo();
    }
}
