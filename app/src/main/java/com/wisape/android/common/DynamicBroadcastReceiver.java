package com.wisape.android.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by LeiGuoting on 8/7/15.
 */
public class DynamicBroadcastReceiver extends BroadcastReceiver {
    private volatile boolean destroyed;
    private OnDynamicBroadcastReceiverListener listener;

    public DynamicBroadcastReceiver(OnDynamicBroadcastReceiverListener onDynamicBroadcastReceiverListener){
        if(null == onDynamicBroadcastReceiverListener){
            throw new IllegalArgumentException("OnDynamicBroadcastReceiverListener can not be null");
        }
        listener = onDynamicBroadcastReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(destroyed){
            return;
        }

        listener.onReceiveBroadcast(context, intent);
    }

    public void destroy(){
        destroyed = true;
        listener = null;
    }

    public interface OnDynamicBroadcastReceiverListener{
        void onReceiveBroadcast(Context context, Intent intent);
    }
}
