package com.wisape.android.model;

import android.net.Uri;
import android.os.Parcel;

import org.json.JSONObject;

/**
 * 用户信息实体
 * Created by LeiGuoting on 15/6/16.
 */
public class UserInfo{
    public long user_id;
    public char user_sex;
    public long user_token_id;
    public String user_email;
    public String access_token;
    public String user_pwd;
    public String nick_name;
    public String user_ext;         //平台类型见 UserManager
    public String user_ext_name;
    public String user_ico_n;
    public String user_ico_b;
    public String user_ico_s;
    public String user_back_img;
    public String unique_str;       //第三方平台ID
    public String install_id;


//    public UserInfo() {}
//
//
//    public static UserInfo fromJsonObject(JSONObject object) {
//        UserInfo entity = new UserInfo();
//        entity.user_id = object.optLong("user_id", 0);
//        entity.user_sex = object.optString("user_sex", "").charAt(0);
//        entity.user_token_id = object.optLong("user_token_id", 0);
//        entity.nick_name = object.optString("nick_name", "");
//        entity.user_pwd = object.optString("user_pwd", "");
//        entity.user_email = object.optString("user_email", "");
//        entity.access_token = object.optString("access_token", "");
//        entity.user_ext = object.optString("user_ext", "");
//        entity.user_ext_name = object.optString("user_ext_name", "");
//        entity.user_ico_n = object.optString("user_ico_n", "");
//        entity.user_ico_b = object.optString("user_ico_b", "");
//        entity.user_ico_s = object.optString("user_ico_s", "");
//        entity.user_back_img = object.optString("user_back_img", "");
//        entity.unique_str = object.optString("unique_str", "");
//        return entity;
//    }
////
////
//    @Override
//    public int describeContents() {
//        return 0;
//    }
////
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeLong(this.user_id);
//        dest.writeInt(user_sex);
//        dest.writeLong(this.user_token_id);
//        dest.writeString(this.user_email);
//        dest.writeString(this.access_token);
//        dest.writeString(this.user_pwd);
//        dest.writeString(this.nick_name);
//        dest.writeString(this.user_ext);
//        dest.writeString(this.user_ext_name);
//        dest.writeString(Uri.encode(this.user_ico_n));
//        dest.writeString(Uri.encode(this.user_ico_b));
//        dest.writeString(Uri.encode(this.user_ico_s));
//        dest.writeString(Uri.encode(this.user_back_img));
//        dest.writeString(this.unique_str);
//    }
////
//    protected UserInfo(Parcel in) {
//        this.user_id = in.readLong();
//        this.user_sex = (char) in.readInt();
//        this.user_token_id = in.readLong();
//        this.user_email = in.readString();
//        this.access_token = in.readString();
//        this.user_pwd = in.readString();
//        this.nick_name = in.readString();
//        this.user_ext = in.readString();
//        this.user_ext_name = in.readString();
//        this.user_ico_n = Uri.decode(in.readString());
//        this.user_ico_b = Uri.decode(in.readString());
//        this.user_ico_s = Uri.decode(in.readString());
//        this.user_back_img = Uri.decode(in.readString());
//        this.unique_str = in.readString();
//    }
//
//    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
//        public UserInfo createFromParcel(Parcel source) {
//            return new UserInfo(source);
//        }
//
//        public UserInfo[] newArray(int size) {
//            return new UserInfo[size];
//        }
//    };
}