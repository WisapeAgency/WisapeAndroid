package com.wisape.android.content;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.activity.MessageCenterDetailActivity;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;

import java.util.List;

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
     * 消息标题
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
        JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
        LogUtil.d("收到推送消息:" + jsonObject.toJSONString());
        if (destroyed) {
            return;
        }
        int typeKey = jsonObject.getInteger(MESSAGE_TYPE_KEY);

        if (LOGIN_OUT_BY_OHTER == typeKey) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (runningTaskInfos != null) {
                runningTaskInfos.clear();
            }
//            SignUpActivity.launch(context, );
            return;
        }
        Utils.sendNotificatio(context, MessageCenterDetailActivity.class, jsonObject.getInteger(MESSAGE_ID),
                jsonObject.getString(MESSAGE_TITILE),
                jsonObject.getString(MESSAGE_SUBJECT));
        broadcastReciverListener.updateMsgCount();
    }

    public void destroy() {
        LogUtil.d("销毁消息中心广播接收器");
        destroyed = true;
        broadcastReciverListener = null;
    }


}
