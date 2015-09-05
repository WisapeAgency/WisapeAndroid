package com.wisape.android.widget;
/**
 * recycleView列表项以及列表中的子view点击监听
 * Created by huangmeng on 15/8/26.
 */
public interface OnRecycleViewClickListener {

    /**
     * 列表项点击事件
     * @param storyId 单个实体ID
     */
    void onItemClick(long storyId);

    /**
     * 列表项中的子view点击
     * @param storyId 单个实体ID
     */
    void onItemSubViewClick(long storyId);
}