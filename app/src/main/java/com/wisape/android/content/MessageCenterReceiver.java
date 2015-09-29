package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.activity.MessageCenterDetailActivity;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.util.Utils;

/**
 * 消息中心，消息接收处理
 * Created by hm on 2015/8/15.
 */
public class MessageCenterReceiver extends BroadcastReceiver {

    private static final String TAG = MessageCenterReceiver.class.getSimpleName();

    /**
     * 被挤下线
     */
    private static final int LOGIN_OUT_BY_OHTER = 0;

    /**
     * 消息类型的key
     */
    private static final String MESSAGE_TYPE_KEY = "type";

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

    public MessageCenterReceiver(BroadCastReciverListener broadcastReciveListener) {
        this.broadcastReciverListener = broadcastReciveListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"消息中心收到消息");
        if(destroyed){
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
        int typeKey = jsonObject.getInteger(MESSAGE_TYPE_KEY);

        if (LOGIN_OUT_BY_OHTER == typeKey) {
            Log.e(TAG,"强制下线");
            SignUpActivity.launch(context,"您在其他终端了!");
            return;
        }

        Utils.sendNotificatio(context, MessageCenterDetailActivity.class,jsonObject.getInteger(MESSAGE_ID),
                jsonObject.getString(MESSAGE_TITILE),
                jsonObject.getString(MESSAGE_SUBJECT));
        broadcastReciverListener.updateMsgCount();
    }

    public void destroy(){
        Log.e(TAG,"销毁系统消息广播接收器");
        destroyed = true;
        broadcastReciverListener = null;
    }


}
