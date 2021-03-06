package com.wisape.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
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
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.wisape.android.logic.StoryLogic.PREFERENCES;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryManager{
    private static final String TAG = "StoryManager";
    public static final String TEMPLATE_DIRECTORY = "template";
    public static  final String STORY_DIRECTORY = "story";
    private static final String STORY_SETTINGS = "settings";
    private static final String STORY_MUSIC = "music";
    private static final String STORY_FONT = "font";

    public static final String EXTRA_STORY_SETTINGS = "_story_settings";
    private static final ConcurrentLinkedQueue<String> actionQueue = new ConcurrentLinkedQueue<>();

    public static void addAction(String action){
        if(null == action || 0 == action.length()){
            return;
        }

        if(!actionQueue.contains(action)){
            actionQueue.add(action);
        }
    }

    public static boolean containsAction(String action){
        if(null == action || 0 == action.length()){
            return false;
        }
        return actionQueue.contains(action);
    }

    public static Uri downTemplate(Context context, Uri source, String broadcastAction, Bundle tag){
        String templateName = source.getLastPathSegment();
        Log.d(TAG, "#downTemplate templateName:" + templateName + ", source:" + source.toString());
        if(!EnvironmentUtils.isMounted()){
            return null;
        }

        File downFile = new File(EnvironmentUtils.getAppTemporaryDirectory(), templateName);
        Uri downUri = Uri.fromFile(downFile);
        Log.d(TAG, "#downTemplate downUri:" + downUri.toString());
        File template;
        try{
            Downloader.download(context, source, downUri, broadcastAction, tag);

            int index = templateName.lastIndexOf('.');
            String templateDir = templateName;
            if(0 < index){
                templateDir = templateName.substring(0, index);
            }
            Log.d(TAG, "#downTemplate templateDir:" + templateDir);

            template = new File(getStoryTemplateDirectory(), templateDir);
            ZipUtils.unzip(downUri, template);
        }catch (IOException e){
            Log.e(TAG, "", e);
            return null;
        }finally {
            if(null != downFile){
                if(!downFile.delete()){
                    downFile.deleteOnExit();
                }
            }
        }
        Log.d(TAG, "#downTemplate completed!!!");
        return Uri.fromFile(template);
    }

    public static File getStoryTemplateDirectory(){
        File templateDirectory = new File(EnvironmentUtils.getAppDataDirectory(), TEMPLATE_DIRECTORY);
        if (!templateDirectory.exists()){
            templateDirectory.mkdirs();
        }
        return templateDirectory;
    }

    public static File getStoryDirectory(){
        File storyDirectory = new File(EnvironmentUtils.getAppDataDirectory(), STORY_DIRECTORY);
        if (!storyDirectory.exists()){
            storyDirectory.mkdirs();
        }
        return storyDirectory;
    }

    public static File getStoryMusicDirectory(){
        File musicDirectory = new File(EnvironmentUtils.getAppDataDirectory(), STORY_MUSIC);
        if (!musicDirectory.exists()){
            musicDirectory.mkdirs();
        }
        return musicDirectory;
    }

    public static File getStoryFontDirectory(){
        File fontDirectory = new File(EnvironmentUtils.getAppDataDirectory(), STORY_FONT);
        if (!fontDirectory.exists()){
            fontDirectory.mkdirs();
        }
        return fontDirectory;
    }

    public static Uri downTemplate(Context context, StoryTemplateEntity entity, String broadcastAction, Bundle tag){
        if(null == entity){
            return null;
        }

        Uri source = Uri.parse(entity.template);
        Uri template = downTemplate(context, source, broadcastAction, tag);
        if(null != template){
            entity.templateLocal = template.toString();
            StoryLogic.instance().updateStoryTemplate(context, entity);
        }
        return template;
    }

    public static Uri downStoryMusic(Context context, StoryMusicEntity music, String broadcastAction, Bundle tag){
        if(null == music){
            return null;
        }

        Uri download;
        Uri source = music.getDownloadUrl();
        StoryLogic logic = StoryLogic.instance();
        boolean addToActionQueue = null != broadcastAction && 0 < broadcastAction.length();
        try{
            if(addToActionQueue){
                if(actionQueue.contains(broadcastAction)){
                    addToActionQueue = false;
                }else{
                    actionQueue.add(broadcastAction);
                }
            }
            music.status = StoryMusicEntity.STATUS_DOWNLOADING;
            logic.updateStoryMusic(context, music);

            download = downStoryMusic(context, source, broadcastAction, tag);
        }catch (Throwable e){
            Log.e(TAG, "", e);
            Downloader.removeDownloader(source);
            music.status = StoryMusicEntity.STATUS_NONE;
            music.musicLocal = "";
            logic.updateStoryMusic(context, music);
            return null;
        }finally {
            if(addToActionQueue){
                actionQueue.remove(broadcastAction);
            }
        }

        if(null != download){
            music.status = StoryMusicEntity.STATUS_NONE;
            music.musicLocal = download.toString();
            logic.updateStoryMusic(context, music);
        }
        return download;
    }

    public static Uri downStoryMusic(Context context, Uri source, String broadcastAction, Bundle tag) throws IOException{
        if(!EnvironmentUtils.isMounted()){
            return null;
        }

        String musicName = source.getLastPathSegment();
        Uri downUri = Uri.fromFile(new File(getStoryMusicDirectory(), musicName));

        Log.d(TAG, "#downStoryMusic broadcastAction:" + broadcastAction);
        Log.d(TAG, "#downStoryMusic source:" + source);
        Log.d(TAG, "#downStoryMusic dest:" + downUri);
        Downloader.download(context, source, downUri, broadcastAction, tag);
        return downUri;
    }

    public static Uri createStory(Context context, StoryTemplateEntity template){
        Uri templateUri;
        Uri localTemplate = Uri.parse(template.templateLocal);
        if(null == localTemplate){
            templateUri = downTemplate(context, template, null, null);
        }else{
            templateUri = localTemplate;
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
        UserInfo user = UserLogic.instance().getUserInfoFromLocal();
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
                        storySettings = new StorySettingsInfo();
                    }else{
                        try {
                            JSONObject jsonObj = new JSONObject(storySettingsStr);
                            storySettings = StorySettingsInfo.fromJsonObject(jsonObj);
                            storySettingsRef = new SoftReference(storySettings);
                        } catch (JSONException e) {
                            Log.e(TAG, "", e);
                            preferences.edit().remove(EXTRA_STORY_SETTINGS).commit();
                            storySettings = new StorySettingsInfo();
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
            preferences.edit().putString(EXTRA_STORY_SETTINGS, storySettings.toString()).apply();
            storySettingsRef = new SoftReference(storySettings);
        }
    }

    private static SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
