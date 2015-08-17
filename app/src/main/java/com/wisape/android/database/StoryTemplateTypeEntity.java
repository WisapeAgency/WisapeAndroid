package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.wisape.android.model.StoryTemplateTypeInfo;

/**
 * Created by tony on 2015/7/20.
 */
public class StoryTemplateTypeEntity extends BaseEntity implements Parcelable{
    public int serverId;
    public String name;
    public int order;

    public static StoryTemplateTypeEntity transform(StoryTemplateTypeInfo info){
        if(null == info){
            return null;
        }

        StoryTemplateTypeEntity entity = new StoryTemplateTypeEntity();
        entity.serverId = info.id;
        entity.name = info.name;
        entity.order = info.order;
        return entity;
    }

    public static StoryTemplateTypeInfo convert(StoryTemplateTypeEntity entity){
        if(null == entity){
            return null;
        }

        StoryTemplateTypeInfo info = new StoryTemplateTypeInfo();
        info.id = entity.serverId;
        info.name = entity.name;
        info.order = entity.order;
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.serverId);
        dest.writeString(this.name);
        dest.writeInt(this.order);
    }

    public StoryTemplateTypeEntity() {
    }

    protected StoryTemplateTypeEntity(Parcel in) {
        this.serverId = in.readInt();
        this.name = in.readString();
        this.order = in.readInt();
    }

    public static final Creator<StoryTemplateTypeEntity> CREATOR = new Creator<StoryTemplateTypeEntity>() {
        public StoryTemplateTypeEntity createFromParcel(Parcel source) {
            return new StoryTemplateTypeEntity(source);
        }

        public StoryTemplateTypeEntity[] newArray(int size) {
            return new StoryTemplateTypeEntity[size];
        }
    };
}
