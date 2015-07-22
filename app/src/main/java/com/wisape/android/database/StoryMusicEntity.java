package com.wisape.android.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryMusicInfo;
import com.wisape.android.widget.StoryMusicAdapter;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
@DatabaseTable(tableName = "_story_music")
public class StoryMusicEntity extends BaseEntity implements Parcelable, StoryMusicAdapter.StoryMusicDataInfo {
    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String name;
    @DatabaseField()
    public String music; //URL
    /**
     * Mapping with {@link StoryMusicTypeEntity#serverId}
     */
    @DatabaseField()
    public long type;
    @DatabaseField()
    public String recStatus;

    public static StoryMusicEntity fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryMusicEntity entity = new StoryMusicEntity();
        entity.id = json.optLong("id");
        entity.createAt = json.optLong("createAt");
        entity.updateAt = json.optLong("updateAt");
        entity.reserved = json.optString("reserved");
        entity.reservedInt = json.optInt("reservedInt");
        entity.serverId = json.optLong("serverId");
        entity.name = json.optString("name");
        entity.music = json.optString("music");
        entity.type = json.optLong("type");
        entity.recStatus = json.optString("recStatus");
        return entity;
    }

    public static StoryMusicEntity transform(StoryMusicInfo info){
        if(null == info){
            return null;
        }

        StoryMusicEntity entity = new StoryMusicEntity();
        entity.serverId = info.id;
        entity.name = info.music_name;
        entity.music = info.music_url;
        entity.type = info.type;
        entity.recStatus = info.rec_status;
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
        info.rec_status = entity.recStatus;
        return info;
    }

    @Override
    public long getId() {
        return serverId;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getDownloadUrl() {
        return music;
    }

    @Override
    public int getItemViewType() {
        return StoryMusicAdapter.VIEW_TYPE_MUSIC_ENTITY;
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
        dest.writeLong(this.type);
        dest.writeString(this.recStatus);
    }

    public StoryMusicEntity() {
    }

    protected StoryMusicEntity(Parcel in) {
        this.serverId = in.readLong();
        this.name = in.readString();
        this.music = in.readString();
        this.type = in.readLong();
        this.recStatus = in.readString();
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
