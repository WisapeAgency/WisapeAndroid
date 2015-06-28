package com.oauth.android;

import android.os.Parcel;
import android.os.Parcelable;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.SinaWeiboApi20;
import org.scribe.builder.api.TwitterApi;

/**
 * OAuthParams
 * Created by malei on 14/12/2.
 */
public class OAuthParams implements Parcelable {

    public static final int VERSION_OAUTH_1 = 1;
    public static final int VERSION_OAUTH_2 = 2;
    //
    public static final int OAUTH_FACEBOOK = 0x0001;
    public static final int OAUTH_TWITTER = 0x0002;
    public static final int OAUTH_SOUNDCLOUD = 0x0003;
    public static final int OAUTH_SINA = 0x0004;
    public static final int OAUTH_GOOGLEPLUS = 0x0005;
    public static final int OAUTH_WECHAT = 0x0006;

    public static final Creator<OAuthParams> CREATOR = new Creator<OAuthParams>() {
        @Override
        public OAuthParams createFromParcel(Parcel source) {
            return new OAuthParams(source);
        }

        @Override
        public OAuthParams[] newArray(int size) {
            return new OAuthParams[size];
        }
    };

    private int version;
    private int oauth;
    private String name;
    private String scope;
    private String apiKey;
    private String apiSecret;
    private String callback;

    public OAuthParams(int version, int oauth, String name, String scope,String apiKey, String apiSecret, String callback) {
        this.version = version;
        this.oauth = oauth;
        this.name = name;
        this.scope = scope;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callback = callback;
    }

    public OAuthParams(Parcel source) {
        version = source.readInt();
        oauth = source.readInt();
        name = source.readString();
        scope = source.readString();
        apiKey = source.readString();
        apiSecret = source.readString();
        callback = source.readString();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOauth() {
        return oauth;
    }

    public void setOauth(int oauth) {
        this.oauth = oauth;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(version);
        dest.writeInt(oauth);
        dest.writeString(name);
        dest.writeString(scope);
        dest.writeString(apiKey);
        dest.writeString(apiSecret);
        dest.writeString(callback);
    }

    public Class<? extends Api> getApiProvider() {
        Class<? extends Api> clazz = null;
        switch (oauth) {
            case OAuthParams.OAUTH_FACEBOOK:
                clazz = FacebookApi.class;
                break;

            case OAuthParams.OAUTH_TWITTER:
                clazz = TwitterApi.class;
                break;

            case OAuthParams.OAUTH_SOUNDCLOUD:
                clazz = SoundCloudApi.class;
                break;

            case OAuthParams.OAUTH_SINA:
                clazz = SinaWeiboApi20.class;
                break;

            case OAuthParams.OAUTH_GOOGLEPLUS:
                clazz = GoogleApi20.class;
                break;
            default:
                break;
        }
        return clazz;
    }
}
