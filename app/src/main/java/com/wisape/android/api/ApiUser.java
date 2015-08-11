package com.wisape.android.api;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.ServerInfo;
import com.wisape.android.model.UserActivityInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.model.UserMessageInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.network.WWWConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public class ApiUser extends ApiBase{
    private static final String TAG = ApiUser.class.getSimpleName();
    private static final int WHAT_USER_INFO = 0x01;
    private static final int WHAT_USER_MESSAGE = 0x02;
    private static final int WHAT_USER_ACTIVITY = 0x03;

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
        UserInfo user = (UserInfo)convert(WHAT_USER_INFO, message);
        return user;
    }

    /**
     * 重置秘密
     * @param context
     * @param attrInfo
     * @param tag
     * @return 服务器端返回的消息
     */
    public Requester.ServerMessage resetPassword(Context context,AttrResetPasswordInfo attrInfo,Object tag ){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_forget_pwd));
        Log.e(TAG, "#reset password uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attrInfo);
        return requester.post(uri,attrInfo.convert(),tag);
    }


    public UserInfo updateProfile(Context context, AttrUserProfile profile, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_profile_update));
        Log.d(TAG, "#updateProfile uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, profile);
        Requester.ServerMessage message = requester.post(uri, profile.convert(), tag);
        UserInfo user = (UserInfo)convert(WHAT_USER_INFO, message);
        return user;
    }


    public UserMessageInfo[] listUserMessages(Context context, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_message));
        Log.d(TAG, "#listUserMessages uri:" + uri.toString());

        Requester requester = Requester.instance();
        AttributeInfoImpl attr = new AttributeInfoImpl();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        UserMessageInfo msgArray[] = (UserMessageInfo[]) convertArray(message);
        return msgArray;
    }

    public UserActivityInfo[] listUserActivities(Context context, AttrActivityInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_user_activity));
        Log.d(TAG, "#listUserActivities uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        UserActivityInfo activities[] = (UserActivityInfo[])convertArray(WHAT_USER_ACTIVITY, message);
        return activities;
    }

    @Override
    protected ServerInfo onConvert(int what, JSONObject json) {
        ServerInfo info;
        switch (what){
            default :
                info = null;
                break;

            case WHAT_USER_INFO :
                info = UserInfo.fromJsonObject(json);
                break;

            case WHAT_USER_ACTIVITY :
                info = UserActivityInfo.fromJsonObject(json);
                break;
        }
        return info;
    }

    @Override
    protected ServerInfo onConvertError(int what) {
        ServerInfo info;
        switch (what){
            default :
                info = null;
                break;

            case WHAT_USER_INFO :
                info = new UserInfo();
                break;

            case WHAT_USER_ACTIVITY :
                info = new UserActivityInfo();
                break;
        }
        return info;
    }

    @Override
    protected ServerInfo[] onConvertArray(int what, JSONArray jsonArray, int status) {
        ServerInfo[] infoArray;
        switch (what){
            default :
                infoArray = null;
                break;

            case WHAT_USER_MESSAGE :
                int size = (null == jsonArray ? 0 : jsonArray.length());
                if(0 == size){
                    infoArray = new UserMessageInfo[0];
                }else{
                    JSONObject jsonObj;
                    infoArray = new UserMessageInfo[size];
                    for(int i = 0; i < size; i ++){
                        jsonObj = jsonArray.optJSONObject(i);
                        infoArray[i] = UserMessageInfo.fromJsonObject(jsonObj);
                    }
                }
                break;

            case WHAT_USER_ACTIVITY :
                size = (null == jsonArray ? 0 : jsonArray.length());
                if(0 == size){
                    infoArray = new UserActivityInfo[0];
                }else{
                    JSONObject jsonObj;
                    infoArray = new UserActivityInfo[size];
                    for(int i = 0; i < size; i ++){
                        jsonObj = jsonArray.optJSONObject(i);
                        infoArray[i] = UserActivityInfo.fromJsonObject(jsonObj);
                    }
                }
                break;
        }
        return infoArray;
    }

    @Override
    protected ServerInfo[] onConvertArrayError(int what) {
        ServerInfo[] infoArray;
        switch (what){
            default :
                infoArray = null;
                break;

            case WHAT_USER_MESSAGE :
                infoArray = new UserMessageInfo[0];
                break;
        }
        return infoArray;
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

    /**
     * 重置密码表单信息
     */
    public static class AttrResetPasswordInfo extends AttributeInfo{

        public static final String ATTR_EMAIL = "user_email";

        public String email;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_EMAIL, null == email ? "" : email);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        protected int acquireAttributeNumber() {
            return 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
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

    public static class AttrActivityInfo extends AttributeInfo{
        public static final String ATTR_COUNTRY_CODE = "country_code";

        public String countryIso;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_COUNTRY_CODE, countryIso);
        }

        @Override
        protected int acquireAttributeNumber() {
            return 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.countryIso);
        }

        public AttrActivityInfo() {
        }

        protected AttrActivityInfo(Parcel in) {
            this.countryIso = in.readString();
        }

        public static final Creator<AttrActivityInfo> CREATOR = new Creator<AttrActivityInfo>() {
            public AttrActivityInfo createFromParcel(Parcel source) {
                return new AttrActivityInfo(source);
            }

            public AttrActivityInfo[] newArray(int size) {
                return new AttrActivityInfo[size];
            }
        };
    }
}
