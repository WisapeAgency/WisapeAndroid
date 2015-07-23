package com.wisape.android.database;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryMusicTypeInfo;
import com.wisape.android.widget.StoryMusicAdapter;

/**
 * Created by tony on 2015/7/22.
 */
@DatabaseTable(tableName = "_story_music_type")
public class StoryMusicTypeEntity extends BaseEntity implements Parcelable, StoryMusicAdapter.StoryMusicDataInfo{

    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String name;
    @DatabaseField()
    public int order;

    public static StoryMusicTypeEntity transform(StoryMusicTypeInfo info){
        if(null == info){
            return null;
        }

        StoryMusicTypeEntity entity = new StoryMusicTypeEntity();
        entity.serverId = info.id;
        entity.name = info.name;
        entity.order = info.order;
        return entity;
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
    public Uri getDownloadUrl() {
        return null;
    }

    @Override
    public Uri getMusicLocal() {
        return null;
    }

    @Override
    public void setProgress(int progress) {}

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public void setUiStatus(int status) {}

    @Override
    public int getUiStatus() {
        return 0;
    }

    @Override
    public int getItemViewType() {
        return StoryMusicAdapter.VIEW_TYPE_MUSIC_TYPE;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.serverId);
        dest.writeString(this.name);
        dest.writeInt(this.order);
    }

    public StoryMusicTypeEntity() {
    }

    protected StoryMusicTypeEntity(Parcel in) {
        this.serverId = in.readLong();
        this.name = in.readString();
        this.order = in.readInt();
    }

    public static final Creator<StoryMusicTypeEntity> CREATOR = new Creator<StoryMusicTypeEntity>() {
        public StoryMusicTypeEntity createFromParcel(Parcel source) {
            return new StoryMusicTypeEntity(source);
        }

        public StoryMusicTypeEntity[] newArray(int size) {
            return new StoryMusicTypeEntity[size];
        }
    };
}
