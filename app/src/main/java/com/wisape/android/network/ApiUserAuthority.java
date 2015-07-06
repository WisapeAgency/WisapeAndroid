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
public class ApiUserAuthority extends ApiBase{
    private static final String TAG = ApiUserAuthority.class.getSimpleName();

    public static ApiUserAuthority instance(){
        return new ApiUserAuthority();
    }

    private ApiUserAuthority(){}

    public UserInfo signUp(Context context, AttrSignUpInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_login));
        Log.d(TAG, "#signUp uri:" + uri.toString() + ", port:" + uri.getPort());

        Requester requester = Requester.instance();
        setAccessToken(context, attrInfo);
        Requester.ServerMessage message = requester.post(uri, attrInfo.convert(), tag);
        UserInfo user;
        if(message.succeed()){
            user = UserInfo.fromJsonObject(message.data);
        }else{
            user = new UserInfo();
            user.message = message.message;
        }
        user.status = message.status;
        Log.d(TAG, "#signUp ServerMessage:" + message.toString());
        message.recycle();
        return user;
    }

    public UserInfo signUpWith(Context context, AttrSignUpWithInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_login));
        Log.d(TAG, "#signup uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attrInfo);
        Requester.ServerMessage message = requester.post(uri, attrInfo.convert(), tag);
        UserInfo user;
        if(message.succeed()){
            user = UserInfo.fromJsonObject(message.data);
        }else{
            user = new UserInfo();
            user.status = message.status;
            user.message = message.message;
        }
        message.recycle();
        return user;
    }

    public static class AttrSignUpWithInfo extends AttributeInfo{
        public static final String ATTR_EXT_ID = "user_ext_id";
        public static final String ATTR_EXT_ICON = "user_ico";
        public static final String ATTR_NICK_NAME = "nick_name";
        public static final String ATTR_PLATFORM = "unique_str";

        public long extId;
        public String extIcon;
        public String nickName;
        public String platform;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_EXT_ID, Long.toString(extId));
            params.put(ATTR_EXT_ICON, extIcon);
            params.put(ATTR_NICK_NAME, nickName);
            params.put(ATTR_PLATFORM, platform);
        }

        @Override
        protected int acquireAttributeNumber() {
            return 4;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.extId);
            dest.writeString(this.extIcon);
            dest.writeString(this.nickName);
            dest.writeString(this.platform);
        }

        public AttrSignUpWithInfo() {
        }

        protected AttrSignUpWithInfo(Parcel in) {
            this.extId = in.readLong();
            this.extIcon = in.readString();
            this.nickName = in.readString();
            this.platform = in.readString();
        }

        public static final Creator<AttrSignUpWithInfo> CREATOR = new Creator<AttrSignUpWithInfo>() {
            public AttrSignUpWithInfo createFromParcel(Parcel source) {
                return new AttrSignUpWithInfo(source);
            }

            public AttrSignUpWithInfo[] newArray(int size) {
                return new AttrSignUpWithInfo[size];
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
