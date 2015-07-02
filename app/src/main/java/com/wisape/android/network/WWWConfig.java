package com.wisape.android.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import com.wisape.android.R;

import java.util.Locale;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public class WWWConfig{
    private static String schema;
    private static String authority;
    public static int timeoutMills;

    public static void initialize(Context context){
        Resources res = context.getResources();
        schema = res.getString(R.string.www_schema);
        String host = res.getString(R.string.www_host);
        String port = res.getString(R.string.www_port);
        String version = res.getString(R.string.www_version);
        authority = String.format(Locale.US, "%1$s:%2$s/%3$s", host, port, version);
        timeoutMills = Integer.parseInt(res.getString(R.string.www_timeout));

        //initialize other about www config
        VolleyHelper.initialize(context);
    }

    public static Uri acquireUri(String path){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(schema);
        builder.authority(authority);
        builder.path(path);
        return builder.build();
    }
}
