package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 更新用户信息
 * Created by huangmeng on 15/9/26.
 */
public class UpdateUserInfoBroadcastReciver extends BroadcastReceiver {

    public static final String ACTION = "com.wisape.android.content.UpdateUserInfoBroadcastReciver";

    private volatile boolean isDestoyed;

    private UpdateUserInfoBoradcastReciverListener listener;

    public UpdateUserInfoBroadcastReciver(UpdateUserInfoBoradcastReciverListener broadcastReciverListener) {
        listener = broadcastReciverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isDestoyed) {
            listener.updateUserInfo();
        }
    }

    public void destory() {
        isDestoyed = true;
        listener = null;
    }

    public interface UpdateUserInfoBoradcastReciverListener {
        void updateUserInfo();
    }
}
