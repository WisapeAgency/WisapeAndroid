package com.wisape.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.content.UpdateUserInfoBroadcastReciver;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.view.CircleTransform;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 用户信息修改
 * Created by LeiGuoting on 6/7/15.
 */
public class UserProfileActivity extends BaseActivity {

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    public static final int REQUEST_CODE_PROFILE = 0x102;
    public static final int REQEUST_CODE_CROP_IMG = 0x01;

    private static final int LOADER_WHAT_PROFILE_UPDATE = 1;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 200;

    public static final String EXTRAS_EMAIL = "email";
    public static final String EXTRAS_NAME = "nickName";
    public static final String EXTRAS_ICON_URI = "icon_uri";

    public static void launch(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), UserProfileActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }


    @InjectView(R.id.user_profile_icon)
    protected ImageView iconView;

    @InjectView(R.id.user_profile_name_edit)
    protected android.widget.EditText nameEdit;

    @InjectView(R.id.user_profile_email_edit)
    protected TextView emailEdit;

    private Uri userIconUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);
        UserInfo userInfo = UserLogic.instance().getUserInfoFromLocal();
        if(null != userInfo){
            nameEdit.setText(userInfo.nick_name);
            emailEdit.setText(userInfo.user_email);
            String iconUrl = userInfo.user_ico_n;
            if (null != iconUrl && 0 < iconUrl.length()) {
                Picasso.with(this)
                        .load(iconUrl)
                        .transform(new CircleTransform())
                        .fit()
                        .centerCrop()
                        .into(iconView);
            }
        }
    }


    @OnClick(R.id.linear_email)
    @SuppressWarnings("unused")
    protected void onEmailTextOnClicked() {
        String email = emailEdit.getText().toString();
        if ("".equals(email)) {
            AddEmailAccoutActivity.launch(this, AddEmailAccoutActivity.REQEUST_CODE_ADD_EMAIL);
        } else {
            ChangeEamilActivity.Launch(this, ChangeEamilActivity.REQUEST_CODE_CHANGE_EMAIL);
        }
    }

    @OnClick(R.id.linear_photo)
    @SuppressWarnings("unused")
    protected void doIconClicked() {
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }


    @Override
    protected boolean onBackNavigation() {
        doSaveProfile();
        return true;
    }

    private void doSaveProfile() {
        if (isDestroyed()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        String newName = nameEdit.getText().toString();
        String newEmail = emailEdit.getText().toString();

        String oldName = UserLogic.instance().getUserInfoFromLocal().nick_name;
        String oldEmail = UserLogic.instance().getUserInfoFromLocal().user_email;

        if (newName.equals(oldName) && newEmail.equals(oldEmail) && null == userIconUri) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (0 != oldName.length() && 0 == newName.length() || 0 != oldEmail.length() && 0 == newEmail.length()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Log.e(TAG, "更新用户信息!");
        Bundle args = new Bundle();
        args.putString(EXTRAS_NAME, newName);
        args.putString(EXTRAS_EMAIL, newEmail);
        if(null == userIconUri){
            args.putString(EXTRAS_ICON_URI,"");
        }else{
            args.putString(EXTRAS_ICON_URI,userIconUri.getPath());
        }
        startLoadWithProgress(LOADER_WHAT_PROFILE_UPDATE, args);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = UserLogic.instance().updateProfile(args.getString(EXTRAS_NAME),
                args.getString(EXTRAS_ICON_URI),
                args.getString(EXTRAS_EMAIL), UserLogic.instance().getUserInfoFromLocal().access_token);
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if (STATUS_SUCCESS == data.arg1) {
            UserInfo userInfo = (UserInfo) data.obj;
            UserLogic.instance().saveUserToSharePrefrence(userInfo);
            setResult(RESULT_OK);
            Intent intent = new Intent();
            intent.setAction(UpdateUserInfoBroadcastReciver.ACTION);
            sendBroadcast(intent);
        } else {
            showToast((String)data.obj);
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if (null != nameEdit) {
            nameEdit.setOnTouchListener(null);
            emailEdit = null;
        }

        if (null != emailEdit) {
            emailEdit.setOnTouchListener(null);
            emailEdit = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data == null){
            return;
        }
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            switch (requestCode) {
                case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                    Uri imgUri = extras.getParcelable(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    File file = new File(EnvironmentUtils.getAppCacheDirectory(), "head");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    File head = new File(file, Utils.acquireUTCTimestamp()+".jpg");
                    if(head.exists()){
                        head.delete();
                    }
                    userIconUri = Uri.fromFile(head);

                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imgUri, "image/*");
                    //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
                    intent.putExtra("crop", "true");
                    // aspectX aspectY 是宽高的比例
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    intent.putExtra("scale", true);

                    // outputX outputY 是裁剪图片宽高
                    intent.putExtra("outputX", WIDTH);
                    intent.putExtra("outputY", HEIGHT);
                    intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, userIconUri);
                    intent.putExtra("noFaceDetection", true); // no face detection
                    startActivityForResult(intent, REQEUST_CODE_CROP_IMG);

                    break;
                case REQEUST_CODE_CROP_IMG:
                    if(null != userIconUri){
                        Utils.loadImg(this,userIconUri.getPath(),iconView);
                        Picasso.with(this).load(new File(userIconUri.getPath()))
                                .transform(new CircleTransform())
                                .fit()
                                .centerCrop()
                                .into(iconView);
                    }
                    break;
                case AddEmailAccoutActivity.REQEUST_CODE_ADD_EMAIL:
                    emailEdit.setText(extras.getString(AddEmailAccoutActivity.EXTRA_EMAIL_ACCOUNT));
                    break;
                case ChangeEamilActivity.REQUEST_CODE_CHANGE_EMAIL:
                    emailEdit.setText(extras.getString(ChangeEamilActivity.EXTRA_EMAIL_ACCOUNT));
                    break;
            }
        }
    }
}
