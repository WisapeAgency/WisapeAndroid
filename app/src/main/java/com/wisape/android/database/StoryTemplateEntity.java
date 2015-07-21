package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryTemplateInfo;

/**
 * Created by LeiGuoting on 15/7/15.
 */
@DatabaseTable(tableName = "_story_template")
public class StoryTemplateEntity extends BaseEntity implements Parcelable {

    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String name;
    @DatabaseField()
    public String thumb;       //URL
    @DatabaseField()
    public String description;
    @DatabaseField()
    public String template;    //URL
    @DatabaseField()
    public String recStatus;
    @DatabaseField()
    public long type;
    @DatabaseField()
    public int order;
    @DatabaseField()
    public String orderType;

    public static StoryTemplateEntity transform(StoryTemplateInfo info){
        if(null == info){
            return null;
        }

        StoryTemplateEntity entity = new StoryTemplateEntity();
        entity.serverId = info.id;
        entity.name = info.temp_name;
        entity.thumb = info.temp_img;
        entity.description = info.temp_description;
        entity.template = info.temp_url;
        entity.recStatus = info.rec_status;
        entity.type = info.type;
        entity.order = info.order;
        entity.orderType = info.order_type;
        return entity;
    }

    public static StoryTemplateInfo convert(StoryTemplateEntity entity){
        if(null == entity){
            return null;
        }

        StoryTemplateInfo info = new StoryTemplateInfo();
        info.id = entity.serverId;
        info.temp_name = entity.name;
        info.temp_description = entity.description;
        info.temp_img = entity.thumb;
        info.temp_url = entity.template;
        info.rec_status = entity.recStatus;
        info.type = entity.type;
        info.order = entity.order;
        info.order_type = entity.orderType;
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.serverId);
        dest.writeString(this.name);
        dest.writeString(this.thumb);
        dest.writeString(this.description);
        dest.writeString(this.template);
        dest.writeString(this.recStatus);
        dest.writeLong(this.type);
        dest.writeInt(this.order);
        dest.writeString(this.orderType);
    }

    public StoryTemplateEntity() {
    }

    protected StoryTemplateEntity(Parcel in) {
        this.serverId = in.readLong();
        this.name = in.readString();
        this.thumb = in.readString();
        this.description = in.readString();
        this.template = in.readString();
        this.recStatus = in.readString();
        this.type = in.readLong();
        this.order = in.readInt();
        this.orderType = in.readString();
    }

    public static final Creator<StoryTemplateEntity> CREATOR = new Creator<StoryTemplateEntity>() {
        public StoryTemplateEntity createFromParcel(Parcel source) {
            return new StoryTemplateEntity(source);
        }

        public StoryTemplateEntity[] newArray(int size) {
            return new StoryTemplateEntity[size];
        }
    };
}
