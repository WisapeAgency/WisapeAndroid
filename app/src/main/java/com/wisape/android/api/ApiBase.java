package com.wisape.android.api;

import android.content.Context;
import android.os.UserManager;
import android.util.Log;

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
//        String accessToken = UserManager.instance().acquireAccessToken(context);
//        Log.d("ApiBase", "#setAccessToken accessToken:" + "");
        attr.setAccessToken("");
    }

    protected ServerInfo convert(int what, Requester.ServerMessage message){
        ServerInfo info;
        if(message.succeed()){
            Object data = message.data;
            if(data instanceof JSONObject){
                info = onConvert(what, (JSONObject) message.data);
            }else{
                info = onConvertError(what);
                message.status = Requester.ServerMessage.STATUS_LOCAL_OPT_JSON_FAILED;
            }
        }else{
            info = onConvertError(what);
            info.message = message.message;
        }
        info.status = message.status;
        Log.d("", "#convert ServerMessage:" + message.toString());
        message.recycle();
        return info;
    }

    protected ServerInfo convert(Requester.ServerMessage message){
        return convert(0, message);
    }

    protected abstract ServerInfo onConvert(int what, JSONObject jsonObj);

    protected abstract ServerInfo onConvertError(int what);


    protected ServerInfo[] convertArray(int what, Requester.ServerMessage message){
        ServerInfo[] infoArray;
        if(message.succeed()){
            Object data = message.data;
            if(data instanceof JSONArray){
                infoArray = onConvertArray(what, (JSONArray) message.data, message.status);
            }else{
                infoArray = onConvertArrayError(what);
            }
        }else{
            infoArray = onConvertArrayError(what);
        }
        Log.d("", "#convert ServerMessage:" + message.toString());
        message.recycle();
        return infoArray;
    }

    protected ServerInfo[] convertArray(Requester.ServerMessage message){
        return convertArray(0, message);
    }

    protected ServerInfo[] onConvertArray(int what, JSONArray jsonArray, int status){
        throw new UnsupportedOperationException("");
    }

    protected ServerInfo[] onConvertArrayError(int what){
        throw new UnsupportedOperationException("");
    }
}

