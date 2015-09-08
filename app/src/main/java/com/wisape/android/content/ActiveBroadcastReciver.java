package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.util.Utils;

/**
 * 活动消息广播接收器
 * Created by huangmeng on 15/9/8.
 */
public class ActiveBroadcastReciver extends BroadcastReceiver {

    private static final String TAG = ActiveBroadcastReciver.class.getSimpleName();

    /**
     * 消息ID
     */
    protected static final String MESSAGE_ID = "id";

    /**
     *  消息标题
     */
    protected static final String MESSAGE_TITILE = "message_title";

    /**
     * 消息简介
     */
    protected static final String MESSAGE_SUBJECT = "message_subject";

    /**
     * 获取消息内容的key
     */
    private static final String DATA_KEY = "com.parse.Data";

    private volatile boolean destroyed;
    private BroadCastReciverListener broadcastReciverListener;

    public ActiveBroadcastReciver(BroadCastReciverListener broadCastReciverListener){
        this.broadcastReciverListener = broadCastReciverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"收到新的活动消息");
        if(!destroyed){
            JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
            Utils.sendNotificatio(context, MainActivity.class, jsonObject.getInteger(MESSAGE_ID),
                    jsonObject.getString(MESSAGE_TITILE),
                    jsonObject.getString(MESSAGE_SUBJECT));
            broadcastReciverListener.updateMsgCount();
        }
    }
    
    public void destroy(){
        Log.e(TAG,"销毁活动广播接收器");
        destroyed = true;
        broadcastReciverListener = null;
    }
}
