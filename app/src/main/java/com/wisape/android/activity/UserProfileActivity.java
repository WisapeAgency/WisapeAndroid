package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.soundcloud.android.crop.Crop;
import com.wisape.android.msg.UserProfileMessage;
import com.wisape.android.R;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.api.ApiUser;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.wisape.android.activity.MainActivity.EXTRA_USER_INFO;

/**
 * 用户信息修改
 * Created by LeiGuoting on 6/7/15.
 */
public class UserProfileActivity extends BaseActivity{
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    public static final int REQUEST_CODE_PROFILE = 0x102;
    public static final String EXTRA_PROFILE_ICON_URI = "_profile_icon_uri";
    public static final String ACTION_PROFILE_UPDATED = "action_profile_updated";

    private static final int LOADER_WHAT_PROFILE_UPDATE = 0x01;

    public static void launch(Fragment fragment, int requestCode){
        fragment.startActivityForResult(getIntent(fragment.getActivity().getApplicationContext()), requestCode);
    }

    public static Intent getIntent(Context context){
        return  new Intent(context, UserProfileActivity.class);
    }

    @InjectView(R.id.user_profile_icon)
    protected SimpleDraweeView iconView;

    @InjectView(R.id.user_profile_name_edit)
    protected TextView nameEdit;

    @InjectView(R.id.user_profile_email_edit)
    protected TextView emailEdit;

    private Uri userIconUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);

        nameEdit.setText(wisapeApplication.getUserInfo().nick_name);
        emailEdit.setText(wisapeApplication.getUserInfo().user_email);
        String iconUrl = wisapeApplication.getUserInfo().user_ico_n;
        if(null != iconUrl && 0 < iconUrl.length()){
            Log.e(TAG,"firstIconUrl:" + iconUrl);
            iconView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
            FrescoFactory.bindImageFromUri(iconView, iconUrl);
        }
        EventBus.getDefault().register(this);
    }


    @OnClick(R.id.user_profile_email_edit)
    @SuppressWarnings("unused")
    protected void onEmailTextOnClicked(){
        String email = emailEdit.getText().toString();
        if("".equals(email)){
            AddEmailAccoutActivity.launch(this,AddEmailAccoutActivity.REQEUST_CODE);
        }else{
            ChangeEamilActivity.Launche(this);
        }
    }

    @OnClick(R.id.user_profile_icon)
    @SuppressWarnings("unused")
    protected void doIconClicked(){
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            default :
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case PhotoSelectorActivity.REQUEST_CODE_PHOTO :
                if(RESULT_OK == resultCode){
                    userIconUri= data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    Log.e(TAG,"iconUrl:" + userIconUri.toString());
                    iconView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
                    FrescoFactory.bindImageFromUri(iconView, "file://" + userIconUri.toString());
                }
                break;

            case Crop.REQUEST_CROP :
                if(RESULT_OK == resultCode){
                    iconView.setImageURI(userIconUri);
                }
                break;
            case AddEmailAccoutActivity.REQEUST_CODE:
                if(RESULT_OK == resultCode){
                    emailEdit.setText(data.getStringExtra(AddEmailAccoutActivity.EMAIL_ACCOUNT));
                }
        }

    }

    @Override
    protected boolean onBackNavigation() {
        doSaveProfile();
        return super.onBackNavigation();
    }

    @Override
    public void onBackPressed() {
        onBackNavigation();
    }

    private void doSaveProfile(){
        if(isDestroyed()){
            return;
        }

        String newName = nameEdit.getText().toString();
        String newEmail = emailEdit.getText().toString();

        String oldName = wisapeApplication.getUserInfo().nick_name;
        String oldEmail = wisapeApplication.getUserInfo().user_email;

        if(newName.equals(oldName) && newEmail.equals(oldEmail) && null == userIconUri){
            return;
        }

        if(0 != oldName.length() && 0 == newName.length() || 0 != oldEmail.length() && 0 == newEmail.length()){
            return;
        }

        Bundle args = new Bundle();
        ApiUser.AttrUserProfile profile = new ApiUser.AttrUserProfile();
        profile.nickName = newName;
        profile.userEmail = newEmail;
        args.putParcelable(EXTRA_USER_INFO, profile);
        args.putParcelable(EXTRA_PROFILE_ICON_URI, userIconUri);
        startLoad(LOADER_WHAT_PROFILE_UPDATE, args);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        ApiUser.AttrUserProfile profile = args .getParcelable(EXTRA_USER_INFO);
        Uri iconUri = args.getParcelable(EXTRA_PROFILE_ICON_URI);
        UserLogic.instance().updateProfile(getApplicationContext(), profile, iconUri, null);
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        EventBus.getDefault().unregister(this);
        if(null != nameEdit){
            nameEdit.setOnTouchListener(null);
            emailEdit = null;
        }

        if(null != emailEdit){
            emailEdit.setOnTouchListener(null);
            emailEdit = null;
        }
    }

    public void onEventMainThread(UserProfileMessage message){
            emailEdit.setText(message.getUserEmail());
    }
}
