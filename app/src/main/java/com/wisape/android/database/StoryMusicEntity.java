package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryMusicInfo;

/**
 * Created by LeiGuoting on 15/7/15.
 */
@DatabaseTable(tableName = "_story_music")
public class StoryMusicEntity extends BaseEntity implements Parcelable{
    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String name;
    @DatabaseField()
    public String music; //URL

    public static StoryMusicEntity transform(StoryMusicInfo info){
        if(null == info){
            return null;
        }

        StoryMusicEntity entity = new StoryMusicEntity();
        entity.serverId = info.id;
        entity.name = info.music_name;
        entity.music = info.music_url;
        return entity;
    }

    public static StoryMusicInfo convert(StoryMusicEntity entity){
        if(null == entity){
            return null;
        }

        StoryMusicInfo info = new StoryMusicInfo();
        info.id = entity.serverId;
        info.music_name = entity.name;
        info.music_url = entity.music;
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
        dest.writeString(this.music);
    }

    public StoryMusicEntity() {
    }

    protected StoryMusicEntity(Parcel in) {
        this.serverId = in.readLong();
        this.name = in.readString();
        this.music = in.readString();
    }

    public static final Creator<StoryMusicEntity> CREATOR = new Creator<StoryMusicEntity>() {
        public StoryMusicEntity createFromParcel(Parcel source) {
            return new StoryMusicEntity(source);
        }

        public StoryMusicEntity[] newArray(int size) {
            return new StoryMusicEntity[size];
        }
    };
}
