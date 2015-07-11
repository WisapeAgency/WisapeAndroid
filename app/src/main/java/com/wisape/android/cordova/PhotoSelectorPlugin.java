package com.wisape.android.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.soundcloud.android.crop.Crop;
import com.wisape.android.activity.PhotoSelectorActivity;
import com.wisape.android.activity.ScaleCropImageActivity;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.network.NanoServer;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;

import static com.wisape.android.activity.PhotoSelectorActivity.REQUEST_CODE_PHOTO;

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

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if(ACTION_STORY_BACKGROUND.equals(action)){
            startActivityForResult(PhotoSelectorActivity.getIntent(getCurrentActivity().getApplicationContext()), REQUEST_CODE_PHOTO);
            callbackContextMap.put(action, callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "#onActivityResult ___ requestCode:" + requestCode);
        switch (requestCode){
            default :
                super.onActivityResult(requestCode, resultCode, intent);
                return;

            case REQUEST_CODE_PHOTO :
                if(Activity.RESULT_OK == resultCode){
                    Uri imageUri = intent.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                    cropImgUri = PhotoSelectorActivity.buildCropUri(getCurrentActivity(), 1);
                    //Intent cropIntent = Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), cropImgUri).asSquare().getIntent(getCurrentActivity().getApplicationContext());
                    //startActivityForResult(cropIntent, Crop.REQUEST_CROP);

                    Intent cropIntent = ScaleCropImageActivity.getIntent(getCurrentActivity(), PhotoProvider.getPhotoUri(imageUri.getPath()), cropImgUri);
                    startActivityForResult(cropIntent, ScaleCropImageActivity.REQUEST_CODE_CROP);
                }
                break;

            case Crop.REQUEST_CROP :
                if(Activity.RESULT_OK == resultCode){
                    CallbackContext callback = callbackContextMap.get(ACTION_STORY_BACKGROUND);
                    String localPath = cropImgUri.getPath();
                    Uri newUri = NanoServer.makeImageUrl(localPath);
                    callback.success(newUri.toString());
                    cropImgUri = null;
                }
                break;
        }
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
