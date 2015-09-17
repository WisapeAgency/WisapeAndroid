package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

public class StoryFontInfo extends ServerInfo{
    public long id;
    public String name;
    public String preview_url;
    public String zip_url;
    public int default_down;

    public static StoryFontInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryFontInfo music = new StoryFontInfo();
        music.id = json.optLong("id");
        music.name = json.optString("name");
        music.preview_url = json.optString("preview_url");
        music.zip_url = json.optString("zip_url");
        music.default_down = json.optInt("default_down");
        return music;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.preview_url);
        dest.writeString(this.zip_url);
        dest.writeInt(this.default_down);
    }

    public StoryFontInfo() {
    }

    protected StoryFontInfo(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.preview_url = in.readString();
        this.zip_url = in.readString();
        this.default_down = in.readInt();
    }

    public static final Creator<StoryFontInfo> CREATOR = new Creator<StoryFontInfo>() {
        public StoryFontInfo createFromParcel(Parcel source) {
            return new StoryFontInfo(source);
        }

        public StoryFontInfo[] newArray(int size) {
            return new StoryFontInfo[size];
        }
    };
}
