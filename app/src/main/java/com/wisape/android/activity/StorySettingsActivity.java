package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.soundcloud.android.crop.Crop;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.model.StoryGestureInfo;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.util.FrescoFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tony on 2015/7/21.
 */
public class StorySettingsActivity extends BaseActivity{
    private static final int WHAT_LOAD_SETTINGS = 0x01;

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(getIntent(activity.getApplicationContext()), requestCode);
    }

    public static Intent getIntent(Context context){
        Intent intent = new Intent(context, StorySettingsActivity.class);
        return intent;
    }

    @InjectView(R.id.story_settings_name)
    protected AppCompatEditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected AppCompatEditText storyDescEdit;
    @InjectView(R.id.story_settings_cover_sdv)
    protected SimpleDraweeView storyBgView;

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
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @OnClick(R.id.story_settings_music_layout)
    @SuppressWarnings("unused")
    protected void doSelectStoryMusic(){
        StoryMusicEntity music = (null == storySettings ? null : storySettings.defaultMusic);
        StoryMusicActivity.launch(this, music, StoryMusicActivity.REQUEST_CODE_STORY_MUSIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            default :
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case PhotoSelectorActivity.REQUEST_CODE_PHOTO :
                if(RESULT_OK == resultCode){
                    storyCoverUri = PhotoSelectorActivity.buildCropUri(this, 0);
                    Uri imageUri = data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    //Intent cropIntent = Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), storyCoverUri).asSquare().getIntent(getApplicationContext());
                    //startActivityForResult(cropIntent, Crop.REQUEST_CROP);

                    Intent cropIntent = ScaleCropImageActivity.getIntent(this, PhotoProvider.getPhotoUri(imageUri.getPath()), storyCoverUri);
                    startActivityForResult(cropIntent, ScaleCropImageActivity.REQUEST_CODE_CROP);
                }
                break;

            case Crop.REQUEST_CROP :
                if(RESULT_OK == resultCode){
                    storyBgView.setImageURI(storyCoverUri);
                }
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
                    Uri defaultCover = settings.defaultCover;
                    if(null != defaultCover){
                        FrescoFactory.bindImageFromUri(storyBgView, defaultCover.toString());
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
        StoryManager.saveStorySettings(getApplicationContext(), settings);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
