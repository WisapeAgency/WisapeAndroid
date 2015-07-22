package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryGestureInfo extends BaseInfo{
    public long id;
    public String name;

    public static StoryGestureInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryGestureInfo gesture = new StoryGestureInfo();
        gesture.id = json.optLong("id");
        gesture.name = json.optString("name");
        return gesture;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
    }

    public StoryGestureInfo() {
    }

    protected StoryGestureInfo(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
    }

    public static final Creator<StoryGestureInfo> CREATOR = new Creator<StoryGestureInfo>() {
        public StoryGestureInfo createFromParcel(Parcel source) {
            return new StoryGestureInfo(source);
        }

        public StoryGestureInfo[] newArray(int size) {
            return new StoryGestureInfo[size];
        }
    };
}
