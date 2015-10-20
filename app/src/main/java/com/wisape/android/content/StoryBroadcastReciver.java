package com.wisape.android.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wisape.android.model.StoryInfo;
import com.wisape.android.util.LogUtil;

/**
 * story设置广播接收器
 * Created by huangmeng on 15/9/19.
 */
public class StoryBroadcastReciver extends BroadcastReceiver {

    private static final String TAG = StoryBroadcastReciver.class.getSimpleName();

    public static final String STORY_ACTION = "com.wisape.android.content.StoryBroadcastReciver";
    public static final String EXTRAS_TYPE = "story_type";
    public static final String EXRAS_DATA = "storyinfo";

    private volatile boolean isDestoyed;

    private StoryBroadcastReciverListener listener;

    public StoryBroadcastReciver(StoryBroadcastReciverListener broadcastReciverListener){
        listener = broadcastReciverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d("收到推送消息更新首页story信息" );
        if(!isDestoyed){
            listener.storyStateChange(intent.getExtras().getInt(EXTRAS_TYPE));
        }
    }

    public void destory(){
        LogUtil.d("销毁更新首页story广播接收器");
        isDestoyed = true;
        listener = null;
    }
}
