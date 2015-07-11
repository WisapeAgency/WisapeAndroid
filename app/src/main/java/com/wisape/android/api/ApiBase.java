package com.wisape.android.api;

import android.content.Context;
import android.util.Log;

import com.wisape.android.common.UserManager;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.ServerInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.Requester;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

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
            Object data = message.data;
            if(data instanceof JSONObject){
                info = onConvert((JSONObject) message.data);
            }else{
                info = onConvertError();
                message.status = Requester.ServerMessage.STATUS_LOCAL_OPT_JSON_FAILED;
            }
        }else{
            info = onConvertError();
            info.message = message.message;
        }
        info.status = message.status;
        Log.d("", "#convert ServerMessage:" + message.toString());
        message.recycle();
        return info;
    }

    protected abstract ServerInfo onConvert(JSONObject jsonObj);

    protected abstract ServerInfo onConvertError();

    protected ServerInfo[] convertArray(Requester.ServerMessage message){
        ServerInfo[] infoArray;
        if(message.succeed()){
            Object data = message.data;
            if(data instanceof JSONArray){
                infoArray = onConvertArray((JSONArray) message.data, message.status);
            }else{
                infoArray = onConvertArrayError();
            }
        }else{
            infoArray = onConvertArrayError();
        }
        Log.d("", "#convert ServerMessage:" + message.toString());
        message.recycle();
        return infoArray;
    }

    protected ServerInfo[] onConvertArray(JSONArray jsonArray, int status){
        throw new UnsupportedOperationException("");
    }

    protected ServerInfo[] onConvertArrayError(){
        throw new UnsupportedOperationException("");
    }
}

