package com.wisape.android.bean;

import com.wisape.android.widget.PhotoWallAdapter;

/**
 * Created by LeiGuoting on 18/6/15.
 */
public class AppPhotoInfo extends PhotoInfo implements PhotoWallAdapter.AppPhotoItemData {
    public static final short VIEW_TYPE_PHOTO = 0x01;
    public static final short VIEW_TYPE_CAMERA = 0x02;

    private int viewType = VIEW_TYPE_PHOTO;
    private boolean selected;

    public AppPhotoInfo(){}

    public AppPhotoInfo(int viewType){
        this.viewType = viewType;
    }

    public void setViewType(int viewType){
        this.viewType = viewType;
    }

    @Override
    public int getItemViewType() {
        return viewType;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
