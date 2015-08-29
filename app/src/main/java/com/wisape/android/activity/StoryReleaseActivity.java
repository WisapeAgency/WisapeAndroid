package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.util.FrescoFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tony on 2015/7/24.
 */
public class StoryReleaseActivity extends BaseActivity{
    public static final int REQUEST_CODE_STORY_RELEASE = 110;
    private static final String TAG = StoryReleaseActivity.class.getSimpleName();
    private static final int WHAT_LOAD_STORY_SETTINGS = 0x01;
    private static final int WHAT_RELEASE_STORY = 0x02;

    public static final String EXTRA_STORY_CHANNEL = "extra_story_channel";

    public static final int CHANNEL_FACEBOOK = 0x01;
    public static final int CHANNEL_MESSENGER = 0x02;
    public static final int CHANNEL_TWITTER = 0x03;
    public static final int CHANNEL_LINKEDIN = 0x04;
    public static final int CHANNEL_WECHAT = 0x05;
    public static final int CHANNEL_MOMENTS = 0x06;
    public static final int CHANNEL_GOOGLE_PLUS = 0x07;
    public static final int CHANNEL_COPY_URL = 0x08;
    public static final int CHANNEL_QR_CODE = 0x09;
    public static final int CHANNEL_EMAIL = 0x0a;
    public static final int CHANNEL_SMS = 0x0b;
    public static final int CHANNEL_MORE = 0x0c;

    public static final String PACKAGE_FACEBOOK = "com.facebook.katana";
    public static final String PACKAGE_MESSAGE = "com.twitter.android";
    public static final String PACKAGE_TWITTER = "com.twitter.android";
    public static final String PACKAGE_LINKEDIN = "com.facebook.katana";
    public static final String PACKAGE_WECHAT = "com.facebook.katana";
    public static final String PACKAGE_MOMENTS = "com.facebook.katana";
    public static final String PACKAGE_GOOGLE_PLUS = "com.facebook.katana";
    public static final String PACKAGE_COPY_URL = "com.facebook.katana";
    public static final String PACKAGE_QR_CODE = "com.facebook.katana";
    public static final String PACKAGE_EMAIL = "com.facebook.katana";
    public static final String PACKAGE_SMS = "com.facebook.katana";

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(getIntent(activity), requestCode);
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
    protected SimpleDraweeView storyCoverView;

    private Map<Integer, String> shareMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_release);
        ButterKnife.inject(this);
        startLoad(WHAT_LOAD_STORY_SETTINGS, null);
        initShareMap();
    }

    private void initShareMap(){
        shareMap.put(CHANNEL_FACEBOOK,PACKAGE_FACEBOOK);
        shareMap.put(CHANNEL_MESSENGER,PACKAGE_MESSAGE);
        shareMap.put(CHANNEL_TWITTER,PACKAGE_TWITTER);
        shareMap.put(CHANNEL_LINKEDIN,PACKAGE_LINKEDIN);
        shareMap.put(CHANNEL_WECHAT,PACKAGE_WECHAT);
        shareMap.put(CHANNEL_MOMENTS,PACKAGE_MOMENTS);
        shareMap.put(CHANNEL_GOOGLE_PLUS,PACKAGE_GOOGLE_PLUS);
        shareMap.put(CHANNEL_COPY_URL,PACKAGE_COPY_URL);
        shareMap.put(CHANNEL_QR_CODE,PACKAGE_QR_CODE);
        shareMap.put(CHANNEL_EMAIL,PACKAGE_EMAIL);
        shareMap.put(CHANNEL_SMS,PACKAGE_SMS);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg;
        switch (what){
            default :
                msg = null;
                break;

            case WHAT_LOAD_STORY_SETTINGS :
                msg = Message.obtain();
                StorySettingsInfo settings = StoryManager.acquireStorySettings(getApplicationContext());
                msg.obj = settings;
                msg.arg1 = STATUS_SUCCESS;
                break;

            case WHAT_RELEASE_STORY :
                msg = Message.obtain();
                msg.what = WHAT_RELEASE_STORY;
                msg.setData(args);
                break;
        }
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        switch (data.what){
            default :
                return;

            case WHAT_LOAD_STORY_SETTINGS :
                if(STATUS_SUCCESS == data.arg1){
                    StorySettingsInfo settings = (StorySettingsInfo)data.obj;
                    storyNameEdit.setText(settings.defaultName);
                    storyDescEdit.setText(settings.defaultDesc);
                    Uri cover = settings.defaultCover;
                    if(null != cover){
                        FrescoFactory.bindImageFromUri(storyCoverView, cover);
                    }
                }
                break;

            case WHAT_RELEASE_STORY :
                Bundle bundle = data.getData();
                int channel = bundle.getInt(EXTRA_STORY_CHANNEL,0);
                share2Platform(channel);
                break;
        }
    }

    private void releaseStory(int channel){
        Bundle args = new Bundle();
        args.putInt(EXTRA_STORY_CHANNEL, channel);
        startLoad(WHAT_RELEASE_STORY, args);
    }

    private void share2Platform(int channel){
        File template = new File(StoryManager.getStoryTemplateDirectory(),"mingpian01");
        File thumb = new File(template,"thumb.jpg");
        String msg = "推荐给大家，http://www.wisape.com/demo/playstory/index.html";

        String app_package = shareMap.get(channel);
        if ("".equals(app_package)){
            shareMessage("标题", "消息标题", msg, thumb);
        } else {
            shareMessage(app_package,"标题", "消息标题", msg, thumb);
        }
    }

    public void shareMessage(String activityTitle, String msgTitle, String msgText, File image) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (null == image) {
            intent.setType("text/plain"); // 纯文本
        } else {
            if (image.exists() && image.isFile()) {
                intent.setType("image/jpg");
                Uri uri = Uri.fromFile(image);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }


    private void shareMessage(String type,String activityTitle, String msgTitle, String msgText,
                              File imgPath) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/jpeg");
        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(Intent.EXTRA_SUBJECT,  msgTitle);
                    share.putExtra(Intent.EXTRA_TEXT,     msgText);
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imgPath) ); // Optional, just if you wanna share an image.
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return;
            startActivity(Intent.createChooser(share, "Select"));
        }
    }

    @OnClick(R.id.story_release_fb)
    @SuppressWarnings("unused")
    protected void doShare2Facebook(){
        share2Platform(CHANNEL_FACEBOOK);
    }

    @OnClick(R.id.story_release_messenger)
    @SuppressWarnings("unused")
    protected void doShare2Messenger(){
        share2Platform(CHANNEL_MESSENGER);
    }

    @OnClick(R.id.story_release_twitter)
    @SuppressWarnings("unused")
    protected void doShare2Twitter(){
        share2Platform(CHANNEL_TWITTER);
    }

    @OnClick(R.id.story_release_linkedin)
    @SuppressWarnings("unused")
    protected void doShare2LinkedIn(){
        share2Platform(CHANNEL_LINKEDIN);
    }

    @OnClick(R.id.story_release_wechat)
    @SuppressWarnings("unused")
    protected void doShare2WeChat(){
        share2Platform(CHANNEL_WECHAT);
    }

    @OnClick(R.id.story_release_moments)
    @SuppressWarnings("unused")
    protected void doShare2Moments(){
        share2Platform(CHANNEL_MOMENTS);
    }

    @OnClick(R.id.story_release_google_plus)
    @SuppressWarnings("unused")
    protected void doShare2GooglePlus(){
        share2Platform(CHANNEL_GOOGLE_PLUS);
    }

    @OnClick(R.id.story_release_link)
    @SuppressWarnings("unused")
    protected void doShare2CopyUrl(){
        share2Platform(CHANNEL_COPY_URL);
    }

    @OnClick(R.id.story_release_qr)
    @SuppressWarnings("unused")
    protected void doShare2QR(){
        share2Platform(CHANNEL_QR_CODE);
    }

    @OnClick(R.id.story_release_email)
    @SuppressWarnings("unused")
    protected void doShare2Email(){
        share2Platform(CHANNEL_EMAIL);
    }

    @OnClick(R.id.story_release_sms)
    @SuppressWarnings("unused")
    protected void doShare2SMS(){
        share2Platform(CHANNEL_SMS);
    }

    @OnClick(R.id.story_release_more)
    @SuppressWarnings("unused")
    protected void doShare2More(){
        share2Platform(CHANNEL_MORE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
