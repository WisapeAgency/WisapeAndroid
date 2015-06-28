package com.wisape.android.util;

import android.net.Uri;

import static android.content.ContentResolver.SCHEME_FILE;
/**
 * Created by LeiGuoting on 18/6/15.
 */
public class FrescoUriUtils {

    public static Uri fromFilePath(String path){
        return new Uri.Builder().scheme(SCHEME_FILE).authority("").path(path).build();
    }

    public static Uri fromResId(int resId){
        return new Uri.Builder().scheme("res").path(Integer.toString(resId)).build();
    }
}
