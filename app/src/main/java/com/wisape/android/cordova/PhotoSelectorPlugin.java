package com.wisape.android.cordova;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.PhotoSelectorActivity;
import com.wisape.android.common.StoryManager;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.util.*;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * 图片选择
 * Created by LeiGuoting on 3/7/15.
 */
public class PhotoSelectorPlugin extends AbsPlugin {
    public static final int REQEUST_CODE_CROP_IMG = 1;

    private int width;
    private int height;
    private Uri bgUri;
    private CallbackContext callbackContext;


    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        width = args.getInt(0);
        height = args.getInt(1);
        this.callbackContext = callbackContext;

        Intent intent = new Intent(getCurrentActivity(), PhotoSelectorActivity.class);
        startActivityForResult(intent, PhotoSelectorActivity.REQUEST_CODE_PHOTO);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        switch (requestCode) {
            case PhotoSelectorActivity.REQUEST_CODE_PHOTO:
                Uri imgUri = extras.getParcelable(PhotoSelectorActivity.EXTRA_IMAGE_URI);

                File file = new File(StoryManager.getStoryDirectory(),
                        StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/img");
                if (!file.exists()) {
                    file.mkdirs();
                }
                File head = new File(file, Utils.acquireUTCTimestamp() + ".jpg");
                bgUri = Uri.fromFile(head);

                Intent intent1 = new Intent("com.android.camera.action.CROP");
                intent1.setDataAndType(imgUri, "image/*");
                //下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
                intent1.putExtra("crop", "true");

                float size =(float) width / height;
                // aspectX aspectY 是宽高的比例
                intent1.putExtra("aspectX", size);
                intent1.putExtra("aspectY", 1);
                intent1.putExtra("scale", true);

                // outputX outputY 是裁剪图片宽高
//                intent1.putExtra("outputX", width);
//                intent1.putExtra("outputY", height);
                intent1.putExtra("return-data", false);
                intent1.putExtra(MediaStore.EXTRA_OUTPUT, bgUri);
                intent1.putExtra("noFaceDetection", true); // no face detection
                startActivityForResult(intent1, REQEUST_CODE_CROP_IMG);
                break;
            case REQEUST_CODE_CROP_IMG:
                if(null != callbackContext){
                    if(null != bgUri){
                        com.wisape.android.util.FileUtils.saveBitmap(bgUri.getPath(), com.wisape.android.util.FileUtils.getSmallBitmap(bgUri.getPath()));
                        File imgFile = new File(bgUri.getPath());

                        if(imgFile.exists()){
                            callbackContext.success(bgUri.getPath());
                        }else {
                            callbackContext.success("");
                        }
                    }else {
                        callbackContext.error("crop error");
                    }
                }
                bgUri = null;
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bgUri = null;
    }
}
