package com.wisape.android.bean;

import android.os.Parcel;

import com.wisape.android.widget.PhotoWallsAdapter;

/**
 * Created by LeiGuoting on 18/6/15.
 */
public class AppPhotoInfo extends PhotoInfo implements PhotoWallsAdapter.AppPhotoItemData {
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.viewType);
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
    }

    protected AppPhotoInfo(Parcel in) {
        super(in);
        this.viewType = in.readInt();
        this.selected = in.readByte() != 0;
    }

    public static final Creator<AppPhotoInfo> CREATOR = new Creator<AppPhotoInfo>() {
        public AppPhotoInfo createFromParcel(Parcel source) {
            return new AppPhotoInfo(source);
        }

        public AppPhotoInfo[] newArray(int size) {
            return new AppPhotoInfo[size];
        }
    };
}
