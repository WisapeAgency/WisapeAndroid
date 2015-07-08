package com.wisape.android.network;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.UserInfo;

import java.util.Map;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public class ApiUser extends ApiBase{
    private static final String TAG = ApiUser.class.getSimpleName();

    public static ApiUser instance(){
        return new ApiUser();
    }

    private ApiUser(){}

    public UserInfo signUp(Context context, AttrSignUpInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_login));
        Log.d(TAG, "#signUp uri:" + uri.toString() + ", port:" + uri.getPort());

        Requester requester = Requester.instance();
        setAccessToken(context, attrInfo);
        Requester.ServerMessage message = requester.post(uri, attrInfo.convert(), tag);
        return convertUserInfo(message);
    }

    public UserInfo updateProfile(Context context, AttrUserProfile profile, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_profile_update));
        Log.d(TAG, "#signUp uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, profile);
        Requester.ServerMessage message = requester.post(uri, profile.convert(), tag);
        return convertUserInfo(message);
    }

    private UserInfo convertUserInfo(Requester.ServerMessage message){
        UserInfo user;
        if(message.succeed()){
            user = UserInfo.fromJsonObject(message.data);
        }else{
            user = new UserInfo();
            user.message = message.message;
        }
        user.status = message.status;
        Log.d(TAG, "#convertUserInfo ServerMessage:" + message.toString());
        message.recycle();
        return user;
    }

    public static class AttrUserProfile extends AttributeInfo{
        public static final String ATTR_NICK_NAME = "nick_name";
        public static final String ATTR_USER_ICO = "user_ico";
        public static final String ATTR_USER_EMAIL = "user_email";

        public String nickName;
        public String userIcon;
        public String userEmail;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_NICK_NAME, nickName);
            params.put(ATTR_USER_ICO, userIcon);
            params.put(ATTR_USER_EMAIL, userEmail);
        }

        @Override
        protected int acquireAttributeNumber() {
            return 3;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.nickName);
            dest.writeString(this.userIcon);
            dest.writeString(this.userEmail);
        }

        public AttrUserProfile() {
        }

        protected AttrUserProfile(Parcel in) {
            this.nickName = in.readString();
            this.userIcon = in.readString();
            this.userEmail = in.readString();
        }

        public static final Creator<AttrUserProfile> CREATOR = new Creator<AttrUserProfile>() {
            public AttrUserProfile createFromParcel(Parcel source) {
                return new AttrUserProfile(source);
            }

            public AttrUserProfile[] newArray(int size) {
                return new AttrUserProfile[size];
            }
        };
    }

    public static class AttrSignUpInfo extends AttributeInfo {
        public static final String ATTR_TYPE = "type";
        public static final String ATTR_EMAIL = "user_email";
        public static final String ATTR_PASSWORD = "user_pwd";
        public static final String ATTR_USER_ICON = "user_ico";
        public static final String ATTR_NICK_NAME = "nick_name";
        public static final String ATTR_UNIQUE_STR = "unique_str";

        public String type;
        public String email;
        public String password;
        public String userIcon;
        public String nickName;
        public String uniqueStr;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_TYPE, null == type ? "" : type);
            params.put(ATTR_EMAIL, null == email ? "" : email);
            params.put(ATTR_PASSWORD, null == password ? "" : password);
            params.put(ATTR_USER_ICON, null == userIcon ? "" : userIcon);
            params.put(ATTR_NICK_NAME, null == nickName ? "" : nickName);
            params.put(ATTR_UNIQUE_STR, null == uniqueStr ? "" : uniqueStr);
        }

        @Override
        protected int acquireAttributeNumber() {
            return 6;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.type);
            dest.writeString(this.email);
            dest.writeString(this.password);
        }

        public AttrSignUpInfo() {
        }

        protected AttrSignUpInfo(Parcel in) {
            this.type = in.readString();
            this.email = in.readString();
            this.password = in.readString();
        }

        public static final Creator<AttrSignUpInfo> CREATOR = new Creator<AttrSignUpInfo>() {
            public AttrSignUpInfo createFromParcel(Parcel source) {
                return new AttrSignUpInfo(source);
            }

            public AttrSignUpInfo[] newArray(int size) {
                return new AttrSignUpInfo[size];
            }
        };
    }
}
