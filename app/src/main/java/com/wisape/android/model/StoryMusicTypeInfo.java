package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicTypeInfo extends ServerInfo{
    /**
     "id": 1,
     "name": "rock",
     "order": 1
     */
    public long id;
    public String name;
    public int order;

    public static StoryMusicTypeInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryMusicTypeInfo info = new StoryMusicTypeInfo();
        info.id = json.optLong("id");
        info.name = json.optString("name");
        info.order = json.optInt("order");
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.order);
    }

    public StoryMusicTypeInfo() {
    }

    protected StoryMusicTypeInfo(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.order = in.readInt();
    }

    public static final Creator<StoryMusicTypeInfo> CREATOR = new Creator<StoryMusicTypeInfo>() {
        public StoryMusicTypeInfo createFromParcel(Parcel source) {
            return new StoryMusicTypeInfo(source);
        }

        public StoryMusicTypeInfo[] newArray(int size) {
            return new StoryMusicTypeInfo[size];
        }
    };
}
