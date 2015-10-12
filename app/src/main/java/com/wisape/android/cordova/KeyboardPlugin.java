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
        private CallbackContext callbackContext;

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            this.callbackContext = callbackContext;
            Activity activity =  cordova.getActivity();
            if (action.equals("show")) {
                InputMethodManager imm = (InputMethodManager)
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
                callbackContext.success();
                return true;
            }else if (action.equals("hide")){
                InputMethodManager imm = (InputMethodManager)
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!imm.isActive()) {
                    ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                }
                callbackContext.success();
                return true;
            }
            return false;
        }
 }
