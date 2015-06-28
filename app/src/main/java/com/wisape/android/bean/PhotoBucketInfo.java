package com.wisape.android.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoBucketInfo implements Parcelable, Cloneable{
    public long id;
    public String displayName;
    public int childrenCount;
    public String thumbData;

    @Override
    public PhotoBucketInfo clone() throws CloneNotSupportedException {
        return (PhotoBucketInfo)super.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("{\"hashCode\":").append(hashCode());
        builder.append(", \"id\":").append(id);
        builder.append(", \"displayName\":").append(displayName);
        builder.append(", \"childrenCount\":").append(childrenCount);
        builder.append(", \"thumbData\":").append(thumbData);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof PhotoBucketInfo)){
            return false;
        }

        PhotoBucketInfo newBucket = (PhotoBucketInfo) obj;
        return newBucket.hashCode() == this.hashCode() || newBucket.id == this.id;
    }

    public PhotoBucketInfo() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.displayName);
        dest.writeInt(this.childrenCount);
        dest.writeString(this.thumbData);
    }

    protected PhotoBucketInfo(Parcel in) {
        this.id = in.readLong();
        this.displayName = in.readString();
        this.childrenCount = in.readInt();
        this.thumbData = in.readString();
    }

}
