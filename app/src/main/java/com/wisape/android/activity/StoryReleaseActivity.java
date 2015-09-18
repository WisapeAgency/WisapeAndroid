package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.view.CircleTransform;
import com.wisape.android.widget.QrDialogFragment;

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
public class StoryReleaseActivity extends BaseActivity{

    private static final String TAG = StoryReleaseActivity.class.getSimpleName();

    public static final int REQUEST_CODE_STORY_RELEASE = 110;
    private static final String EXTRA_STORYINFO = "story_info";

//    private static final int WHAT_LOAD_STORY_SETTINGS = 0x01;
//    private static final int WHAT_RELEASE_STORY = 0x02;

//    public static final String EXTRA_STORY_CHANNEL = "extra_story_channel";
//
//    public static final int CHANNEL_FACEBOOK = 0x01;
//    public static final int CHANNEL_MESSENGER = 0x02;
//    public static final int CHANNEL_TWITTER = 0x03;
//    public static final int CHANNEL_LINKEDIN = 0x04;
//    public static final int CHANNEL_WECHAT = 0x05;
//    public static final int CHANNEL_MOMENTS = 0x06;
//    public static final int CHANNEL_GOOGLE_PLUS = 0x07;
//    public static final int CHANNEL_COPY_URL = 0x08;
//    public static final int CHANNEL_QR_CODE = 0x09;
//    public static final int CHANNEL_EMAIL = 0x0a;
//    public static final int CHANNEL_SMS = 0x0b;
//    public static final int CHANNEL_MORE = 0x0c;
//
//    public static final String PACKAGE_FACEBOOK = "";

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    public static void launch(Activity activity,StoryInfo storyInfo){
        Intent intent = new Intent(activity.getApplicationContext(),StoryReleaseActivity.class);
        intent.putExtra(EXTRA_STORYINFO,storyInfo);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


    public static Intent getIntent(Context context){
        Intent intent = new Intent(context.getApplicationContext(), StoryReleaseActivity.class);
        return intent;
    }

    @InjectView(R.id.story_settings_name)
    protected AppCompatEditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected AppCompatEditText storyDescEdit;
    @InjectView(R.id.story_settings_cover_sdv)
    protected ImageView storyCoverView;
    private StoryInfo storyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_release);
        ButterKnife.inject(this);
        ShareSDK.initSDK(this);
        setStoryInfo();
//        startLoad(WHAT_LOAD_STORY_SETTINGS, null);
    }

    private void setStoryInfo(){
        this.storyInfo = getIntent().getParcelableExtra(EXTRA_STORYINFO);
        storyNameEdit.setText(storyInfo.story_name);
        storyDescEdit.setText(storyInfo.description);
        Uri cover = Uri.parse(storyInfo.small_img);
        if(null != cover){
            Picasso.with(this).load(cover)
                    .resize(80, 80)
                    .centerCrop()
                    .into(storyCoverView);
        }
    }
//
//    @Override
//    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
//        Message msg;
//        switch (what){
//            default :
//                msg = null;
//                break;
//
//            case WHAT_LOAD_STORY_SETTINGS :
//                msg = Message.obtain();
//                StorySettingsInfo settings = StoryManager.acquireStorySettings(getApplicationContext());
//                msg.obj = settings;
//                msg.arg1 = STATUS_SUCCESS;
//                break;
//
//            case WHAT_RELEASE_STORY :
//                msg = Message.obtain();
//                msg.what = WHAT_RELEASE_STORY;
//                msg.setData(args);
//                break;
//        }
//        return msg;
//    }
//
//    @Override
//    protected void onLoadCompleted(Message data) {
//        switch (data.what){
//            default :
//                return;
//
//            case WHAT_LOAD_STORY_SETTINGS :
//                if(STATUS_SUCCESS == data.arg1){
//                    StorySettingsInfo settings = (StorySettingsInfo)data.obj;
//                    storyNameEdit.setText(settings.defaultName);
//                    storyDescEdit.setText(settings.defaultDesc);
//                    Uri cover = Uri.parse(settings.defaultCover);
//                    if(null != cover){
//                        Picasso.with(this).load(cover)
//                                .resize(80, 80)
//                                .transform(new CircleTransform())
//                                .centerCrop()
//                                .into(storyCoverView);
//                    }
//                }
//                break;
////
////            case WHAT_RELEASE_STORY :
////                Bundle bundle = data.getData();
////                int channel = bundle.getInt(EXTRA_STORY_CHANNEL,0);
////                share2Platform(channel);
////                break;
//        }
//    }
//
//    private void releaseStory(int channel){
//        Bundle args = new Bundle();
//        args.putInt(EXTRA_STORY_CHANNEL, channel);
//        startLoad(WHAT_RELEASE_STORY, args);
//    }

    @OnClick(R.id.story_release_moments)
    @SuppressWarnings("unused")
    protected void doShare2Moments() {
        WechatMoments.ShareParams shareParams = new WechatMoments.ShareParams();

        shareParams.setImageUrl(storyInfo.small_img);
        shareParams.setUrl(storyInfo.story_url);
        shareParams.setShareType(Platform.SHARE_WEBPAGE);

        startShare(WechatMoments.NAME,shareParams);

    }

    @OnClick(R.id.story_release_wechat)
    @SuppressWarnings("unused")
    protected void doShare2WeChat() {
        Wechat.ShareParams shareParams = new Wechat.ShareParams();
        shareParams.setShareType(Platform.SHARE_WEBPAGE);

        shareParams.setUrl(storyInfo.story_url);
        shareParams.setText(storyInfo.story_name);
        shareParams.setImageUrl(storyInfo.small_img);

        startShare(Wechat.NAME, shareParams);
    }


    @OnClick(R.id.story_release_link)
    @SuppressWarnings("unused")
    protected void doShare2CopyUrl() {
        Utils.clipText(this, storyInfo.story_url);
        Toast.makeText(this, "链接已经复制到剪贴板", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.story_release_linkedin)
    @SuppressWarnings("unused")
    protected void doShare2LinkedIn() {

        LinkedIn.ShareParams shareParams = new LinkedIn.ShareParams();
        shareParams.setTitle(storyInfo.story_name);
        shareParams.setTitleUrl(storyInfo.story_url);
        shareParams.setImageUrl(storyInfo.small_img);
        shareParams.setText(storyInfo.story_name);

        startShare(LinkedIn.NAME, shareParams);
    }

    @OnClick(R.id.story_release_fb)
    @SuppressWarnings("unused")
    protected void doShare2Facebook() {
        Facebook.ShareParams shareParams = new Facebook.ShareParams();
        shareParams.setText(storyInfo.story_name);
        shareParams.setImageUrl(storyInfo.small_img);
        startShare(Facebook.NAME, shareParams);
    }

    @OnClick(R.id.story_release_messenger)
    @SuppressWarnings("unused")
    protected void doShare2Messenger() {
        FacebookMessenger.ShareParams shareParams = new FacebookMessenger.ShareParams();
        //TODO  没有电话号码
        shareParams.setAddress(wisapeApplication.getUserInfo().user_email);
        startShare(FacebookMessenger.NAME, shareParams);
    }

    @OnClick(R.id.story_release_google_plus)
    @SuppressWarnings("unused")
    protected void doShare2GooglePlus() {
        GooglePlus.ShareParams shareParams = new GooglePlus.ShareParams();
        shareParams.setText(storyInfo.story_name);
        shareParams.setImageUrl(storyInfo.small_img);
        startShare(GooglePlus.NAME, shareParams);
    }

    @OnClick(R.id.story_release_twitter)
    @SuppressWarnings("unused")
    protected void doShare2Twitter() {
        Twitter.ShareParams shareParams = new Twitter.ShareParams();
        shareParams.setText(storyInfo.story_name);
        shareParams.setImageUrl(storyInfo.small_img);
        startShare(Twitter.NAME, shareParams);
    }


    @OnClick(R.id.story_release_email)
    @SuppressWarnings("unused")
    protected void doShare2Email() {
        Email.ShareParams shareParams = new Email.ShareParams();
        shareParams.setAddress(wisapeApplication.getUserInfo().user_email);
        shareParams.setTitle(storyInfo.story_name);
        shareParams.setText(storyInfo.story_name);
        shareParams.setImageUrl(storyInfo.small_img);
        startShare(Email.NAME, shareParams);
    }

    @OnClick(R.id.story_release_sms)
    @SuppressWarnings("unused")
    protected void doShare2SMS() {

        ShortMessage.ShareParams shareParams = new ShortMessage.ShareParams();
        shareParams.setAddress("1977979333@qq.com");
        shareParams.setTitle("邮件分享title");
        shareParams.setText("邮件分享内容");
        shareParams.setImageUrl("https://www.baidu.com/img/bdlogo.png");

        startShare(ShortMessage.NAME, shareParams);
    }


    @OnClick(R.id.story_release_qr)
    @SuppressWarnings("unused")
    protected void doShare2QR() {
        QrDialogFragment qrDialogFragment = QrDialogFragment.instance(storyInfo.story_url
                , (StoryManager.getStoryDirectory()+ "/" + storyInfo.story_name));
        qrDialogFragment.show(getSupportFragmentManager(), "qr");
    }

    @OnClick(R.id.story_release_more)
    @SuppressWarnings("unused")
    protected void doShare2More() {
        Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
        intent.setType("text/plain"); // 分享发送的数据类型
        String msg = storyInfo.story_name;
        intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
        startActivity(Intent.createChooser(intent, "选择分享"));// 目标应用选择对话框的标题
    }

    private void startShare(final String platName,Platform.ShareParams shareParams){
        Platform platform = ShareSDK.getPlatform(this, platName);
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Log.e(TAG, "平台名称:" + platName + "分享成功");
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                Log.e(TAG,"平台名称:" + platName + "分享失败:" + throwable.getMessage());

            }

            @Override
            public void onCancel(Platform platform, int i) {
                Log.e(TAG,"平台名称:" + platName + "分享取消");

            }
        });
        platform.share(shareParams);
    }

//    private void share2Platform(int channel){
//        File template = new File(StoryManager.getStoryTemplateDirectory(),"mingpian01");
//        File thumb = new File(template,"thumb.jpg");
//        String msg = "推荐给大家，http://www.wisape.com/demo/playstory/index.html";
//        switch (channel){
//            case CHANNEL_MORE:{
//                shareMessage("标题", "消息标题", msg, thumb);
//                break;
//            }
//            default:{
//                shareMessage("com.sina.weibo","标题", "消息标题", msg, thumb);
//                break;
//            }
//        }
//    }
//
//    public void shareMessage(String activityTitle, String msgTitle, String msgText, File image) {
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        if (null == image) {
//            intent.setType("text/plain"); // 纯文本
//        } else {
//            if (image.exists() && image.isFile()) {
//                intent.setType("image/jpg");
//                Uri uri = Uri.fromFile(image);
//                intent.putExtra(Intent.EXTRA_STREAM, uri);
//            }
//        }
//        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
//        intent.putExtra(Intent.EXTRA_TEXT, msgText);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(Intent.createChooser(intent, activityTitle));
//    }


//    private void shareMessage(String type,String activityTitle, String msgTitle, String msgText,
//                              File imgPath) {
//        boolean found = false;
//        Intent share = new Intent(android.content.Intent.ACTION_SEND);
//        share.setType("image/jpeg");
//        // gets the list of intents that can be loaded.
//        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
//        if (!resInfo.isEmpty()){
//            for (ResolveInfo info : resInfo) {
//                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
//                        info.activityInfo.name.toLowerCase().contains(type) ) {
//                    share.putExtra(Intent.EXTRA_SUBJECT,  msgTitle);
//                    share.putExtra(Intent.EXTRA_TEXT,     msgText);
//                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgPath) ); // Optional, just if you wanna share an image.
//                    share.setPackage(info.activityInfo.packageName);
//                    found = true;
//                    break;
//                }
//            }
//            if (!found)
//                return;
//            startActivity(Intent.createChooser(share, "Select"));
//        }
//    }
//
//    @OnClick(R.id.story_release_fb)
//    @SuppressWarnings("unused")
//    protected void doShare2Facebook(){
//        share2Platform(CHANNEL_FACEBOOK);
//    }
//
//    @OnClick(R.id.story_release_messenger)
//    @SuppressWarnings("unused")
//    protected void doShare2Messenger(){
//        share2Platform(CHANNEL_MESSENGER);
//    }
//
//    @OnClick(R.id.story_release_twitter)
//    @SuppressWarnings("unused")
//    protected void doShare2Twitter(){
//        share2Platform(CHANNEL_TWITTER);
//    }
//
//    @OnClick(R.id.story_release_linkedin)
//    @SuppressWarnings("unused")
//    protected void doShare2LinkedIn(){
//        share2Platform(CHANNEL_LINKEDIN);
//    }
//
//    @OnClick(R.id.story_release_wechat)
//    @SuppressWarnings("unused")
//    protected void doShare2WeChat(){
//        share2Platform(CHANNEL_WECHAT);
//    }
//
//    @OnClick(R.id.story_release_moments)
//    @SuppressWarnings("unused")
//    protected void doShare2Moments(){
//        share2Platform(CHANNEL_MOMENTS);
//    }
//
//    @OnClick(R.id.story_release_google_plus)
//    @SuppressWarnings("unused")
//    protected void doShare2GooglePlus(){
//        share2Platform(CHANNEL_GOOGLE_PLUS);
//    }
//
//    @OnClick(R.id.story_release_link)
//    @SuppressWarnings("unused")
//    protected void doShare2CopyUrl(){
//        share2Platform(CHANNEL_COPY_URL);
//    }
//
//    @OnClick(R.id.story_release_qr)
//    @SuppressWarnings("unused")
//    protected void doShare2QR(){
//        share2Platform(CHANNEL_QR_CODE);
//    }
//
//    @OnClick(R.id.story_release_email)
//    @SuppressWarnings("unused")
//    protected void doShare2Email(){
//        share2Platform(CHANNEL_EMAIL);
//    }
//
//    @OnClick(R.id.story_release_sms)
//    @SuppressWarnings("unused")
//    protected void doShare2SMS(){
//        share2Platform(CHANNEL_SMS);
//    }
//
//    @OnClick(R.id.story_release_more)
//    @SuppressWarnings("unused")
//    protected void doShare2More(){
//        share2Platform(CHANNEL_MORE);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        ShareSDK.stopSDK();
    }
}