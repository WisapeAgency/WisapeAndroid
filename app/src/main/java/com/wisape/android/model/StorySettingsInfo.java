package com.wisape.android.model;

import android.net.Uri;
import android.os.Parcel;

import com.wisape.android.database.StoryMusicEntity;

import org.json.JSONObject;

/**
 * Created by tony on 2015/7/22.
 */
public class StorySettingsInfo extends BaseInfo{
    public String defaultName;
    public String defaultDesc;
    public String defaultCover;
    public StoryMusicEntity defaultMusic;
    public StoryGestureInfo defaultGesture;

    public static StorySettingsInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StorySettingsInfo settings = new StorySettingsInfo();
        settings.defaultName = json.optString("defaultName");
        settings.defaultDesc = json.optString("defaultDesc");
        settings.defaultCover = Uri.parse(json.optString("defaultCover")).toString();
        settings.defaultMusic = StoryMusicEntity.fromJsonObject(json.optJSONObject("defaultMusic"));
        settings.defaultGesture = StoryGestureInfo.fromJsonObject(json.optJSONObject("defaultGesture"));
        return settings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.defaultName);
        dest.writeString(this.defaultDesc);
        dest.writeString(this.defaultCover);
        dest.writeParcelable(this.defaultMusic, 0);
        dest.writeParcelable(this.defaultGesture, 0);
    }

    public StorySettingsInfo() {
    }

    protected StorySettingsInfo(Parcel in) {
        this.defaultName = in.readString();
        this.defaultDesc = in.readString();
        this.defaultCover = in.readParcelable(Uri.class.getClassLoader());
        this.defaultMusic = in.readParcelable(StoryMusicEntity.class.getClassLoader());
        this.defaultGesture = in.readParcelable(StoryGestureInfo.class.getClassLoader());
    }

    public static final Creator<StorySettingsInfo> CREATOR = new Creator<StorySettingsInfo>() {
        public StorySettingsInfo createFromParcel(Parcel source) {
            return new StorySettingsInfo(source);
        }

        public StorySettingsInfo[] newArray(int size) {
            return new StorySettingsInfo[size];
        }
    };
}
