package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.content.StoryBroadcastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.QrDialogFragment;

import java.io.File;
import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.facebook.Facebook;
import cn.sharesdk.facebookmessenger.FacebookMessenger;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.google.GooglePlus;
import cn.sharesdk.linkedin.LinkedIn;
import cn.sharesdk.system.email.Email;
import cn.sharesdk.system.text.ShortMessage;
import cn.sharesdk.twitter.Twitter;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * story发布界面
 * Created by tony on 2015/7/24.
 */
public class StoryReleaseActivity extends BaseActivity {

    private static final String TAG = StoryReleaseActivity.class.getSimpleName();

    public static final int REQUEST_CODE_STORY_RELEASE = 110;
    private static final String EXTRA_STORYINFO = "story_info";
    private static final int LOADER_UPDATE_STORYSETTING = 1;

    private static final String EXTRAS_STORY_NAME = "stroy_name";
    private static final String EXTRAS_STORY_DESC = "story_desc";
    private static final String EXTRAS_STROY_IMG = "stroy_img";
    private static final String EXTRAS_STROY_ID = "story_id";

    private static final int WIDTH = 800;
    private static final int HEIGHT = 1200;

    private String storyUrl;
    private String storyLocalImg;


    public static void launch(Activity activity, int requestCode) {
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), StoryReleaseActivity.class);
        activity.startActivity(intent);
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), StoryReleaseActivity.class);
        return intent;
    }

    @InjectView(R.id.story_settings_name)
    protected AppCompatEditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected AppCompatEditText storyDescEdit;
    @InjectView(R.id.story_settings_cover_sdv)
    protected ImageView storyCoverView;
    private StoryEntity storyEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_release);
        ButterKnife.inject(this);
        ShareSDK.initSDK(this);
        setStoryInfo();
    }

    private void setStoryInfo() {
        storyEntity = wisapeApplication.getStoryEntity();
        storyNameEdit.setText(storyEntity.storyName);
        storyDescEdit.setText(storyEntity.storyDesc);
        String uri = storyEntity.storyThumbUri;
        if (!Utils.isEmpty(uri)) {
            Picasso.with(this).load(uri)
                    .resize(80, 80)
                    .centerCrop()
                    .into(storyCoverView);
        }
        storyLocalImg = storyEntity.storyThumbUri;
        storyUrl = HttpUrlConstancts.SHARE_URL + storyEntity.storyServerId;
    }

    @OnClick(R.id.linear_picture)
    public void onPictureClick() {
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            switch (requestCode) {
                case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                    Uri imgUri = extras.getParcelable(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    File file = new File(StoryManager.getStoryDirectory(), "/" + storyEntity.storyName + "/thumb");

                    CutActivity.launch(this, imgUri, WIDTH, HEIGHT, file.getAbsolutePath(), CutActivity.RQEUST_CODE_CROP_IMG);
                    break;
                case CutActivity.RQEUST_CODE_CROP_IMG:
                    storyLocalImg = extras.getString(CutActivity.EXTRA_IMAGE_URI);
                    if (null != storyLocalImg) {
                        Picasso.with(this).load(new File(storyLocalImg))
                                .resize(150, 150)
                                .placeholder(R.mipmap.icon_camera)
                                .error(R.mipmap.icon_about_logo)
                                .centerCrop()
                                .into(storyCoverView);
                        Bundle args = new Bundle();
                        args.putString(EXTRAS_STORY_NAME, storyNameEdit.getText().toString());
                        args.putString(EXTRAS_STORY_DESC, storyDescEdit.getText().toString());
                        args.putLong(EXTRAS_STROY_ID, storyEntity.storyServerId);
                        args.putString(EXTRAS_STROY_IMG, storyLocalImg);
                        startLoad(LOADER_UPDATE_STORYSETTING, args);
                    }
                    break;
            }
        }
    }

    private boolean isStorySettingChange() {
        String storyName = storyNameEdit.getText().toString();
        String storyDesc = storyDescEdit.getText().toString();
        if (storyName.equals(storyEntity.storyName) && storyDesc.equals(storyEntity.storyDesc)
                && Utils.isEmpty(storyLocalImg)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        Bundle args = new Bundle();
        args.putString(EXTRAS_STORY_NAME, storyNameEdit.getText().toString());
        args.putString(EXTRAS_STORY_DESC, storyDescEdit.getText().toString());
        args.putLong(EXTRAS_STROY_ID, storyEntity.storyServerId);
        args.putString(EXTRAS_STROY_IMG, storyLocalImg);
        startLoad(LOADER_UPDATE_STORYSETTING, args);
        MainActivity.launch(this);
        super.onBackPressed();
    }

    @Override
    protected boolean onBackNavigation() {
        onBackPressed();
        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = StoryLogic.instance().updateStorySetting(args.getLong(EXTRAS_STROY_ID),
                args.getString(EXTRAS_STORY_NAME), args.getString(EXTRAS_STROY_IMG),
                args.getString(EXTRAS_STORY_DESC));

        StoryInfo storyInfo = (StoryInfo) msg.obj;

        storyEntity.storyName = storyInfo.story_name;
        storyEntity.storyDesc = storyInfo.description;
        storyEntity.storyThumbUri = storyInfo.small_img;


        Intent intent = new Intent();
        intent.setAction(StoryBroadcastReciver.STORY_ACTION);
        intent.putExtra(StoryBroadcastReciver.EXTRAS_TYPE, StoryBroadcastReciverListener.UPDATE_STORY_SETTING);
        sendBroadcast(intent);
        return msg;
    }


    @OnClick(R.id.story_release_moments)
    @SuppressWarnings("unused")
    protected void doShare2Moments() {
        WechatMoments.ShareParams shareParams = new WechatMoments.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setUrl(storyUrl);
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyUrl);
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        startShare(WechatMoments.NAME, shareParams);

    }

    @OnClick(R.id.story_release_wechat)
    @SuppressWarnings("unused")
    protected void doShare2WeChat() {
        Wechat.ShareParams shareParams = new Wechat.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setUrl(storyUrl);
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyUrl);
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        startShare(Wechat.NAME, shareParams);
    }


    @OnClick(R.id.story_release_link)
    @SuppressWarnings("unused")
    protected void doShare2CopyUrl() {
        Utils.clipText(this, storyUrl);
        Toast.makeText(this, "链接已经复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.story_release_linkedin)
    @SuppressWarnings("unused")
    protected void doShare2LinkedIn() {

        LinkedIn.ShareParams shareParams = new LinkedIn.ShareParams();
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setTitleUrl(storyUrl);

        shareParams.setImageUrl(storyEntity.storyThumbUri);

        shareParams.setText(storyUrl);

        startShare(LinkedIn.NAME, shareParams);
    }

    @OnClick(R.id.story_release_fb)
    @SuppressWarnings("unused")
    protected void doShare2Facebook() {
        Facebook.ShareParams shareParams = new Facebook.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);

        shareParams.setText(storyUrl);
        startShare(Facebook.NAME, shareParams);
    }

    @OnClick(R.id.story_release_messenger)
    @SuppressWarnings("unused")
    protected void doShare2Messenger() {
        FacebookMessenger.ShareParams shareParams = new FacebookMessenger.ShareParams();
        shareParams.setAddress(wisapeApplication.getUserInfo().user_email);
        shareParams.setImageUrl(storyEntity.storyThumbUri);

        shareParams.setTitle(storyEntity.storyName);
        shareParams.setText(storyUrl);
        startShare(FacebookMessenger.NAME, shareParams);
    }

    @OnClick(R.id.story_release_google_plus)
    @SuppressWarnings("unused")
    protected void doShare2GooglePlus() {
        GooglePlus.ShareParams shareParams = new GooglePlus.ShareParams();
        shareParams.setText(storyUrl);
        shareParams.setImageUrl(storyEntity.storyThumbUri);

        startShare(GooglePlus.NAME, shareParams);
    }

    @OnClick(R.id.story_release_twitter)
    @SuppressWarnings("unused")
    protected void doShare2Twitter() {
        Twitter.ShareParams shareParams = new Twitter.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setText(storyUrl);
        startShare(Twitter.NAME, shareParams);
    }


    @OnClick(R.id.story_release_email)
    @SuppressWarnings("unused")
    protected void doShare2Email() {
        Email.ShareParams shareParams = new Email.ShareParams();
        shareParams.setAddress(wisapeApplication.getUserInfo().user_email);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setTitle(storyEntity.storyName);
        shareParams.setText(storyUrl);
        startShare(Email.NAME, shareParams);
    }

    @OnClick(R.id.story_release_sms)
    @SuppressWarnings("unused")
    protected void doShare2SMS() {

        ShortMessage.ShareParams shareParams = new ShortMessage.ShareParams();
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyUrl);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        startShare(ShortMessage.NAME, shareParams);
    }


    @OnClick(R.id.story_release_qr)
    @SuppressWarnings("unused")
    protected void doShare2QR() {
        QrDialogFragment qrDialogFragment = QrDialogFragment.instance(storyEntity.storyUri
                , (StoryManager.getStoryDirectory() + "/" + storyNameEdit.getText().toString()));
        qrDialogFragment.show(getSupportFragmentManager(), "qr");
    }

    @OnClick(R.id.story_release_more)
    @SuppressWarnings("unused")
    protected void doShare2More() {
        Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
        intent.setType("text/plain"); // 分享发送的数据类型
        String msg = storyNameEdit.getText().toString() + storyUrl;
        intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
        startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题
    }

    private void startShare(final String platName, Platform.ShareParams shareParams) {
        Platform platform = ShareSDK.getPlatform(this, platName);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Log.e(TAG, "平台名称:" + platName + "分享成功");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Log.e(TAG, "平台名称:" + platName + "分享失败:" + throwable.getMessage());

            }

            @Override
            public void onCancel(Platform platform, int i) {
                Log.e(TAG, "平台名称:" + platName + "分享取消");

            }
        });
        platform.share(shareParams);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        ShareSDK.stopSDK();
    }
}