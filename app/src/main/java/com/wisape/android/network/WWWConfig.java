package com.wisape.android.network;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import com.wisape.android.R;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public class WWWConfig{
    private static String schema;
    private static String authority;
    private static String PATH;
    private static String version;
    public static int timeoutMills;

    public static void initialize(Context context){
        Resources res = context.getResources();
        schema = res.getString(R.string.www_schema);
        String host = res.getString(R.string.www_host);
        String port = res.getString(R.string.www_port);
        PATH = res.getString(R.string.www_path);
        authority = String.format("%1$s:%2$s", host, port);

        version = res.getString(R.string.www_version);
        timeoutMills = Integer.parseInt(res.getString(R.string.www_timeout_mills));

        //initialize other about www config
        VolleyHelper.initialize(context);
    }

    public static Uri acquireUri(String path){
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(schema);
        builder.encodedAuthority(authority);
        builder.encodedPath(PATH);
//        builder.encodedPath(version);
        builder.appendEncodedPath(path);
        return builder.build();
    }
}
