package com.wisape.android.http;

import com.alibaba.fastjson.JSONObject;

/**
 * 默认回调接口
 * Created by huangmeng on 15/8/18.
 */
public class DefaultHttpRequestListener implements HttpRequestListener {

    /**
     * 网络请求成功
     */
    public static final int STATUS_SUCCESS = 1;
    /**
     * 服务器返回码
     */
    public static final String SERVER_CODE = "success";
    /**
     * 服务器处理失败时返回的消息
     */
    public static final String SERVER_MESSAGE = "message";

    public static final String SERVER_DATA = "data";

    @Override
    public void onError(String message) {
        //TODO 显示提示信息
    }

    @Override
    public void onSuccess(JSONObject responseJson) {
        if(STATUS_SUCCESS != responseJson.getInteger(SERVER_CODE)){
            onError(responseJson.getString(SERVER_MESSAGE));
        }
        onReqeustSuccess(responseJson.getString(SERVER_DATA));
    }

    /**
     * 子类实现进行数据处理
     * @param data  返回的数据
     */
    public void onReqeustSuccess(String data){
    }
}
