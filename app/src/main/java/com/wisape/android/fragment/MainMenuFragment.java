package com.wisape.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.R;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment {


    @InjectView(R.id.sdv_user_head_image)
    SimpleDraweeView sdvUserHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, null, false);
        ButterKnife.inject(this, rootView);
        init();
        return rootView;
    }

    private void init() {
        FrescoFactory.bindImageFromUri(sdvUserHeadImage, "http://static.6yoo.com/yuyan/cms/d/qinsmoon/market/4ad/2015-06-03/b84729802bdc351165bda6f545ce9b93.jpg");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


    @OnClick(R.id.help_center)
    @SuppressWarnings("unused")
    protected void onHelpCenterClick(View view){
        String domain = getString(R.string.mobihelp_config_domain),
                appId = getString(R.string.mobihelp_config_appId),
                appSecret = getString(R.string.mobihelp_config_appSecret);
        Mobihelp.init(getActivity(), new MobihelpConfig(domain,appId,appSecret));
        //Mobihelp.clearUserData(getActivity());
        Mobihelp.showSupport(getActivity());
    }

    @OnClick(R.id.tv_name)
    @SuppressWarnings("unused")
    protected void onNameClicked(){
        UserProfileActivity.launch(getActivity(), 0);
    }
}
