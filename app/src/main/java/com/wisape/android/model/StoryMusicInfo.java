package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class StoryMusicInfo extends ServerInfo{
    /**
     "id": 2,
     "music_name": "rock and roll",
     "music_url": "http://www.wisape.com/uploads/yyzd.mp3"
     */

    public long id;
    public String music_name;
    public String music_url;

    public static StoryMusicInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryMusicInfo music = new StoryMusicInfo();
        music.id = json.optLong("id");
        music.music_name = json.optString("music_name");
        music.music_url = json.optString("music_url");
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
    }

    public StoryMusicInfo() {
    }

    protected StoryMusicInfo(Parcel in) {
        this.id = in.readLong();
        this.music_name = in.readString();
        this.music_url = in.readString();
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
