package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.wisape.android.model.StoryTemplateInfo;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class StoryTemplateEntity extends BaseEntity implements Parcelable {

    public long serverId;
    public String name;
    public String thumb;       //URL
    public String description;
    public String template;    //URL
    public String recStatus;

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
    }

    public static final Parcelable.Creator<StoryTemplateEntity> CREATOR = new Parcelable.Creator<StoryTemplateEntity>() {
        public StoryTemplateEntity createFromParcel(Parcel source) {
            return new StoryTemplateEntity(source);
        }

        public StoryTemplateEntity[] newArray(int size) {
            return new StoryTemplateEntity[size];
        }
    };
}
