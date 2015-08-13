package com.wisape.android.cordova;

import android.widget.Toast;

import com.wisape.android.activity.StoryTemplateActivity;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;


public class TestPlugin extends AbsPlugin{

        private String infos;
        private CallbackContext callbackContext;

        @Override
        public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
            this.callbackContext = callbackContext;
            if (action.equals("test")) {
                // 获取JS传递的参数
                if(null != args && 0 != args.length()){
                    for (int i = 0; i < args.length(); i++) {
                        System.out.println(args.getString(i));
                    }
                }
                this.function();
                return true;
            }
            return false;
        }

        // 方法执行体
        private void function() {
            // 传递返回值 给js方法
//            getCurrentActivity().
            callbackContext.success("success success!");
            Toast.makeText(cordova.getActivity(), "success success", Toast.LENGTH_LONG).show();
            ((StoryTemplateActivity)cordova.getActivity()).invokeJavascriptTest();
        }
 }
