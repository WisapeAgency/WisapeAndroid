package com.wisape.android.cordova;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.google.gson.Gson;
import com.wisape.android.api.ApiStory;
import com.wisape.android.api.ApiUser;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by tony on 2015/7/19.
 */
public class StoryTemplatePlugin extends AbsPlugin{
    public static final String ACTION_GET_STAGE_CATEGORY = "getStageCategory";
    public static final String ACTION_GET_STAGE_LIST = "getStageList";

    private static final int WHAT_GET_STAGE_CATEGORY = 0x01;
    private static final int WHAT_GET_STAGE_LIST = 0x02;

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";

    private CallbackContext callbackContext;
    private StoryLogic logic = StoryLogic.instance();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(null == action || 0 == action.length()){
            return true;
        }
        this.callbackContext = callbackContext;
        if(ACTION_GET_STAGE_CATEGORY.equals(action)){//getStageCategory
            startLoad(WHAT_GET_STAGE_CATEGORY, null);
        } else if (ACTION_GET_STAGE_LIST.equals(action)){//getStageList
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));
            }
            startLoad(WHAT_GET_STAGE_LIST, bundle);
        }
        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Context context = getCurrentActivity().getApplicationContext();
        switch (what){
            default :
                return null;
            case WHAT_GET_STAGE_CATEGORY : {
                JSONArray jsonStr = logic.listStoryTemplateType(context, null);
                callbackContext.success(jsonStr);
                break;
            }
            case WHAT_GET_STAGE_LIST: {
                ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
                attr.type = args.getInt(EXTRA_CATEGORY_ID, 0);
                StoryTemplateEntity[] entities = logic.listStoryTemplate(context, attr, null);
                callbackContext.success(new Gson().toJson(entities));
                break;
            }
        }
        return null;
    }

}
