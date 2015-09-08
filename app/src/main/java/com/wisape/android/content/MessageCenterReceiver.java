package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.activity.SignUpActivity;

/**
 * 消息中心，消息接收处理
 * Created by hm on 2015/8/15.
 */
public class MessageCenterReceiver extends BroadcastReceiver{

    private static final String TAG = MessageCenterReceiver.class.getSimpleName();

    /**
     *被挤下线
     */
    private static final int LOGIN_OUT_BY_OHTER = 0;

    /**
     * 运营消息
     */
    private static final int OPERATION_MESSAGE = 2;
    /**
     * 系统消息
     */
    private static final int SYSTEM_MESSAGE = 1;
    /**
     * 活动中心消息
     */
    private static final int ACTIVE_MESSAGE = 3;


    private static final String MESSAGE_RECEIVER_ACTION_OPERATE = "com.wisape.android.content.MessageCenterReceiver";

    /**
     * 消息类型的key
     */
    private static final String MESSAGE_TYPE_KEY = "type";

    /**
     * 获取消息内容的key
     */
    private static final String DATA_KEY = "com.parse.Data";

    private OnMessageReciveListener mMessageReciveListener;

    public MessageCenterReceiver(OnMessageReciveListener onMessageReciveListener){
        this.mMessageReciveListener = onMessageReciveListener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if(null != context && MESSAGE_RECEIVER_ACTION_OPERATE.equals(intent.getAction())){
            JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
            Log.e(TAG, "#messageReciver:" + jsonObject.toString());
            int typeKey = jsonObject.getInteger(MESSAGE_TYPE_KEY);

            if(LOGIN_OUT_BY_OHTER == typeKey){
                SignUpActivity.launch(context);
            }

            //运营消息
            if(OPERATION_MESSAGE == typeKey){
                Log.e(TAG, "发送运营消息");
                mMessageReciveListener.updateMessageCount(context, intent);
            }

            //活动中心消息
            if (ACTIVE_MESSAGE == typeKey){
                Log.e(TAG, "发送活动中心消息");
                mMessageReciveListener.updateActiveCount(context, intent);
            }

            //系统消息
            if(SYSTEM_MESSAGE == typeKey){
                Log.e(TAG, "发送系统消息");
                mMessageReciveListener.updateMessageCount(context, intent);
            }
        }
    }

    public interface OnMessageReciveListener{
        void updateMessageCount(Context context,Intent intent);
        void updateActiveCount(Context context,Intent intent);
    }

}
