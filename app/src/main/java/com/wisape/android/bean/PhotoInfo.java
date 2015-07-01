package com.wisape.android.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoInfo implements Parcelable, Cloneable{
    public long id;
    public long bucketId;
    public String displayName;
    public String bucketDisplayName;
    public long dateTakenInMills;
    public String data;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("{\"hashCode\":").append(hashCode());
        builder.append(", \"id\":").append(id);
        builder.append(", \"bucketId\":").append(bucketId);
        builder.append(", \"displayName\":").append(displayName);
        builder.append(", \"bucketDisplayName\":").append(bucketDisplayName);
        builder.append(", \"dateTakenInMills\":").append(dateTakenInMills);
        builder.append(", \"data\":").append(data);
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(! (obj instanceof PhotoInfo)){
            return false;
        }

        PhotoInfo newPhoto = (PhotoInfo) obj;
        return newPhoto.hashCode() == this.hashCode() || newPhoto.id == this.id;
    }

    public PhotoInfo() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.bucketId);
        dest.writeString(this.displayName);
        dest.writeString(this.bucketDisplayName);
        dest.writeLong(this.dateTakenInMills);
        dest.writeString(this.data);
    }

    protected PhotoInfo(Parcel in) {
        this.id = in.readLong();
        this.bucketId = in.readLong();
        this.displayName = in.readString();
        this.bucketDisplayName = in.readString();
        this.dateTakenInMills = in.readLong();
        this.data = in.readString();
    }

    public static final Creator<PhotoInfo> CREATOR = new Creator<PhotoInfo>() {
        public PhotoInfo createFromParcel(Parcel source) {
            return new PhotoInfo(source);
        }

        public PhotoInfo[] newArray(int size) {
            return new PhotoInfo[size];
        }
    };
}
