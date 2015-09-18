package com.wisape.android.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.wisape.android.activity.CutActivity;
import com.wisape.android.activity.PhotoSelectorActivity;
import com.wisape.android.network.NanoServer;
import com.wisape.android.util.*;
import com.wisape.android.util.FileUtils;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class PhotoSelectorPlugin extends AbsPlugin {
    private static final String TAG = PhotoSelectorPlugin.class.getSimpleName();
    private static final String ACTION_STORY_BACKGROUND = "story";

    private String cropImgUri;
    private HashMap<String, CallbackContext> callbackContextMap;
    private int width;
    private int height;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        callbackContextMap = new HashMap(3);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Intent intent = new Intent(getCurrentActivity(), PhotoSelectorActivity.class);

        width = args.getInt(0);
        height = args.getInt(1);

        startActivityForResult(intent, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
        synchronized (callbackContextMap) {
            callbackContextMap.remove(ACTION_STORY_BACKGROUND);
            callbackContextMap.put(ACTION_STORY_BACKGROUND, callbackContext);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "#onActivityResult ___ requestCode:" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                    Uri imgUri = extras.getParcelable(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    Intent intent1 = new Intent(getCurrentActivity(), CutActivity.class);
                    intent1.putExtra(CutActivity.EXTRA_IMAGE_URI, imgUri);
                    intent1.putExtra(CutActivity.EXRA_WIDTH,width);
                    intent1.putExtra(CutActivity.EXRA_HEIGHT,height);
                    startActivityForResult(intent1, CutActivity.RQEUST_CODE_CROP_IMG);
                    break;
                case CutActivity.RQEUST_CODE_CROP_IMG:
                    cropImgUri = extras.getString(CutActivity.EXTRA_IMAGE_URI);
                    CallbackContext callback;
                    synchronized (callbackContextMap) {
                        callback = callbackContextMap.remove(ACTION_STORY_BACKGROUND);
                    }
                    if (null != callback) {
                        callback.success(cropImgUri);
                    }
                    cropImgUri = null;
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, intent);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != callbackContextMap) {
            callbackContextMap.clear();
            callbackContextMap = null;
        }
        cropImgUri = null;
    }
}
