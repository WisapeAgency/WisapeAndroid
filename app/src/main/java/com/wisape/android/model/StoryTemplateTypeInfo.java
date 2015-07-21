package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by tony on 2015/7/20.
 */
public class StoryTemplateTypeInfo extends ServerInfo{
    /**
     "id": 1,
     "name": "大",
     "order": 1
     */
    public long id;
    public String name;
    public int order;

    public static StoryTemplateTypeInfo fromJsonObject(JSONObject jsonObj){
        if(null == jsonObj){
            return null;
        }

        StoryTemplateTypeInfo info = new StoryTemplateTypeInfo();
        info.id = jsonObj.optLong("id");
        info.name = jsonObj.optString("name");
        info.order = jsonObj.optInt("order");
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

    public StoryTemplateTypeInfo() {
    }

    protected StoryTemplateTypeInfo(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.order = in.readInt();
    }

    public static final Creator<StoryTemplateTypeInfo> CREATOR = new Creator<StoryTemplateTypeInfo>() {
        public StoryTemplateTypeInfo createFromParcel(Parcel source) {
            return new StoryTemplateTypeInfo(source);
        }

        public StoryTemplateTypeInfo[] newArray(int size) {
            return new StoryTemplateTypeInfo[size];
        }
    };
}