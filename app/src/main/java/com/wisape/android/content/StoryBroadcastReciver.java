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

    public static final String STORY_ACTION = "com.wisape.android.content.StoryBroadcastReciver";
    public static final String EXTRAS_TYPE = "story_type";
    public static final String TYPE_UPDATE_STORY = "update_story";

    private volatile boolean isDestoyed;
    private StoryBroadcastReciverListener listener;

    public StoryBroadcastReciver(StoryBroadcastReciverListener broadcastReciverListener){
        listener = broadcastReciverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!isDestoyed){
            listener.storyStateChange(1);
        }
    }

    public void destory(){
        isDestoyed = true;
        listener = null;
    }

    /**
     * story广播接收监听器
     */
    public interface StoryBroadcastReciverListener{
        void storyStateChange(int type);
    }
}
