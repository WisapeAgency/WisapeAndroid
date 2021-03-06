package com.wisape.android.cordova;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.wisape.android.activity.StoryTemplateActivity;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * 软键盘打开与关闭
 */
public class KeyboardPlugin extends AbsPlugin{
        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            Activity activity =  cordova.getActivity();
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (action.equals("show")) {
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                callbackContext.success();
                return true;
            }else if (action.equals("hide")){
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                callbackContext.success();
                return true;
            }
            return false;
        }
 }
