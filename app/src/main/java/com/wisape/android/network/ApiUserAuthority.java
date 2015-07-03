package com.wisape.android.network;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.UserInfo;

import java.util.HashMap;
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

    public UserInfo signup(Context context, AttrSignupInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_login));
        Log.d(TAG, "#signup uri:" + uri.toString() + ", port:" + uri.getPort());

        Requester requester = Requester.instance();
        Requester.ServerMessage message = requester.post(context, uri, attrInfo.convert(), tag);
        UserInfo user;
        if(message.succeed()){
            user = UserInfo.parse(message.data);
        }else{
            user = new UserInfo();
            user.status = message.status;
            user.message = message.message;
        }
        Log.d(TAG, "#signup ServerMessage:" + message.toString());
        message.recycle();
        return user;
    }

    public UserInfo signupWithPlatform(Context context, AttrSignupPlatformInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_login));
        Log.d(TAG, "#signup uri:" + uri.toString());

        Requester requester = Requester.instance();
        Requester.ServerMessage message = requester.post(context, uri, attrInfo.convert(), tag);
        UserInfo user;
        if(message.succeed()){
            user = UserInfo.parse(message.data);
        }else{
            user = new UserInfo();
            user.status = message.status;
            user.message = message.message;
        }
        message.recycle();
        return user;
    }

    public static class AttrSignupPlatformInfo extends AttributeInfo{
        public static final String ATTRIBUTE_EXT_ID = "user_ext_id";
        public static final String ATTRIBUTE_EXT_ICON = "user_ico";
        public static final String ATTRIBUTE_NICK_NAME = "nick_name";
        public static final String ATTRIBUTE_PLATFORM = "unique_str";

        public long extId;
        public String extIcon;
        public String nickName;
        public String platform;

        @Override
        public Map<String, String> convert() {
            Map<String, String> params = new HashMap(4);
            params.put(ATTRIBUTE_EXT_ID, Long.toString(extId));
            params.put(ATTRIBUTE_EXT_ICON, extIcon);
            params.put(ATTRIBUTE_NICK_NAME, nickName);
            params.put(ATTRIBUTE_PLATFORM, platform);
            return params;
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

        public AttrSignupPlatformInfo() {
        }

        protected AttrSignupPlatformInfo(Parcel in) {
            this.extId = in.readLong();
            this.extIcon = in.readString();
            this.nickName = in.readString();
            this.platform = in.readString();
        }

        public static final Creator<AttrSignupPlatformInfo> CREATOR = new Creator<AttrSignupPlatformInfo>() {
            public AttrSignupPlatformInfo createFromParcel(Parcel source) {
                return new AttrSignupPlatformInfo(source);
            }

            public AttrSignupPlatformInfo[] newArray(int size) {
                return new AttrSignupPlatformInfo[size];
            }
        };
    }

    public static class AttrSignupInfo extends AttributeInfo {
        public static final String ATTRIBUTE_TYPE = "type";
        public static final String ATTRIBUTE_EMAIL = "user_email";
        public static final String ATTRIBUTE_PASSWORD = "user_pwd";

        public static final int DEFINE_TYPE_SIGNUP = 1;

        public int type;
        public String email;
        public String password;

        public Map<String, String> convert(){
            Map<String, String> params = new HashMap(3);
            params.put(ATTRIBUTE_TYPE, Integer.toString(type));
            params.put(ATTRIBUTE_EMAIL, null == email ? "" : email);
            params.put(ATTRIBUTE_PASSWORD, null == password ? "" : password);
            return params;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.type);
            dest.writeString(this.email);
            dest.writeString(this.password);
        }

        public AttrSignupInfo() {
        }

        protected AttrSignupInfo(Parcel in) {
            this.type = in.readInt();
            this.email = in.readString();
            this.password = in.readString();
        }

        public static final Creator<AttrSignupInfo> CREATOR = new Creator<AttrSignupInfo>() {
            public AttrSignupInfo createFromParcel(Parcel source) {
                return new AttrSignupInfo(source);
            }

            public AttrSignupInfo[] newArray(int size) {
                return new AttrSignupInfo[size];
            }
        };
    }
}
