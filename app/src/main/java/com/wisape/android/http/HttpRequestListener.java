package com.wisape.android.http;

import com.alibaba.fastjson.JSONObject;

/**
 * 网络回调接口
 * Created by huangmeng on 15/8/18.
 */
public interface HttpRequestListener {

    void onSuccess(JSONObject responseJson);

    void onError(String message);
}
