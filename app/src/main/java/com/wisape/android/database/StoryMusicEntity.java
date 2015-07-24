package com.wisape.android.database;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryMusicInfo;
import com.wisape.android.widget.StoryMusicAdapter;

import org.cubieline.lplayer.media.StreamPlugin;
import org.cubieline.lplayer.media.Track;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
@DatabaseTable(tableName = "_story_music")
public class StoryMusicEntity extends BaseEntity implements Parcelable, StoryMusicAdapter.StoryMusicDataInfo, Track {
    public static final int STATUS_NONE = 0;
    public static final int STATUS_DOWNLOADING = 0x01;

    @DatabaseField()
    public long serverId;
    @DatabaseField()
    public String name;
    @DatabaseField(dataType= DataType.STRING)
    public String music; //URL
    /**
     * Mapping with {@link StoryMusicTypeEntity#serverId}
     */
    @DatabaseField()
    public long type;
    @DatabaseField()
    public String recStatus;
    @DatabaseField(dataType= DataType.STRING)
    public String musicLocal;

    @DatabaseField()
    public int status;

    private int downloadProgress;

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
        entity.musicLocal = json.optString("musicLocal");
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

    public boolean equals(StoryMusicInfo info){
        return this.serverId == info.id &&
                this.name == info.music_name &&
                this.music == info.music_url &&
                this.type == info.type &&
                this.recStatus == info.rec_status;
    }

    public void update(StoryMusicInfo info){
        if(null == this.name || !this.name.equals(info.music_name)){
            this.name = info.music_name;
        }

        if(null == this.music || !this.music.equals(info.music_url)){
            this.music = info.music_url;
            this.musicLocal = "";
        }

        if(this.type != info.type){
            this.type = info.type;
        }

        if(null == this.recStatus || !this.recStatus.equals(info.rec_status)){
            this.recStatus = info.rec_status;
        }
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
        return (null == music || 0 == music.length()) ? null : Uri.parse(music);
    }

    @Override
    public Uri getMusicLocal() {
        return (null == musicLocal || 0 == musicLocal.length()) ? null : Uri.parse(musicLocal);
    }

    @Override
    public int getItemViewType() {
        return StoryMusicAdapter.VIEW_TYPE_MUSIC_ENTITY;
    }

    @Override
    public void setProgress(int progress) {
        this.downloadProgress = progress;
    }

    @Override
    public int getProgress() {
        return downloadProgress;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    /**  ********  Track Interface  ********  */
    @Override
    public int getType() {
        return 0;
    }

    @Override
    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public Track parseFromJson(JSONObject json) throws JSONException {
        return fromJsonObject(json);
    }

    @Override
    public long getTrackId() {
        return serverId;
    }

    @Override
    public String getTrackName() {
        return name;
    }

    @Override
    public String getDataSource() {
        return musicLocal;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public long getAlbumId() {
        return 0;
    }

    @Override
    public String getAlbumName() {
        return null;
    }

    @Override
    public String getAlbumIconPath() {
        return null;
    }

    @Override
    public String getAlbumIconUrl() {
        return null;
    }

    @Override
    public long getArtistId() {
        return 0;
    }

    @Override
    public String getArtistName() {
        return null;
    }

    @Override
    public String getArtistPath() {
        return null;
    }

    @Override
    public String getArtistUrl() {
        return null;
    }

    @Override
    public int getPluginCode() {
        return StreamPlugin.PLUGIN_CODE_DEFAULT;
    }
    /**  ********  Track Interface  End ********  */

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
        dest.writeString(this.musicLocal);
        dest.writeInt(this.status);
        dest.writeInt(this.downloadProgress);
    }

    public StoryMusicEntity() {
    }

    protected StoryMusicEntity(Parcel in) {
        this.serverId = in.readLong();
        this.name = in.readString();
        this.music = in.readString();
        this.type = in.readLong();
        this.recStatus = in.readString();
        this.musicLocal = in.readString();
        this.status = in.readInt();
        this.downloadProgress = in.readInt();
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
