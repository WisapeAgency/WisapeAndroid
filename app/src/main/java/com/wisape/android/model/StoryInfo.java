package com.wisape.android.model;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryInfo extends ServerInfo{
    /**
     "createtime": 1436343993,
     "uid": "2",
     "description": "xxxx",
     "rec_status": "A",
     "like_num": 0,
     "view_num": 0,
     "share_num": 0,
     "small_img": "http://loc.wisuploads/20150708/1436343993_13689.jpg",
     "story_name": "shit",
     "story_url": "http://loc.wis/html/2/201507080826",
     "id": "1"
     */

    public long id;
    public long uid;
    public String story_name;
    public String description;
    public String createtime;

    /**
     * Story Status
     * See {@link com.wisape.android.api.ApiStory.AttrStoryInfo#storyStatus}
     */
    public String rec_status;
    public int like_num;
    public int view_num;
    public int share_num;
    /**
     * Story thumb
     * See {@link com.wisape.android.api.ApiStory.AttrStoryInfo#storyThumb}
     */
    public String small_img;
    public String story_url;


    public static StoryInfo fromJsonObject(JSONObject json){
        if(null == json){
            return null;
        }

        StoryInfo story = new StoryInfo();
        story.id = json.optLong("id");
        story.uid = json.optLong("uid");
        story.story_name = json.optString("story_name");
        story.description = json.optString("description");
        story.createtime = json.optString("createtime");
        story.rec_status = json.optString("rec_status");
        story.like_num = json.optInt("like_num");
        story.view_num = json.optInt("view_num");
        story.share_num = json.optInt("share_num");
        story.small_img = json.optString("small_img");
        story.story_url = json.optString("story_url");
        return story;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.uid);
        dest.writeString(this.story_name);
        dest.writeString(this.description);
        dest.writeString(this.createtime);
        dest.writeString(this.rec_status);
        dest.writeInt(this.like_num);
        dest.writeInt(this.view_num);
        dest.writeInt(this.share_num);
        dest.writeString(this.small_img);
        dest.writeString(this.story_url);
    }

    public StoryInfo() {
    }

    protected StoryInfo(Parcel in) {
        this.id = in.readLong();
        this.uid = in.readLong();
        this.story_name = in.readString();
        this.description = in.readString();
        this.createtime = in.readString();
        this.rec_status = in.readString();
        this.like_num = in.readInt();
        this.view_num = in.readInt();
        this.share_num = in.readInt();
        this.small_img = in.readString();
        this.story_url = in.readString();
    }

    public static final Creator<StoryInfo> CREATOR = new Creator<StoryInfo>() {
        public StoryInfo createFromParcel(Parcel source) {
            return new StoryInfo(source);
        }

        public StoryInfo[] newArray(int size) {
            return new StoryInfo[size];
        }
    };
}
