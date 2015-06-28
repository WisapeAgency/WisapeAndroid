package com.wisape.android.bean;

import android.os.Parcel;

import com.wisape.android.widget.PhotoBucketsAdapter;

/**
 * Created by LeiGuoting on 28/6/15.
 */
public class AppPhotoBucketInfo extends PhotoBucketInfo implements PhotoBucketsAdapter.AppBucketItemData {
    private boolean selected;

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
        dest.writeByte(selected ? (byte) 1 : (byte) 0);
    }

    public AppPhotoBucketInfo() {
    }

    protected AppPhotoBucketInfo(Parcel in) {
        super(in);
        this.selected = in.readByte() != 0;
    }

    public static final Creator<AppPhotoBucketInfo> CREATOR = new Creator<AppPhotoBucketInfo>() {
        public AppPhotoBucketInfo createFromParcel(Parcel source) {
            return new AppPhotoBucketInfo(source);
        }

        public AppPhotoBucketInfo[] newArray(int size) {
            return new AppPhotoBucketInfo[size];
        }
    };
}
