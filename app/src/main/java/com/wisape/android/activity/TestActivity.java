package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.api.ApiStory;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class TestActivity extends BaseActivity{
    private static final String TAG = TestActivity.class.getSimpleName();

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), TestActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button)
    @SuppressWarnings("unused")
    protected void doBtnTest(){
        startLoad(2, null);
    }

    @OnClick(R.id.qr_code)
    @SuppressWarnings("unused")
    protected void doQr(){
        startLoad(3, null);
    }


    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        if(1 == what){
            Uri source = Uri.fromFile(new File(EnvironmentUtils.getAppTemporaryDirectory(), "template_light.zip"));
            File targetDir = new File(EnvironmentUtils.getAppDataDirectory(), "template_light");
            try{
                Log.d(TAG, "#onLoadBackgroundRunning ___");
                ZipUtils.unzip(source, targetDir);
            }catch (IOException e){
                Log.e(TAG, "", e);
            }
        }else if(2 == what){
            Uri zipSource = Uri.fromFile(new File(EnvironmentUtils.getAppDataDirectory(), "template_light"));
            File targetDir = EnvironmentUtils.getAppTemporaryDirectory();
            try{
                ZipUtils.zip(zipSource, targetDir, "template_one.zip");
            }catch (IOException e){
                Log.e(TAG, "", e);
            }
        }

        else if(3 == what){
            ApiStory.AttrStoryInfo story = new ApiStory.AttrStoryInfo();
            Uri thumb = Uri.fromFile(new File(EnvironmentUtils.getAppTemporaryDirectory(), "_crop_0_66899726.jpeg"));
            story.attrStoryThumb = thumb;
            story.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
            story.story = Uri.fromFile(new File(EnvironmentUtils.getAppDataDirectory(), "template_light"));
            story.storyName = "story_one_1.zip";
            story.storyDescription = "hahahaha";
            StoryLogic.instance().update(getApplicationContext(), story, getCancelableTag());
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
