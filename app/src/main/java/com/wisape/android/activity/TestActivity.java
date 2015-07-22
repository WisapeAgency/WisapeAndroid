package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.wisape.android.R;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.json.JSONArray;

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
        //startLoad(5, null);
        StorySettingsActivity.launch(this, 0);
    }

    @OnClick(R.id.qr_code)
    @SuppressWarnings("unused")
    protected void doQr(){
        startLoad(6, null);
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

        else if(4 == what){
            //获取模板类型
            StoryLogic logic = StoryLogic.instance();
            JSONArray jsonArray = logic.listStoryTemplateTypeLocal(getApplicationContext());
            if(null == jsonArray){
                Log.d(TAG, "listStoryTemplateTypeLocal is null.");
                jsonArray = logic.listStoryTemplateType(getApplicationContext(), getCancelableTag());
            }

            if(null != jsonArray){
                Log.d(TAG, "#listStoryTemplateType TemplateType:" + jsonArray.toString());
            }else{
                Log.d(TAG, "#listStoryTemplateType jsonArray is null.");
            }
        }

        else if(5 == what){
            StoryLogic logic = StoryLogic.instance();
            StoryTemplateEntity[] templateEntities = logic.listStoryTemplate(getApplicationContext(), getCancelableTag());
            if(null == templateEntities){
                Log.d(TAG, "#listStoryTemplate templateEntities is null");
            }else{
                Gson gson = new Gson();
                for(StoryTemplateEntity entity : templateEntities){
                    Log.d(TAG, "#listStoryTemplate entity:" + gson.toJson(entity));
                    if(6 == entity.serverId){
                        StoryManager.downTemplate(getApplicationContext(), entity, null);
                    }
                }
            }
        }

        else if(6 == what){
            //Uri source = Uri.parse("http://106.75.194.11/uploads/2015072108/c93f5445c3304227bb1538493f9c7a0e.zip");
            //StoryManager.downTemplate(getApplicationContext(), source, null);
            StoryLogic logic = StoryLogic.instance();
            StoryTemplateEntity[] templateEntities = logic.listStoryTemplateLocal(getApplicationContext());
            if(null == templateEntities){
                Log.d(TAG, "#listStoryTemplate templateEntities is null");
            }else{
                Gson gson = new Gson();
                for(StoryTemplateEntity entity : templateEntities){
                    Log.d(TAG, "#listStoryTemplate entity:" + gson.toJson(entity));
                    if(6 == entity.serverId){
                        StoryManager.createStory(getApplicationContext(), entity);
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
