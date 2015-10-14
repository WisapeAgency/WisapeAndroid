package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.MessageCenterDetailActivity;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.util.Utils;

/**
 *数据同步
 */
public class DataSynchronizerReceiver extends BroadcastReceiver {

    private static final String TAG = DataSynchronizerReceiver.class.getSimpleName();

//    private static final int MESSAGE_TYPE_TEMPLATE = 4;
//
//    private static final int MESSAGE_TYPE_FONT = 5;

    /**
     * 消息类型的key
     */
    private static final String MESSAGE_TYPE_KEY = "type";

    /**
     * 获取消息内容的key
     */
    private static final String DATA_KEY = "com.parse.Data";

    private volatile boolean destroyed;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"收到数据同步消息");
        if(destroyed){
            return;
        }
//        JSONObject jsonObject = JSONObject.parseObject(intent.getExtras().getString(DATA_KEY));
//        int typeKey = jsonObject.getInteger(MESSAGE_TYPE_KEY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DataSynchronizer.getInstance().synchronous(WisapeApplication.getInstance().getApplicationContext());//
                }catch (Exception e){
                    Log.e(TAG,"同步数据失败:" + e.getMessage());
                }
            }
        }).start();
    }

    public void destroy(){
        Log.e(TAG,"销毁广播接收器");
        destroyed = true;
    }


}
