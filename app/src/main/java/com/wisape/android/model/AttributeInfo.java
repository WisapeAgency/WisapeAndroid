package com.wisape.android.model;

import android.os.Parcelable;

import com.wisape.android.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public abstract class AttributeInfo extends BaseInfo implements Parcelable{
    public static final String ATTR_ACCESS_TOKEN = "access_token";
    public static final String ATTR_EXPIRES = "expires";

    private String accessToken;

    public final void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    public final Map<String, String> convert(){
        HashMap<String, String> params = new HashMap(acquireAttributeNumber() + 2);
        onConvert(params);
        params.put(ATTR_ACCESS_TOKEN, null == accessToken ? "" : accessToken);
        params.put(ATTR_EXPIRES,(Utils.acquireUTCTimestamp()));
        return params;
    }

    protected abstract void onConvert(Map<String, String> params);

    protected abstract int acquireAttributeNumber();
}
