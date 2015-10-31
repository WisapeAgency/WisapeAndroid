package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.activity.MessageCenterDetailActivity;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;

/**
 * Created by huangmeng on 15/10/31.
 */
public class ClearNumberReciver extends BroadcastReceiver {

    private volatile boolean destroyed;
    private ClearNumberListener clearNumberListener;

    public static final String CLEAR_ACTION = "com.wisape.android.content.ClearNumberReciver";

    public ClearNumberReciver(ClearNumberListener clearNumberListener) {
        this.clearNumberListener = clearNumberListener;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        clearNumberListener.clearNumber();
    }

    public void destroy() {
        LogUtil.d("销毁消息中心广播接收器");
        destroyed = true;
        clearNumberListener = null;
    }

    public interface ClearNumberListener{
        void clearNumber();
    }
}
