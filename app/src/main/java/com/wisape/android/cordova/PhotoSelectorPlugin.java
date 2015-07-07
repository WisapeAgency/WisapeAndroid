package com.wisape.android.cordova;

import android.util.Log;

import com.wisape.android.activity.PhotoSelectorActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONException;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class PhotoSelectorPlugin extends AbsPlugin {
    private static final String TAG = PhotoSelectorPlugin.class.getSimpleName();

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "#execute action:" + action);
        if("select".equals(action)){
            PhotoSelectorActivity.launch(getCurrentActivity(), 1);
            return true;
        }
        return false;
    }
}
