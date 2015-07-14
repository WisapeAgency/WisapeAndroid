package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 14/7/15.
 */
public class UserMessageInfo extends ServerInfo{
    public long id;
    public String message;

    public static UserMessageInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        UserMessageInfo msg = new UserMessageInfo();
        msg.id = json.optLong("id");
        msg.message = json.optString("message");
        return msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.message);
    }

    public UserMessageInfo() {
    }

    protected UserMessageInfo(Parcel in) {
        this.id = in.readLong();
        this.message = in.readString();
    }

    public static final Creator<UserMessageInfo> CREATOR = new Creator<UserMessageInfo>() {
        public UserMessageInfo createFromParcel(Parcel source) {
            return new UserMessageInfo(source);
        }

        public UserMessageInfo[] newArray(int size) {
            return new UserMessageInfo[size];
        }
    };
}
