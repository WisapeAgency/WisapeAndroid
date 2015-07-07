package com.wisape.android.common;

import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.oauth.android.OAuthParams;
import com.oauth.android.OAuthRequestor;
import com.wisape.android.common.TwitterProfileRequester.TwitterParams;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by LeiGuoting on 6/7/15.
 */
public class TwitterProfileRequester implements ProfileRequester<TwitterParams>{
    private static final String TAG = TwitterProfileRequester.class.getSimpleName();
    private static final String URL_PROFILE = "https://api.twitter.com/1.1/users/show.json";

    @Override
    public ProfileInfo request(TwitterParams params) {
        Uri uri = Uri.parse(new StringBuilder("oauth://twitter?").append(params.refreshResponse).toString());
        String userId = uri.getQueryParameter("user_id");
        String screenName = uri.getQueryParameter("screen_name");

        OAuthParams oAuthParams = new OAuthParams(OAuthParams.VERSION_OAUTH_1,
                OAuthParams.OAUTH_TWITTER,
                "",
                "",
                params.apiKey,
                params.apiSecret,
                params.apiCallback);

        HashMap<String, String> map = new HashMap();
        map.put("user_id", userId);
        map.put("screen_name", screenName);

        ProfileInfo profile;
        try {
            String data = OAuthRequestor.get(oAuthParams, params.token, params.screen, URL_PROFILE, map);
            Log.d(TAG, "#request data:" + data);
            JSONObject jsonObj = new JSONObject(data);
            profile = new ProfileInfo(UserManager.SIGN_UP_WITH_TWITTER);
            profile.uniqueStr = jsonObj.optString("id");
            profile.nickName = jsonObj.optString("name");
            profile.icon = jsonObj.optString("profile_image_url");
        } catch (Exception e) {
            profile = null;
            Log.e(TAG, "", e);
        }
        return profile;
    }

    public static class TwitterParams extends ProfileRequester.Param{
        public String refreshResponse;
        public String apiKey;
        public String apiSecret;
        public String apiCallback;


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.refreshResponse);
            dest.writeString(this.apiKey);
            dest.writeString(this.apiSecret);
            dest.writeString(this.apiCallback);
        }

        public TwitterParams() {
        }

        protected TwitterParams(Parcel in) {
            super(in);
            this.refreshResponse = in.readString();
            this.apiKey = in.readString();
            this.apiSecret = in.readString();
            this.apiCallback = in.readString();
        }

        public static final Creator<TwitterParams> CREATOR = new Creator<TwitterParams>() {
            public TwitterParams createFromParcel(Parcel source) {
                return new TwitterParams(source);
            }

            public TwitterParams[] newArray(int size) {
                return new TwitterParams[size];
            }
        };
    }
}
