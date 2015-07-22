package com.wisape.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.Downloader;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.SecurityUtils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

import static com.wisape.android.logic.StoryLogic.PREFERENCES;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryManager{
    private static final String TAG = "StoryManager";
    private static final String TEMPLATE_DIRECTORY = "template";
    private static  final String STORY_DIRECTORY = "story";
    private static final String STORY_SETTINGS = "settings";
    private static final String STORY_MUSIC = "music";

    public static final String EXTRA_STORY_SETTINGS = "_story_settings";

    public static Uri downTemplate(Context context, Uri source, String broadcastAction){
        String templateName = source.getLastPathSegment();
        Log.d(TAG, "#downTemplate templateName:" + templateName + ", source:" + source.toString());
        if(!EnvironmentUtils.isMounted()){
            return null;
        }

        Uri downUri = Uri.fromFile(new File(EnvironmentUtils.getAppTemporaryDirectory(), templateName));
        Log.d(TAG, "#downTemplate downUri:" + downUri.toString());
        File template;
        try{
            Downloader.download(context, source, downUri, broadcastAction);

            int index = templateName.lastIndexOf('.');
            String templateDir = templateName;
            if(0 < index){
                templateDir = templateName.substring(0, index);
            }
            Log.d(TAG, "#downTemplate templateDir:" + templateDir);

            template = new File(getStoryTemplateDirectory(), templateDir);
            ZipUtils.unzip(downUri, template);
            FileUtils.deleteDirectory(new File(downUri.getPath()));
        }catch (IOException e){
            Log.e(TAG, "", e);
            return null;
        }
        Log.d(TAG, "#downTemplate completed!!!");
        return Uri.fromFile(template);
    }

    private static File getStoryTemplateDirectory(){
        return new File(EnvironmentUtils.getAppDataDirectory(), TEMPLATE_DIRECTORY);
    }

    private static File getStoryDirectory(){
        return new File(EnvironmentUtils.getAppDataDirectory(), STORY_DIRECTORY);
    }

    public static Uri downTemplate(Context context, StoryTemplateEntity entity, String broadcastAction){
        if(null == entity){
            return null;
        }

        Uri source = Uri.parse(entity.template);
        Uri template = downTemplate(context, source, broadcastAction);
        if(null != template){
            entity.localUri = template.toString();
            StoryLogic.instance().updateStoryTemplateEntity(context, entity);
        }
        return template;
    }

    public static Uri createStory(Context context, StoryTemplateEntity template){
        Uri templateUri;
        String localTemplate = template.localUri;
        if(null == localTemplate || 0 == localTemplate.length()){
            templateUri = downTemplate(context, template, null);
        }else{
            templateUri = Uri.parse(localTemplate);
        }

        Uri storyUri;
        File sourceDir = new File(templateUri.getPath());
        File storyDir = new File(getStoryDirectory(), makeStoryDirectoryName(context));
        Log.d(TAG, "#createStory sourceDir:" + sourceDir.getPath() + ", storyDir:" + storyDir.getPath());
        try {
            FileUtils.copyDirectory(sourceDir, storyDir);
            storyUri = Uri.fromFile(storyDir);
        } catch (IOException e) {
            Log.e(TAG, "", e);
            storyUri = null;
        }
        Log.d(TAG, "#createStory storyUri:" + storyUri);
        return storyUri;
    }

    private static String makeStoryDirectoryName(Context context){
        UserInfo user = UserManager.instance().signIn(context);
        String primary = new StringBuffer(64).append(user.user_id).append(System.currentTimeMillis()).toString();
        String md5 = SecurityUtils.md5(primary);
        return md5;
    }

    private static SoftReference<StorySettingsInfo> storySettingsRef;
    private static Object storySettingsLock = new Object();
    public static StorySettingsInfo acquireStorySettings(Context context){
        StorySettingsInfo storySettings;
        if(null == storySettingsRef || null == (storySettings = storySettingsRef.get())){
            synchronized (storySettingsLock){
                if(null == storySettingsRef || null == (storySettings = storySettingsRef.get())){
                    SharedPreferences preferences = getSharedPreferences(context);
                    String storySettingsStr = preferences.getString(EXTRA_STORY_SETTINGS, null);
                    if(null == storySettingsStr){
                        storySettings = null;
                    }else{
                        try {
                            JSONObject jsonObj = new JSONObject(storySettingsStr);
                            storySettings = StorySettingsInfo.fromJsonObject(jsonObj);
                            storySettingsRef = new SoftReference(storySettings);
                        } catch (JSONException e) {
                            Log.e(TAG, "", e);
                            preferences.edit().remove(EXTRA_STORY_SETTINGS).commit();
                            storySettings = null;
                        }
                    }
                }
            }
        }
        return storySettings;
    }

    public static void saveStorySettings(Context context, StorySettingsInfo storySettings){
        SharedPreferences preferences = getSharedPreferences(context);
        synchronized (storySettingsLock){
            preferences.edit().putString(EXTRA_STORY_SETTINGS, storySettings.toString()).commit();
            storySettingsRef = new SoftReference(storySettings);
        }
    }

    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
