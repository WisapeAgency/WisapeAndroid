package com.wisape.android.api;

import android.content.Context;
import android.util.Log;

import com.wisape.android.common.UserManager;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.ServerInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.Requester;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public abstract class ApiBase{

    protected final void setAccessToken(Context context, AttributeInfo attr){
        String accessToken = UserManager.instance().acquireAccessToken(context);
        Log.d("ApiBase", "#setAccessToken accessToken:" + accessToken);
        attr.setAccessToken(accessToken);
    }

    protected ServerInfo convert(Requester.ServerMessage message){
        ServerInfo info;
        if(message.succeed()){
            info = onConvert(message.data);
        }else{
            info = onConvertError();
            info.message = message.message;
        }
        info.status = message.status;
        Log.d("", "#convert ServerMessage:" + message.toString());
        message.recycle();
        return info;
    }

    protected abstract ServerInfo onConvert(JSONObject json);

    protected abstract ServerInfo onConvertError();
}

