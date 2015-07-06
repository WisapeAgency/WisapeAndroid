package com.wisape.android.model;

import android.os.Parcel;

import org.json.JSONObject;

/**
 * Created by LeiGuoting on 15/6/16.
 */
public class UserInfo extends ServerInfo {
    public long user_id;
    public char user_sex;
    public long user_token_id;
    public String user_email;
    public String access_token;
    public String user_pwd;
    public String nick_name;
    public String platform;         // ->user_ext
    public String user_ext_name;
    public String user_ico_normal;  // ->user_ico_n
    public String user_ico_big;     // ->user_ico_b
    public String user_ico_small;   // ->user_ico_s
    public String user_back_img;
    public String unique_str;       //第三方平台ID

    public UserInfo() {}

    public static UserInfo fromJsonObject(JSONObject object) {
        UserInfo entity = new UserInfo();
        entity.user_id = object.optLong("user_id", 0);
        entity.user_sex = object.optString("user_sex", "").charAt(0);
        entity.user_token_id = object.optLong("user_token_id", 0);
        entity.nick_name = object.optString("nick_name", "");
        entity.user_pwd = object.optString("user_pwd", "");
        entity.user_email = object.optString("user_email", "");
        entity.access_token = object.optString("access_token", "");
        entity.platform = object.optString("user_ext", "");
        entity.user_ext_name = object.optString("user_ext_name", "");
        entity.user_ico_normal = object.optString("user_ico_n", "");
        entity.user_ico_big = object.optString("user_ico_b", "");
        entity.user_ico_small = object.optString("user_ico_s", "");
        entity.user_back_img = object.optString("user_back_img", "");
        entity.unique_str = object.optString("unique_str", "");
        return entity;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.user_id);
        dest.writeInt(user_sex);
        dest.writeLong(this.user_token_id);
        dest.writeString(this.user_email);
        dest.writeString(this.access_token);
        dest.writeString(this.user_pwd);
        dest.writeString(this.nick_name);
        dest.writeString(this.platform);
        dest.writeString(this.user_ext_name);
        dest.writeString(this.user_ico_normal);
        dest.writeString(this.user_ico_big);
        dest.writeString(this.user_ico_small);
        dest.writeString(this.user_back_img);
        dest.writeString(this.unique_str);
    }

    protected UserInfo(Parcel in) {
        this.user_id = in.readLong();
        this.user_sex = (char) in.readInt();
        this.user_token_id = in.readLong();
        this.user_email = in.readString();
        this.access_token = in.readString();
        this.user_pwd = in.readString();
        this.nick_name = in.readString();
        this.platform = in.readString();
        this.user_ext_name = in.readString();
        this.user_ico_normal = in.readString();
        this.user_ico_big = in.readString();
        this.user_ico_small = in.readString();
        this.user_back_img = in.readString();
        this.unique_str = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };
}