package com.wisape.android.cordova;

import android.os.Bundle;
import android.os.Message;

import com.google.gson.Gson;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import java.util.HashMap;

/**
 * Created by tony on 2015/7/19.
 */
public class StoryTemplatePlugin extends AbsPlugin{
    public static final String ACTION_LIST_TEMPLATE_TYPE = "list_template_type";
    public static final String ACTION_LIST_TEMPLATE = "list_template";

    private static final int WHAT_LIST_TEMPLATE_TYPE = 0x01;
    private static final int WHAT_LIST_TEMPLATE = 0x02;


    private HashMap<String, CallbackContext> callbackContextMap;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        callbackContextMap = new HashMap(3);
    }

    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if(null == action || 0 == action.length()){
            return true;
        }

        if(ACTION_LIST_TEMPLATE_TYPE.equals(action)){
            synchronized (callbackContextMap){
                if(!callbackContextMap.containsKey(action)){
                    callbackContextMap.put(action, callbackContext);
                }
            }

            startLoad(WHAT_LIST_TEMPLATE_TYPE, null);
        }

        else if(ACTION_LIST_TEMPLATE.equals(action)){
            synchronized (callbackContextMap){
                if(!callbackContextMap.containsKey(action)){
                    callbackContextMap.put(action, callbackContext);
                }
            }
            startLoad(WHAT_LIST_TEMPLATE, null);
        }

        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {

        Message msg;
        switch (what){
            default :
                return null;

            case WHAT_LIST_TEMPLATE_TYPE :
                msg = Message.obtain();

                break;

            case WHAT_LIST_TEMPLATE :
                StoryLogic logic = StoryLogic.instance();
                StoryTemplateEntity[] entities = logic.listStoryTemplate(getCurrentActivity().getApplicationContext(), null);
                CallbackContext callbackContext = callbackContextMap.get(ACTION_LIST_TEMPLATE);
                Gson gson = new Gson();
                String jsonStr = gson.toJson(entities);
                callbackContext.success(jsonStr);
                break;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != callbackContextMap){
            callbackContextMap.clear();
            callbackContextMap = null;
        }
    }
}
