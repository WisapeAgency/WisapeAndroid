package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.drawee.view.SimpleDraweeView;
import com.soundcloud.android.crop.Crop;
import com.widgets.EditableTextView;
import com.wisape.android.R;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUser;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.wisape.android.activity.MainActivity.EXTRA_USER_INFO;

/**
 * Created by LeiGuoting on 6/7/15.
 */
public class UserProfileActivity extends BaseActivity{
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    public static final int REQUEST_CODE_PROFILE = 0x102;
    public static final String EXTRA_PROFILE_ICON_URI = "_profile_icon_uri";
    public static final String ACTION_PROFILE_UPDATED = "action_profile_updated";

    private static final int LOADER_WHAT_PROFILE_UPDATE = 0x01;

    public static void launch(Fragment fragment, UserInfo user, int requestCode){
        fragment.startActivityForResult(getIntent(fragment.getActivity().getApplicationContext(), user), requestCode);
    }

    public static Intent getIntent(Context context, UserInfo user){
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(EXTRA_USER_INFO, user);
        return intent;
    }

    private UserInfo user;

    @InjectView(R.id.user_profile_icon)
    protected SimpleDraweeView iconView;

    @InjectView(R.id.user_profile_name_edit)
    protected EditableTextView nameEdit;

    @InjectView(R.id.user_profile_email_edit)
    protected EditableTextView emailEdit;

    private Uri userIconUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args;
        if(null == savedInstanceState){
            args = getIntent().getExtras();
        }else{
            args = savedInstanceState;
        }
        user = args.getParcelable(EXTRA_USER_INFO);

        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);

        nameEdit.setText(user.nick_name);
        emailEdit.setText(user.user_email);
        String iconUrl = user.user_ico_normal;
        if(null != iconUrl && 0 < iconUrl.length()){
            iconView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
            FrescoFactory.bindImageFromUri(iconView, iconUrl);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER_INFO, user);
    }

    @OnClick(R.id.user_profile_name_edit)
    @SuppressWarnings("unused")
    protected void doNameEditClicked(){
        Log.d(TAG, "#doNameEditClicked ___");
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
                    userIconUri = PhotoSelectorActivity.buildCropUri(this, 0);
                    Uri imageUri = data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), userIconUri).asSquare().start(this);
                }
                break;

            case Crop.REQUEST_CROP :
                if(RESULT_OK == resultCode){
                    iconView.setImageURI(userIconUri);
                }
                break;
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
        if(null == newName){
            newName = "";
        }
        if(null == newEmail){
            newEmail = "";
        }

        String oldName = user.nick_name;
        String oldEmail = user.user_email;
        if(null == oldName){
            oldName = "";
        }
        if(null == oldEmail){
            oldEmail = "";
        }

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
        if(null != nameEdit){
            nameEdit.setOnTouchListener(null);
            emailEdit = null;
        }

        if(null != emailEdit){
            emailEdit.setOnTouchListener(null);
            emailEdit = null;
        }
        user = null;
    }
}
