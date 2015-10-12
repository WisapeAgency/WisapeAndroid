package com.wisape.android.cordova;

import com.wisape.android.util.LogUtil;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Web端日志记录器
 */
public class LoggerPlugin extends AbsPlugin{

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("log")) {
            log(args.getString(0));
            return true;
        }
        return false;
    }

    /**
     * 记录日志
     * @param log
     */
    private void log(String log) {
        LogUtil.d(log);
        callbackContext.success();
    }
 }
