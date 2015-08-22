package com.wisape.android.content;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.Message.ActiveMessage;
import com.wisape.android.Message.OperateMessage;
import com.wisape.android.Message.SystemMessage;
import com.wisape.android.R;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.MessageCenterDetailActivity;

import de.greenrobot.event.EventBus;

/**
 * 消息中心，消息接收处理
 * Created by hm on 2015/8/15.
 */
public class MessageCenterReceiver extends BroadcastReceiver{

    private static final String TAG = MessageCenterReceiver.class.getSimpleName();

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
     *  消息标题
     */
    private static final String MESSAGE_TITILE = "message_title";

    /**
     * 消息简介
     */
    private static final String MESSAGE_SUBJECT = "message_subject";

    /**
     * 消息ID
     */
    private static final String MESSAGE_ID = "message_id";
    /**
     * 获取消息内容的key
     */
    private static final String DATA_KEY = "com.parse.Data";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(null != context && MESSAGE_RECEIVER_ACTION_OPERATE.equals(intent.getAction())){
            JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
            Log.e(TAG, "#messageReciver:" + jsonObject.toString());

            //运营消息
            if(OPERATION_MESSAGE == jsonObject.getInteger(MESSAGE_TYPE_KEY)){
                Log.e(TAG,"发送运营消息");
                EventBus.getDefault().post(new OperateMessage());
                sendNotifacation(context, jsonObject, OPERATION_MESSAGE);
            }

            //活动中心消息
            if (ACTIVE_MESSAGE == jsonObject.getInteger(MESSAGE_TYPE_KEY)){
                Log.e(TAG,"发送活动中心消息");
                EventBus.getDefault().post(new ActiveMessage());
                sendNotifacation(context, jsonObject, ACTIVE_MESSAGE);
            }

            //系统消息
            if(SYSTEM_MESSAGE == jsonObject.getInteger(MESSAGE_TYPE_KEY)){
                Log.e(TAG,"发送系统消息");
                EventBus.getDefault().post(new SystemMessage());
                sendNotifacation(context,jsonObject, SYSTEM_MESSAGE);
            }
        }
    }

    private void sendNotifacation(Context context,JSONObject jsonObject,int messageType){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = getIntent(context,messageType);
        intent.putExtra(MessageCenterDetailActivity.MESSAGE_ID, jsonObject.getIntValue(MESSAGE_ID));
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notify = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.logo)
                .setTicker(jsonObject.getString(MESSAGE_TITILE))
                .setContentTitle(jsonObject.getString(MESSAGE_TITILE))
                .setContentText(jsonObject.getString(MESSAGE_SUBJECT))
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .getNotification();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.defaults = Notification.DEFAULT_ALL;
        manager.notify(1, notify);
    }

    private Intent getIntent(Context context,int messageTeype){
        Intent intent = null;
        if(SYSTEM_MESSAGE == (messageTeype) || OPERATION_MESSAGE == messageTeype){
            intent = new Intent(context,MessageCenterDetailActivity.class);
        }
        if(ACTIVE_MESSAGE == messageTeype){
             intent  = new Intent(context, MainActivity.class);
        }
        return intent;
    }
}
