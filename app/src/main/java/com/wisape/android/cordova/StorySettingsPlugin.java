package com.wisape.android.cordova;

import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.StorySettingsActivity;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.util.*;
import com.wisape.android.util.FileUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

/**
 * Created by tony on 2015/7/22.
 */
public class StorySettingsPlugin extends AbsPlugin {
    public static final String ACTION_SETTINGS_OPEN = "open_story_settings";


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action,CallbackContext callbackContext){

        callbackContext.success(getStroySetting().toJSONString());

        return true;
    }


    private JSONObject getStroySetting() {
        StorySettingsInfo storySettingsInfo = StoryManager.acquireStorySettings(WisapeApplication.getInstance().getApplicationContext());
        JSONObject jsonObject = new JSONObject();

        StoryMusicEntity storyMusicEntity = storySettingsInfo.defaultMusic;
        Uri uri = storySettingsInfo.defaultCover;

        if(storyMusicEntity == null){
            jsonObject.put("music","");
        }else{
            jsonObject.put("music",storyMusicEntity.music);
        }

        jsonObject.put("cover", FileUtils.getRealPathFromURI(WisapeApplication.getInstance().getApplicationContext(),
                uri));

        return jsonObject;
    }


}
