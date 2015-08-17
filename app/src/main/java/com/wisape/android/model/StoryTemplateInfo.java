package com.wisape.android.model;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/7/15.
 */
public class StoryTemplateInfo extends ServerInfo{
    /**
     "id": "5",
     "temp_name": "111111111111",
     "temp_img": "http://loc.wis/uploads/2015071704/82c37406f5e14ba5221605532e306ea3.jpg",
     "temp_img_local":/xxxx/ss/dd/thumb.jpg,
     "temp_description": "111111111111111111111111111111111111111111111111111",
     "temp_url": "http://loc.wis/uploads/2015071704/c0ad9aea5579dbd27d1703cd254a02d9.zip",
     "rec_status": "A",
     "type": "2",
     "exists": "0",
     "order": "2",
     "order_type": "N"
     */

    public long id;
    public String temp_name;
    public String temp_img;
    public String temp_img_local;
    public String temp_description;
    public String temp_url;
    public String rec_status;
    public long type;
    public boolean exists;
    public int order;
    public String order_type;

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
        template.type = json.optLong("type");
        template.order = json.optInt("order");
        template.order_type = json.optString("order_type");
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
        dest.writeLong(this.type);
        dest.writeInt(this.order);
        dest.writeString(this.order_type);
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
        this.type = in.readLong();
        this.order = in.readInt();
        this.order_type = in.readString();
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
