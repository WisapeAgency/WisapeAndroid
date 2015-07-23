package com.wisape.android.model;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 14/7/15.
 */
public class UserActivityInfo extends ServerInfo{
    public long id;
    public String title;
    public String bg_img;
    public String url;
    public String rec_status;
    public long start_time;
    public long end_time;
    public String country;

    public static UserActivityInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        UserActivityInfo activity = new UserActivityInfo();
        activity.id = json.optLong("id");
        activity.title = json.optString("title");
        activity.bg_img = json.optString("bg_img");
        activity.url = json.optString("url");
        activity.rec_status = json.optString("rec_status");
        activity.start_time = json.optLong("start_time");
        activity.end_time = json.optLong("end_time");
        activity.country = json.optString("country");
        return activity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.bg_img);
        dest.writeString(this.url);
        dest.writeString(this.rec_status);
        dest.writeLong(this.start_time);
        dest.writeLong(this.end_time);
        dest.writeString(this.country);
    }

    public UserActivityInfo() {
    }

    protected UserActivityInfo(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.bg_img = in.readString();
        this.url = in.readString();
        this.rec_status = in.readString();
        this.start_time = in.readLong();
        this.end_time = in.readLong();
        this.country = in.readString();
    }

    public static final Creator<UserActivityInfo> CREATOR = new Creator<UserActivityInfo>() {
        public UserActivityInfo createFromParcel(Parcel source) {
            return new UserActivityInfo(source);
        }

        public UserActivityInfo[] newArray(int size) {
            return new UserActivityInfo[size];
        }
    };
}
