package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class StoryTemplateInfo extends ServerInfo{
    /**
     "id": "2",
     "temp_name": "2222222222222",
     "temp_img": "http://ddddd.com",
     "temp_description": "adfadfadf",
     "temp_url": "http://wowowow.com/dddd",
     "rec_status": "A"
     */

    public long id;
    public String temp_name;
    public String temp_img;
    public String temp_description;
    public String temp_url;
    public String rec_status;

    public static StoryTemplateInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryTemplateInfo template = new StoryTemplateInfo();
        template.id = json.optLong("id");
        template.temp_name = json.optString("temp_name");
        template.temp_img = json.optString("temp_img");
        template.temp_description = json.optString("temp_description");
        template.temp_url = json.optString("temp_url");
        template.rec_status = json.optString("rec_status");
        return template;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.temp_name);
        dest.writeString(this.temp_img);
        dest.writeString(this.temp_description);
        dest.writeString(this.temp_url);
        dest.writeString(this.rec_status);
    }

    public StoryTemplateInfo() {
    }

    protected StoryTemplateInfo(Parcel in) {
        this.id = in.readLong();
        this.temp_name = in.readString();
        this.temp_img = in.readString();
        this.temp_description = in.readString();
        this.temp_url = in.readString();
        this.rec_status = in.readString();
    }

    public static final Creator<StoryTemplateInfo> CREATOR = new Creator<StoryTemplateInfo>() {
        public StoryTemplateInfo createFromParcel(Parcel source) {
            return new StoryTemplateInfo(source);
        }

        public StoryTemplateInfo[] newArray(int size) {
            return new StoryTemplateInfo[size];
        }
    };
}
