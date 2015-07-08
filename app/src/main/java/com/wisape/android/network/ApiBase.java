package com.wisape.android.network;

import android.content.Context;
import android.util.Log;

import com.wisape.android.common.UserManager;
import com.wisape.android.model.AttributeInfo;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public abstract class ApiBase{

    protected final void setAccessToken(Context context, AttributeInfo attr){
        String accessToken = UserManager.instance().acquireAccessToken(context);
        Log.d("ApiBase", "#setAccessToken accessToken:" + accessToken);
        attr.setAccessToken(accessToken);
    }
}
