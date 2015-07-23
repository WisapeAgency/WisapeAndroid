package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class StoryMusicInfo extends ServerInfo{
    /**
     "id": "2",
     "music_name": "rock and roll",
     "music_url": "http://loc.wis/uploads/2015/var/www/html/wis/uploads/yyzd.mp3",
     "type": "2",
     "rec_status": "A"
     */

    public long id;
    public String music_name;
    public String music_url;
    public long type;
    public String rec_status;

    public static StoryMusicInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryMusicInfo music = new StoryMusicInfo();
        music.id = json.optLong("id");
        music.music_name = json.optString("music_name");
        music.music_url = json.optString("music_url");
        music.type = json.optInt("type");
        music.rec_status = json.optString("rec_status");
        return music;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.music_name);
        dest.writeString(this.music_url);
        dest.writeLong(this.type);
        dest.writeString(this.rec_status);
    }

    public StoryMusicInfo() {
    }

    protected StoryMusicInfo(Parcel in) {
        this.id = in.readLong();
        this.music_name = in.readString();
        this.music_url = in.readString();
        this.type = in.readLong();
        this.rec_status = in.readString();
    }

    public static final Creator<StoryMusicInfo> CREATOR = new Creator<StoryMusicInfo>() {
        public StoryMusicInfo createFromParcel(Parcel source) {
            return new StoryMusicInfo(source);
        }

        public StoryMusicInfo[] newArray(int size) {
            return new StoryMusicInfo[size];
        }
    };
}
