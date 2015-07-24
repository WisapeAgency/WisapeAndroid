package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;
import android.widget.TextView;

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
    protected EditText storyNameEdit;
    @InjectView(R.id.story_settings_desc)
    protected EditText storyDescEdit;
    @InjectView(R.id.story_settings_cover_sdv)
    protected SimpleDraweeView storyBgView;

    @InjectView(R.id.story_settings_music)
    protected TextView storyMusicTxtv;
    @InjectView(R.id.story_settings_gesture)
    protected TextView storyGestureTxtv;

    private StorySettingsInfo storySettings;
    private Uri storyCoverUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_settings);
        ButterKnife.inject(this);
        startLoad(WHAT_LOAD_SETTINGS, null);
        //TODO
        storySettings = new StorySettingsInfo();
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
                    Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), storyCoverUri).asSquare().start(this);
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
                    Uri defaultBackground = settings.defaultBackground;
                    if(null != defaultBackground){
                        FrescoFactory.bindImageFromUri(storyBgView, defaultBackground.toString());
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

    private void doSaveStorySettings(){
        StorySettingsInfo settings;
        if(null == storySettings){
            settings = new StorySettingsInfo();
        }else{
            settings = storySettings;
        }

        String storyName = storyNameEdit.getText().toString();
        String storyDesc = storyDescEdit.getText().toString();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
