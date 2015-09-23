package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.content.StoryBroadcastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.model.StoryGestureInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 故事设置
 * Created by tony on 2015/7/21.
 */
public class StorySettingsActivity extends BaseActivity {
    private static final int LOADER_UPATE_SETTINGS = 0x01;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 1200;

    public static final int REQUEST_SETTING = 1;

    public static void launch(Activity activity, int requestCode) {
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

    private StoryEntity storyEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_settings);
        ButterKnife.inject(this);
        storyEntity = wisapeApplication.getStoryEntity();
        setViewData();
    }

    private void setViewData() {
        String storyName = storyEntity.storyName;
        if (!Utils.isEmpty(storyName)) {
            storyNameEdit.setText(storyName);
        }
        String storyDesc = storyEntity.storyDesc;
        if (!Utils.isEmpty(storyDesc)) {
            storyDescEdit.setText(storyDesc);
        }

        String uri = storyEntity.storyThumbUri;
        if (null != uri) {
            Picasso.with(this).load(new File(uri))
                    .resize(150, 150)
                    .centerCrop()
                    .into(storyBgView);
        }
        String storyMusicName = storyEntity.storyMusicName;
        if (!Utils.isEmpty(storyMusicName)) {
            storyMusicTxtv.setText(storyMusicName);
        }
        String storyGestorName = storyEntity.storyGestor;
        if (!Utils.isEmpty(storyGestorName)) {
            storyGestureTxtv.setText(storyGestorName);
        }
    }

    @OnClick(R.id.story_settings_cover_sdv)
    @SuppressWarnings("unused")
    protected void doSelectStoryCover() {
        PhotoSelectorActivity.launch(this, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
    }

    @OnClick(R.id.story_settings_music_layout)
    @SuppressWarnings("unused")
    protected void doSelectStoryMusic() {
        StoryMusicEntity musicEntity = new StoryMusicEntity();
        String musicName = storyEntity.storyMusicName;
        musicEntity.musicLocal = storyEntity.storyMusicLocal;
        musicEntity.name = storyEntity.storyMusicName;
        musicEntity.serverId = storyEntity.musicServerId;
        StoryMusicActivity.launch(this, musicEntity, StoryMusicActivity.REQUEST_CODE_STORY_MUSIC);
    }

    @OnClick(R.id.story_settings_gesture_layout)
    @SuppressWarnings("unused")
    protected void doSelectStroySlideMotion() {
        StoryGestureInfo storyGestureInfo = null;
        String gestorName = storyEntity.storyGestor;
        if (!Utils.isEmpty(gestorName)) {
            storyGestureInfo = new StoryGestureInfo();
            if (gestorName.contains("left")) {
                storyGestureInfo.id = GestorChoiceActivity.GESTOR_LEFT_ID;
            } else {
                storyGestureInfo.id = GestorChoiceActivity.GETSORY_TOP_ID;
            }
        }
        GestorChoiceActivity.launch(this, storyGestureInfo, GestorChoiceActivity.REQUEST_CODE_SLIDE_MOTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
                return;

            case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                if (RESULT_OK == resultCode) {
                    Uri imageUri = data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    File file = new File(StoryManager.getStoryDirectory(), storyEntity.storyName + "/thumb");
                    if (null != imageUri) {
                        CutActivity.launch(this, imageUri, WIDTH, HEIGHT,file.getAbsolutePath(), CutActivity.RQEUST_CODE_CROP_IMG);
                    }
                }
                break;

            case CutActivity.RQEUST_CODE_CROP_IMG:
                if (RESULT_OK == resultCode) {
                    String storyCoverUri = data.getStringExtra(CutActivity.EXTRA_IMAGE_URI);
                    if (null != storyCoverUri) {
                        storyEntity.storyThumbUri = storyCoverUri.toString();
                        Picasso.with(this).load(new File(storyCoverUri))
                                .resize(80, 80)
                                .centerCrop()
                                .into(storyBgView);
                    }
                }
                break;

            case GestorChoiceActivity.REQUEST_CODE_SLIDE_MOTION:
                StoryGestureInfo gestureInfo = data.getParcelableExtra(GestorChoiceActivity.EXTRO_SELECT_SLIDE);
                this.storyEntity.storyGestor = gestureInfo.name;
                storyGestureTxtv.setText(gestureInfo.name);
                break;

            case StoryMusicActivity.REQUEST_CODE_STORY_MUSIC:
                if (RESULT_OK == resultCode) {
                    StoryMusicEntity selectedMusic = data.getParcelableExtra(StoryMusicActivity.EXTRA_SELECTED_MUSIC);
                    this.storyEntity.storyMusicName = selectedMusic.name;
                    String path = Uri.parse(selectedMusic.musicLocal).getPath();
                    this.storyEntity.storyMusicLocal = path;
                    this.storyMusicTxtv.setText(selectedMusic.name);
                    this.storyEntity.musicServerId = selectedMusic.serverId;
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

    private void doSaveStorySettings() {
        storyEntity.storyName = storyNameEdit.getText().toString();
        storyEntity.storyDesc = storyDescEdit.getText().toString();
        wisapeApplication.setStoryEntity(storyEntity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        Intent intent = new Intent();
    }
}
