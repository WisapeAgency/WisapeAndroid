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
import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class PhotoSelectorPlugin extends AbsPlugin {
    private static final String TAG = PhotoSelectorPlugin.class.getSimpleName();
    private static final String ACTION_STORY_BACKGROUND = "story";

    private Uri cropImgUri;
    private HashMap<String, CallbackContext> callbackContextMap;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        callbackContextMap = new HashMap(3);
    }

    public boolean execute(String action,CallbackContext callbackContext) throws JSONException {

            PhotoSelectorActivity.launch(getCurrentActivity(), PhotoSelectorActivity.REQUEST_CODE_PHOTO);
//            startActivityForResult(PhotoSelectorActivity.getIntent(getCurrentActivity().getApplicationContext()), REQUEST_CODE_PHOTO);
            synchronized (callbackContextMap){
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
                    CutActivity.launch(getCurrentActivity(), imgUri, CutActivity.RQEUST_CODE_CROP_IMG);
                    break;
                case CutActivity.RQEUST_CODE_CROP_IMG:
                    cropImgUri = extras.getParcelable(CutActivity.EXTRA_IMAGE_URI);
                    CallbackContext callback;
                    synchronized (callbackContextMap){
                        callback = callbackContextMap.remove(ACTION_STORY_BACKGROUND);
                    }
                    if(null != callback){
                        callback.success(FileUtils.getRealPathFromURI(getCurrentActivity(),
                                cropImgUri));
                    }
                    cropImgUri = null;
                    break;
               default:
                   super.onActivityResult(requestCode,resultCode,intent);
                   break;
            }
        }
//        switch (requestCode){
//            default :
//                super.onActivityResult(requestCode, resultCode, intent);
//                return;
//
//            case REQUEST_CODE_PHOTO :
//                if(Activity.RESULT_OK == resultCode){
//                    Uri imageUri = intent.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
//                    cropImgUri = PhotoSelectorActivity.buildCropUri(getCurrentActivity(), 1);
////                    Intent cropIntent = Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), cropImgUri).asSquare().getIntent(getCurrentActivity().getApplicationContext());
////                    startActivityForResult(cropIntent, Crop.REQUEST_CROP);
//
//                    //Intent cropIntent = ScaleCropImageActivity.getIntent(getCurrentActivity(), PhotoProvider.getPhotoUri(imageUri.getPath()), cropImgUri);
//                    //startActivityForResult(cropIntent, ScaleCropImageActivity.REQUEST_CODE_CROP);
//                }
//                break;
//
////            case Crop.REQUEST_CROP :
////                if(Activity.RESULT_OK == resultCode){
////                    CallbackContext callback;
////                    synchronized (callbackContextMap){
////                        callback = callbackContextMap.remove(ACTION_STORY_BACKGROUND);
////                    }
////
////                    if(null != callback){
////                        String localPath = cropImgUri.getPath();
////                        Uri newUri = NanoServer.makeImageUrl(localPath);
////                        callback.success(newUri.toString());
////                    }
////                    cropImgUri = null;
////                }
////                break;
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != callbackContextMap){
            callbackContextMap.clear();
            callbackContextMap = null;
        }
        cropImgUri = null;
    }
}
