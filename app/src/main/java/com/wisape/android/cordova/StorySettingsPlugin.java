package com.wisape.android.cordova;

import com.wisape.android.activity.StorySettingsActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONException;

/**
 * Created by tony on 2015/7/22.
 */
public class StorySettingsPlugin extends AbsPlugin{
    public static final String ACTION_SETTINGS_OPEN = "open_story_settings";

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if(null == action || 0 == action.length()){
            return true;
        }

        if(ACTION_SETTINGS_OPEN.equals(action)){
            startActivityForResult(StorySettingsActivity.getIntent(getCurrentActivity().getApplicationContext()), 1);
        }
        return true;
    }


}
