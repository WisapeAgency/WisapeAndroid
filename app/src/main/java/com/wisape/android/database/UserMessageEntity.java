package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.UserMessageInfo;

/**
 * Created by LeiGuoting on 14/7/15.
 */
@DatabaseTable(tableName = "_user_message")
public class UserMessageEntity extends BaseEntity implements Parcelable{

    @DatabaseField()
    public long serverId;

    @DatabaseField()
    public String message;

    @DatabaseField()
    public int status;

    public static UserMessageEntity transform(UserMessageInfo info){
        if(null == info){
            return null;
        }

        UserMessageEntity entity = new UserMessageEntity();
        entity.serverId = info.id;
        entity.message = info.message;
        entity.status = LOCAL_STATUS_NEW;
        return entity;
    }

    public static UserMessageInfo convert(UserMessageEntity entity){
        if(null == entity){
            return null;
        }

        UserMessageInfo info = new UserMessageInfo();
        info.id = entity.serverId;
        info.message = entity.message;
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.serverId);
        dest.writeString(this.message);
        dest.writeInt(this.status);
    }

    public UserMessageEntity() {
    }

    protected UserMessageEntity(Parcel in) {
        this.serverId = in.readLong();
        this.message = in.readString();
        this.status = in.readInt();
    }

    public static final Creator<UserMessageEntity> CREATOR = new Creator<UserMessageEntity>() {
        public UserMessageEntity createFromParcel(Parcel source) {
            return new UserMessageEntity(source);
        }

        public UserMessageEntity[] newArray(int size) {
            return new UserMessageEntity[size];
        }
    };
}
