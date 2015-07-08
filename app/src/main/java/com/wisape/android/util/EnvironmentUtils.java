package com.wisape.android.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by LeiGuoting on 8/7/15.
 */
public class EnvironmentUtils {
    private static final String EBACKUP_ROOT = "wisape" + File.separator + "com.wisape.android";

    public static final String DIR_DATA = "data";

    public static final String DIR_TEMPORARY = "temporary";

    public static final String DIR_CACHE = "cache";

    public static boolean isMounted(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getAppExternalRootDirectory(){
        File extRootDir = Environment.getExternalStorageDirectory();
        File appRoot = new File(extRootDir,EBACKUP_ROOT);
        if(!appRoot.exists()){
            appRoot.mkdirs();
        }

        return appRoot;
    }

    public static File getAppDirectory(String type){
        File directory = new File(getAppExternalRootDirectory(),type);
        if(! directory.exists()){
            directory.mkdirs();
        }

        return directory;
    }

    public static File getAppTemporaryDirectory(){
        return getAppDirectory(DIR_TEMPORARY);
    }
}
