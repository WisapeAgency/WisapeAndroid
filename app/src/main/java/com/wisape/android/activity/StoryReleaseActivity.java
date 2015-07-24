package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.util.FrescoFactory;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_release);
        ButterKnife.inject(this);
        startLoad(WHAT_LOAD_STORY_SETTINGS, null);
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
                int channel = 0;
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

    }

    @OnClick(R.id.story_release_fb)
    @SuppressWarnings("unused")
    protected void doShare2Facebook(){
        releaseStory(CHANNEL_FACEBOOK);
    }

    @OnClick(R.id.story_release_messenger)
    @SuppressWarnings("unused")
    protected void doShare2Messenger(){
        releaseStory(CHANNEL_MESSENGER);
    }

    @OnClick(R.id.story_release_twitter)
    @SuppressWarnings("unused")
    protected void doShare2Twitter(){
        releaseStory(CHANNEL_TWITTER);
    }

    @OnClick(R.id.story_release_linkedin)
    @SuppressWarnings("unused")
    protected void doShare2LinkedIn(){
        releaseStory(CHANNEL_LINKEDIN);
    }

    @OnClick(R.id.story_release_wechat)
    @SuppressWarnings("unused")
    protected void doShare2WeChat(){
        releaseStory(CHANNEL_WECHAT);
    }

    @OnClick(R.id.story_release_moments)
    @SuppressWarnings("unused")
    protected void doShare2Moments(){
        releaseStory(CHANNEL_MOMENTS);
    }

    @OnClick(R.id.story_release_google_plus)
    @SuppressWarnings("unused")
    protected void doShare2GooglePlus(){
        releaseStory(CHANNEL_GOOGLE_PLUS);
    }

    @OnClick(R.id.story_release_link)
    @SuppressWarnings("unused")
    protected void doShare2CopyUrl(){
        releaseStory(CHANNEL_COPY_URL);
    }

    @OnClick(R.id.story_release_qr)
    @SuppressWarnings("unused")
    protected void doShare2QR(){
        releaseStory(CHANNEL_QR_CODE);
    }

    @OnClick(R.id.story_release_email)
    @SuppressWarnings("unused")
    protected void doShare2Email(){
        releaseStory(CHANNEL_EMAIL);
    }

    @OnClick(R.id.story_release_sms)
    @SuppressWarnings("unused")
    protected void doShare2SMS(){
        releaseStory(CHANNEL_SMS);
    }

    @OnClick(R.id.story_release_more)
    @SuppressWarnings("unused")
    protected void doShare2More(){
        releaseStory(CHANNEL_MORE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
