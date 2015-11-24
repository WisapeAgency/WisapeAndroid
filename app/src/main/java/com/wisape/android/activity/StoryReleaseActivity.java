package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.AppCompatEditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.content.StoryBroadcastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.ComfirmDialog;
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
    private static final String WISAPE_SD_CARD_LOCATION = "/WISAPE_SD_CARD_LOCATION/";
    public static final int REQUEST_CODE_STORY_RELEASE = 110;
    public static final int REQEUST_CODE_CROP_IMG = 0x01;
    private static final int LOADER_UPDATE_STORYSETTING = 1;
    private static final int LOADER_UPDATE_INFO = 2;
    private static final int LOADER_UPLOAD_STORY = 3;
    private static final String EXTRAS_STORY_NAME = "stroy_name";
    private static final String EXTRAS_STORY_DESC = "story_desc";
    private Uri bgUri;
    private boolean isUpload = false;
    private boolean isSuccess = false;


    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), StoryReleaseActivity.class);
        activity.startActivity(intent);
    }

    @InjectView(R.id.story_settings_name)
    protected AppCompatEditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected AppCompatEditText storyDescEdit;
    @InjectView(R.id.story_settings_cover_sdv)
    protected ImageView storyCoverView;
    private StoryEntity storyEntity;
    private String thumbImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_release);
        ButterKnife.inject(this);
        ShareSDK.initSDK(this);
        updateStoryInfo();
        setStoryInfo();
    }

    private void updateStoryInfo() {
        if (ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE.equals(StoryLogic.instance().getStoryEntityFromShare().status)) {
            isUpload = true;
            isSuccess = true;
        } else {
            startLoad(LOADER_UPLOAD_STORY, null);
        }
    }

    private void setStoryInfo() {
        storyEntity = StoryLogic.instance().getStoryEntityFromShare();
        storyNameEdit.setText(storyEntity.storyName);
        storyDescEdit.setText(storyEntity.storyDesc);
        if (storyEntity.localCover == 0) {
            File storyDirectory = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal);
            File coverFile = new File(storyDirectory, "thumb.jpg");
            if (coverFile.exists()) {
                thumbImage = coverFile.getAbsolutePath();
            } else {
                thumbImage = storyEntity.storyThumbUri;
            }
        } else {
            thumbImage = storyEntity.storyThumbUri;
        }
        Utils.loadImg(this, thumbImage, storyCoverView);
        LogUtil.d("storylocalCover:" + storyEntity.localCover + "封面地址:" + storyEntity.storyThumbUri + " :story地址:" + storyEntity.storyUri);
    }

    @OnClick(R.id.linear_picture)
    @SuppressWarnings("unused")
    public void onPictureClick() {
        if (!isUpload) {
            if(!isSuccess){
                showToast("upload story failure");
            }else{
                showProgressDialog(R.string.progress_loading_data);
            }
            return;
        }
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            switch (requestCode) {
                case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                    Uri imgUri = extras.getParcelable(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    File file = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal + "/thumb.jpg");
                    if (file.exists()) {
                        file.delete();
                    }
                    bgUri = Uri.fromFile(file);
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imgUri, "image/*");
                    //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
                    intent.putExtra("crop", "true");
                    // aspectX aspectY 是宽高的比例
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1.5);
                    intent.putExtra("scale", false);

                    // outputX outputY 是裁剪图片宽高
                    intent.putExtra("outputX", 600);
                    intent.putExtra("outputY", 800);
                    intent.putExtra("return-data", false);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, bgUri);
                    intent.putExtra("noFaceDetection", true); // no face detection
                    startActivityForResult(intent, REQEUST_CODE_CROP_IMG);
                    break;
                case REQEUST_CODE_CROP_IMG:
                    Bundle args = new Bundle();
                    args.putString(EXTRAS_STORY_NAME, storyNameEdit.getText().toString());
                    args.putString(EXTRAS_STORY_DESC, storyDescEdit.getText().toString());
                    storyEntity.localCover = 1;
                    startLoadWithProgress(LOADER_UPDATE_STORYSETTING, args);
                    break;
            }
        }
    }

    private boolean isStorySettingChange() {
        String storyName = storyNameEdit.getText().toString();
        String storyDesc = storyDescEdit.getText().toString();
        return (storyName.equals(storyEntity.storyName) && storyDesc.equals(storyEntity.storyDesc));
    }

    @Override
    public void onBackPressed() {

        if(!isUpload){
            LogUtil.d("产生草稿story");
            StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
            story.status = ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
            StoryEntity storyEntity = StoryLogic.instance().updateStory(this, story);
            StoryLogic.instance().saveStoryEntityToShare(storyEntity);
            Intent intent = new Intent();
            intent.setAction(StoryBroadcastReciver.STORY_ACTION);
            intent.putExtra(StoryBroadcastReciver.EXTRAS_TYPE, StoryBroadcastReciverListener.TYPE_ADD_STORY);
            WisapeApplication.getInstance().getApplicationContext().sendBroadcast(intent);
        }

        if (!isStorySettingChange()) {
            Bundle args = new Bundle();
            args.putString(EXTRAS_STORY_NAME, storyNameEdit.getText().toString());
            args.putString(EXTRAS_STORY_DESC, storyDescEdit.getText().toString());
            startLoadWithProgress(LOADER_UPDATE_INFO, args);
            return;
        }
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
        Message message = Message.obtain();

        switch (what) {
            case LOADER_UPDATE_INFO:
                message = StoryLogic.instance().updateStorySetting(storyEntity,
                        args.getString(EXTRAS_STORY_NAME), "",
                        args.getString(EXTRAS_STORY_DESC));
                break;
            case LOADER_UPDATE_STORYSETTING:
                FileUtils.saveBitmap(bgUri.getPath(), FileUtils.getSmallBitmap(bgUri.getPath()));
                storyEntity.localCover = 1;
                message = StoryLogic.instance().updateStorySetting(storyEntity,
                        args.getString(EXTRAS_STORY_NAME), bgUri.getPath(),
                        args.getString(EXTRAS_STORY_DESC));
                break;
            case LOADER_UPLOAD_STORY:
                StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
                ApiStory.AttrStoryInfo storyAttr = new ApiStory.AttrStoryInfo();
                storyAttr.story = Uri.fromFile(new File(StoryManager.getStoryDirectory(), story.storyLocal));
                storyAttr.storyName = story.storyName;
                if (Utils.isEmpty(story.storyMusicName)) {
                    storyAttr.bgMusic = "";
                } else {
                    storyAttr.bgMusic = story.storyMusicName;
                }
                storyAttr.storyDescription = story.storyDesc;
                storyAttr.userId = UserLogic.instance().getUserInfoFromLocal().user_id;
                storyAttr.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
//                storyAttr.imgPrefix = StoryManager.getStoryDirectory().getAbsolutePath() + "/" + story.storyLocal;
                storyAttr.imgPrefix = WISAPE_SD_CARD_LOCATION + "/story/" + story.storyLocal;
                storyAttr.story_local = story.storyLocal;
                if (story.localCover == 0) {
                    storyAttr.attrStoryThumb = Uri.parse(StoryManager.getStoryDirectory().getAbsolutePath() + "/" + story.storyLocal + "/thumb.jpg");
                }

                if ("-1".equals(story.status)) {
                    storyAttr.sid = -1;
                } else {
                    storyAttr.sid = story.storyServerId;
                }

                boolean upload = StoryLogic.instance().update(WisapeApplication.getInstance().getApplicationContext(), storyAttr, "release");
                if (upload) {
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                    message.what = LOADER_UPLOAD_STORY;
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                    message.what = LOADER_UPLOAD_STORY;
                }

                break;
        }

        return message;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        closeProgressDialog();
        switch (data.what) {
            case LOADER_UPDATE_STORYSETTING:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    setStoryInfo();
                } else {
                    showToast((String) data.obj);
                }

                break;
            case LOADER_UPDATE_INFO:
                if (HttpUrlConstancts.STATUS_SUCCESS != data.arg1) {
                    showToast((String) data.obj);
                }
                MainActivity.launch(this);
                break;
            case LOADER_UPLOAD_STORY:
                if(HttpUrlConstancts.STATUS_SUCCESS == data.arg1){
                    setStoryInfo();
                    isUpload = true;
                    isSuccess = true;
                }else{
                    LogUtil.d("上传story失败");
                    isUpload = false;
                    isSuccess = false;
                    ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.upload_failure_title), getString(R.string.upload_failure));
                    comfirmDialog.show(getSupportFragmentManager(), "reply");
                    comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
                        @Override
                        public void onConfirmClicked() {
                            startLoadWithProgress(LOADER_UPLOAD_STORY, null);
                        }
                    });

                }
                break;
        }
    }

    @OnClick(R.id.story_release_moments)
    @SuppressWarnings("unused")
    protected void doShare2Moments() {
        WechatMoments.ShareParams shareParams = new WechatMoments.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setUrl(storyEntity.storyUri);
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyDescEdit.getText().toString());
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        startShare(WechatMoments.NAME, shareParams);

    }

    @OnClick(R.id.story_release_wechat)
    @SuppressWarnings("unused")
    protected void doShare2WeChat() {
        Wechat.ShareParams shareParams = new Wechat.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setUrl(storyEntity.storyUri);
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyDescEdit.getText().toString());
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        startShare(Wechat.NAME, shareParams);
    }


    @OnClick(R.id.story_release_link)
    @SuppressWarnings("unused")
    protected void doShare2CopyUrl() {
        if (!isUpload) {
            showProgressDialog(R.string.progress_loading_data);
            return;
        }
        Utils.clipText(this, storyEntity.storyUri);
        Toast.makeText(this, "copy url", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.story_release_linkedin)
    @SuppressWarnings("unused")
    protected void doShare2LinkedIn() {
        LinkedIn.ShareParams shareParams = new LinkedIn.ShareParams();
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setTitleUrl(storyEntity.storyUri);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setText(storyEntity.storyUri);
        startShare(LinkedIn.NAME, shareParams);
    }

    @OnClick(R.id.story_release_fb)
    @SuppressWarnings("unused")
    protected void doShare2Facebook() {

//        ShareLinkContent content = new ShareLinkContent.Builder().setContentDescription(storyDescEdit.getText().toString())
//                .setContentTitle(storyNameEdit.getText().toString())
//                .setImageUrl(Uri.parse(storyEntity.storyThumbUri))
//                .setContentUrl(Uri.parse(storyEntity.storyUri))
//                .build();
//
//       AccessToken accessToken =  AccessToken.getCurrentAccessToken();
//        if(null == accessToken){
//        }
//        ShareApi.share(content, new FacebookCallback<Sharer.Result>() {
//            @Override
//            public void onSuccess(Sharer.Result result) {
//                LogUtil.d("facebook分享成功:");
//            }
//
//            @Override
//            public void onCancel() {
//                LogUtil.d("facebook分享取消:");
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                LogUtil.e("facebook分享失败:",error.getCause());
//
//            }
//        });

        String html = "<a href=http://ww.baidu.com>" + "<img src=" + storyEntity.storyUri+"/></a>";
        Facebook.ShareParams shareParams = new Facebook.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setUrl(storyEntity.storyUri);
        shareParams.setTitle(storyEntity.storyUri);
        shareParams.setText(storyEntity.storyUri);
        startShare(Facebook.NAME, shareParams);
    }

    @OnClick(R.id.story_release_messenger)
    @SuppressWarnings("unused")
    protected void doShare2Messenger() {
        FacebookMessenger.ShareParams shareParams = new FacebookMessenger.ShareParams();
        shareParams.setAddress(UserLogic.instance().getUserInfoFromLocal().user_email);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setTitle(storyEntity.storyName);
        shareParams.setText(storyEntity.storyUri);
        startShare(FacebookMessenger.NAME, shareParams);
    }

    @OnClick(R.id.story_release_google_plus)
    @SuppressWarnings("unused")
    protected void doShare2GooglePlus() {
        GooglePlus.ShareParams shareParams = new GooglePlus.ShareParams();
        shareParams.setText(storyEntity.storyUri);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        startShare(GooglePlus.NAME, shareParams);
    }

    @OnClick(R.id.story_release_twitter)
    @SuppressWarnings("unused")
    protected void doShare2Twitter() {
        String html = storyEntity.storyUri.replace("http://","");
        Twitter.ShareParams shareParams = new Twitter.ShareParams();
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setText(html);
        startShare(Twitter.NAME, shareParams);
    }


    @OnClick(R.id.story_release_email)
    @SuppressWarnings("unused")
    protected void doShare2Email() {
        Email.ShareParams shareParams = new Email.ShareParams();
        shareParams.setAddress(UserLogic.instance().getUserInfoFromLocal().user_email);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        shareParams.setTitle(storyEntity.storyName);
        shareParams.setText(storyEntity.storyUri);
        startShare(Email.NAME, shareParams);
    }

    @OnClick(R.id.story_release_sms)
    @SuppressWarnings("unused")
    protected void doShare2SMS() {
        ShortMessage.ShareParams shareParams = new ShortMessage.ShareParams();
        shareParams.setTitle(storyNameEdit.getText().toString());
        shareParams.setText(storyEntity.storyUri);
        shareParams.setImageUrl(storyEntity.storyThumbUri);
        startShare(ShortMessage.NAME, shareParams);
    }


    @OnClick(R.id.story_release_qr)
    @SuppressWarnings("unused")
    protected void doShare2QR() {
        if (!isUpload) {
            showProgressDialog(R.string.progress_loading_data);
            return;
        }
        QrDialogFragment qrDialogFragment = QrDialogFragment.instance(storyEntity.storyUri
                , (StoryManager.getStoryDirectory() + "/" + storyEntity.storyLocal));
        qrDialogFragment.show(getSupportFragmentManager(), "qr");
    }

    @OnClick(R.id.story_release_more)
    @SuppressWarnings("unused")
    protected void doShare2More() {
        if (!isUpload) {
            showProgressDialog(R.string.progress_loading_data);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
        intent.setType("text/plain"); // 分享发送的数据类型
        String msg = storyEntity.storyUri;
        intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
        startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题
    }

    private void startShare(final String platName, Platform.ShareParams shareParams) {
        if (!isUpload) {
            showProgressDialog(R.string.progress_loading_data);
            return;
        }

        Platform platform = ShareSDK.getPlatform(this, platName);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                LogUtil.d(platName + "分享成功");
                Message message = Message.obtain();
                message.obj = platName + " publish success";
                handler.sendMessage(message);
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                LogUtil.e(platName + "分享成功失败", throwable);
                Message message = Message.obtain();
                message.obj = platName + " publish failure,not client";
                handler.sendMessage(message);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                Message message = Message.obtain();
                message.obj = platName + " publish cancle";
                handler.sendMessage(message);
            }
        });
        platform.share(shareParams);
    }

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            showToast((String) msg.obj);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        ShareSDK.stopSDK();
    }
}