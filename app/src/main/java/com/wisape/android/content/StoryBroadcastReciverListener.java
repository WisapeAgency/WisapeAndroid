package com.wisape.android.content;

import android.content.Intent;

import com.wisape.android.database.StoryEntity;
import com.wisape.android.model.StoryInfo;

/**
 * story广播接收器监听器
 * Created by huangmeng on 15/9/19.
 */
public interface StoryBroadcastReciverListener {

    /*发布story*/
    public static final int TYPE_PUBLISH_STORY = 1;

    public static final int UPDATE_STORY_SETTING = 2;

    public static final int ADD_JUKE_STORY = 6;

    /*修改story 包含修改*/
    public static final int TYPE_UPDATE_STORY = 3;

    /*新建story*/
    public static final int TYPE_ADD_STORY = 4;

    void storyStateChange(int type);

}
