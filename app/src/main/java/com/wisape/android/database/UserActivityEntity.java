package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.UserActivityInfo;

/**
 * Created by LeiGuoting on 14/7/15.
 */
@DatabaseTable(tableName = "_user_activity")
public class UserActivityEntity extends BaseEntity implements Parcelable {
    public static final int LOCAL_STATUS_DELETE = 0x03;

    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String title;
    @DatabaseField()
    public String bgImg;
    @DatabaseField()
    public String url;
    @DatabaseField()
    public String recStatus;
    @DatabaseField()
    public long startAt;
    @DatabaseField()
    public long endAt;
    @DatabaseField()
    public String countryIso;
    @DatabaseField()
    public int status;

    public static UserActivityEntity transform(UserActivityInfo info){
        if(null == info){
            return null;
        }

        UserActivityEntity entity = new UserActivityEntity();
        entity.serverId = info.id;
        entity.title = info.title;
        entity.bgImg = info.bg_img;
        entity.url = info.url;
        entity.recStatus = info.rec_status;
        entity.startAt = info.start_time;
        entity.endAt = info.end_time;
        entity.countryIso = info.country;
        entity.status = LOCAL_STATUS_NEW;
        return entity;
    }

    public static UserActivityInfo convert(UserActivityEntity entity){
        if(null == entity){
            return null;
        }

        UserActivityInfo info = new UserActivityInfo();
        info.id = entity.serverId;
        info.title = entity.title;
        info.bg_img = entity.bgImg;
        info.url = entity.url;
        info.rec_status = entity.recStatus;
        info.start_time = entity.startAt;
        info.end_time = entity.endAt;
        info.country = entity.countryIso;
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.serverId);
        dest.writeString(this.title);
        dest.writeString(this.bgImg);
        dest.writeString(this.url);
        dest.writeString(this.recStatus);
        dest.writeLong(this.startAt);
        dest.writeLong(this.endAt);
        dest.writeString(this.countryIso);
        dest.writeInt(this.status);
    }

    public UserActivityEntity() {
    }

    protected UserActivityEntity(Parcel in) {
        this.serverId = in.readLong();
        this.title = in.readString();
        this.bgImg = in.readString();
        this.url = in.readString();
        this.recStatus = in.readString();
        this.startAt = in.readLong();
        this.endAt = in.readLong();
        this.countryIso = in.readString();
        this.status = in.readInt();
    }

    public static final Creator<UserActivityEntity> CREATOR = new Creator<UserActivityEntity>() {
        public UserActivityEntity createFromParcel(Parcel source) {
            return new UserActivityEntity(source);
        }

        public UserActivityEntity[] newArray(int size) {
            return new UserActivityEntity[size];
        }
    };
}
