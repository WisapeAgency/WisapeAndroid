package com.wisape.android.model;

import android.os.Parcel;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * UserEntity
 * Created by Xugm on 15/6/16.
 */
public class UserInfo extends ServerInfo {
    public int id;
    public String nick_name;
    public String user_pwd;
    public String email;
    public String user_ext;
    public String user_ext_id;
    public String user_ext_name;
    public String user_sex;
    public String user_icon_n;
    public String user_icon_b;
    public String user_icon_s;
    public String user_intersting;
    public String user_star;
    public String user_sig;
    public String user_token_id;
    public String user_back_img;
    public String user_id;
    public String accesstoken;
    public int token_start;
    public int token_end;
    public String isDefaultUser;

    public UserInfo() {
    }

    protected UserInfo(Parcel source) {
        readFromParcel(source);
    }

    public static UserInfo fromJsonObject(JSONObject json) {
        return new Gson().fromJson(json.toString(), UserInfo.class);
    }

    public static UserInfo parse(JSONObject object) {
        UserInfo entity = new UserInfo();
        entity.id = object.optInt("id", 0);
        entity.nick_name = object.optString("nick_name", "");
        entity.user_pwd = object.optString("user_pwd", "");
        entity.email = object.optString("email", "");
        entity.user_ext = object.optString("user_ext", "");
        entity.user_ext_id = object.optString("user_ext_id", "");
        entity.user_ext_name = object.optString("user_ext_name", "");
        entity.user_sex = object.optString("user_sex", "");
        entity.user_icon_n = object.optString("user_icon_n", "");
        entity.user_icon_b = object.optString("user_icon_b", "");
        entity.user_icon_s = object.optString("user_icon_s", "");
        entity.user_intersting = object.optString("user_intersting", "");
        entity.user_star = object.optString("user_star", "");
        entity.user_sig = object.optString("user_sig", "");
        entity.user_token_id = object.optString("user_token_id", "");
        entity.user_back_img = object.optString("user_back_img", "");
        entity.user_id = object.optString("user_id", "");
        entity.accesstoken = object.optString("accesstoken", "");
        entity.token_start = object.optInt("token_start", 0);
        entity.token_end = object.optInt("token_end", 0);
        entity.isDefaultUser = "false";

        return entity;
    }

    public static JSONObject toJsonObject(UserInfo entity) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", entity.id);
        jsonObj.put("nick_name", entity.nick_name);
        jsonObj.put("user_pwd", entity.user_pwd);
        jsonObj.put("email", entity.email);
        jsonObj.put("user_ext", entity.user_ext);
        jsonObj.put("user_ext_id", entity.user_ext_id);
        jsonObj.put("user_ext_name", entity.user_ext_name);
        jsonObj.put("user_sex", entity.user_sex);
        jsonObj.put("user_icon_n", entity.user_icon_n);
        jsonObj.put("user_icon_b", entity.user_icon_b);
        jsonObj.put("user_icon_s", entity.user_icon_s);
        jsonObj.put("user_intersting", entity.user_intersting);
        jsonObj.put("user_star", entity.user_star);
        jsonObj.put("user_sig", entity.user_sig);
        jsonObj.put("user_token_id", entity.user_token_id);
        jsonObj.put("user_back_img", entity.user_back_img);
        jsonObj.put("user_id", entity.user_id);
        jsonObj.put("accesstoken", entity.accesstoken);
        jsonObj.put("token_start", entity.token_start);
        jsonObj.put("token_end", entity.token_end);

        return jsonObj;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nick_name);
        dest.writeString(user_pwd);
        dest.writeString(email);
        dest.writeString(user_ext);
        dest.writeString(user_ext_id);
        dest.writeString(user_ext_name);
        dest.writeString(user_sex);
        dest.writeString(user_icon_n);
        dest.writeString(user_icon_b);
        dest.writeString(user_icon_s);
        dest.writeString(user_intersting);
        dest.writeString(user_star);
        dest.writeString(user_sig);
        dest.writeString(user_token_id);
        dest.writeString(user_back_img);
        dest.writeString(user_id);
        dest.writeString(accesstoken);
        dest.writeInt(token_start);
        dest.writeInt(token_end);
        dest.writeString(isDefaultUser);
    }

    protected void readFromParcel(Parcel source) {
        id = source.readInt();
        nick_name = source.readString();
        user_pwd = source.readString();
        email = source.readString();
        user_ext = source.readString();
        user_ext_id = source.readString();
        user_ext_name = source.readString();
        user_sex = source.readString();
        user_icon_n = source.readString();
        user_icon_b = source.readString();
        user_icon_s = source.readString();
        user_intersting = source.readString();
        user_star = source.readString();
        user_sig = source.readString();
        user_token_id = source.readString();
        user_back_img = source.readString();
        user_id = source.readString();
        accesstoken = source.readString();
        token_start = source.readInt();
        token_end = source.readInt();
        isDefaultUser = source.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {

        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }

    };
}