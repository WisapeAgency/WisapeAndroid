package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.model.StoryGestureInfo;
import com.wisape.android.model.StorySettingsInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 故事设置
 * Created by tony on 2015/7/21.
 */
public class StorySettingsActivity extends BaseActivity{
    private static final int WHAT_LOAD_SETTINGS = 0x01;

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), StorySettingsActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @InjectView(R.id.story_settings_name)
    protected AppCompatEditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected AppCompatEditText storyDescEdit;

    @InjectView(R.id.story_settings_cover_sdv)
    protected ImageView storyBgView;

    @InjectView(R.id.story_settings_music)
    protected AppCompatTextView storyMusicTxtv;
    @InjectView(R.id.story_settings_gesture)
    protected AppCompatTextView storyGestureTxtv;

    private StorySettingsInfo storySettings;
    private Uri storyCoverUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_settings);
        ButterKnife.inject(this);
        startLoad(WHAT_LOAD_SETTINGS, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        doSaveStorySettings();
    }

    @OnClick(R.id.story_settings_cover_sdv)
    @SuppressWarnings("unused")
    protected void doSelectStoryCover(){
        PhotoSelectorActivity.launch(this,PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @OnClick(R.id.story_settings_music_layout)
    @SuppressWarnings("unused")
    protected void doSelectStoryMusic(){
        StoryMusicEntity music = (null == storySettings ? null : storySettings.defaultMusic);
        StoryMusicActivity.launch(this, music, StoryMusicActivity.REQUEST_CODE_STORY_MUSIC);
    }

    @OnClick(R.id.story_settings_gesture_layout)
    @SuppressWarnings("unused")
    protected void doSelectStroySlideMotion(){
        GestorChoiceActivity.launch(this, storySettings.defaultGesture, GestorChoiceActivity.REQUEST_CODE_SLIDE_MOTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            default :
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case PhotoSelectorActivity.REQUEST_CODE_PHOTO :
                if(RESULT_OK == resultCode){
                    Uri imageUri = data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    if(null != imageUri){
                        CutActivity.launch(this,imageUri,CutActivity.RQEUST_CODE_CROP_IMG);
                    }
                }
                break;

            case CutActivity.RQEUST_CODE_CROP_IMG :
                if(RESULT_OK == resultCode){
                    storyCoverUri = data.getParcelableExtra(CutActivity.EXTRA_IMAGE_URI);
                    if(null != storyCoverUri){
                        storySettings.defaultCover = storyCoverUri.toString();
                        Picasso.with(this).load(storyCoverUri)
                                .resize(80, 80)
                                .centerCrop()
                                .into(storyBgView);
                    }
                }
                break;

            case GestorChoiceActivity.REQUEST_CODE_SLIDE_MOTION:
                StoryGestureInfo gestureInfo = data.getParcelableExtra(GestorChoiceActivity.EXTRO_SELECT_SLIDE);
                storySettings.defaultGesture = gestureInfo;
                storyGestureTxtv.setText(gestureInfo.name);
                break;

            case StoryMusicActivity.REQUEST_CODE_STORY_MUSIC :
                if(RESULT_OK == resultCode){
                    StoryMusicEntity selectedMusic = data.getParcelableExtra(StoryMusicActivity.EXTRA_SELECTED_MUSIC);
                    storySettings.defaultMusic = selectedMusic;
                    storyMusicTxtv.setText(selectedMusic.name);
                }
                break;
        }
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg;
        switch (what){
            default :
                msg = null;
                break;

            case WHAT_LOAD_SETTINGS :
                msg = Message.obtain();
                msg.obj = StoryManager.acquireStorySettings(getApplicationContext());
                break;

        }
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        switch (data.what){
            default :
                return;

            case WHAT_LOAD_SETTINGS :
                if(null != data.obj){
                    StorySettingsInfo settings = (StorySettingsInfo) data.obj;
                    storyNameEdit.setText(settings.defaultName);
                    storyDescEdit.setText(settings.defaultDesc);
                    Uri defaultCover =Uri.parse(settings.defaultCover);
                    if(null != defaultCover){
                        Picasso.with(this).load(defaultCover)
                                .resize(150, 150)
                                .centerCrop()
                                .into(storyBgView);
                    }

                    StoryMusicEntity music = settings.defaultMusic;
                    if(null != music){
                        storyMusicTxtv.setText(music.name);
                    }
                    StoryGestureInfo gesture = settings.defaultGesture;
                    if(null != gesture){
                        storyGestureTxtv.setText(gesture.name);
                    }
                    storySettings = settings;
                }
                break;
        }
    }

    @Override
    protected boolean onBackNavigation() {
        doSaveStorySettings();
        return super.onBackNavigation();
    }

    @Override
    public void onBackPressed() {
        doSaveStorySettings();
        super.onBackPressed();
    }

    private void doSaveStorySettings(){
        StorySettingsInfo settings = storySettings;

        String storyName = storyNameEdit.getText().toString();
        String storyDesc = storyDescEdit.getText().toString();
        settings.defaultName = storyName;
        settings.defaultDesc = storyDesc;
        if (null != storyCoverUri){
            settings.defaultCover = storyCoverUri.toString();
        }

        StoryManager.saveStorySettings(getApplicationContext(), settings);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
