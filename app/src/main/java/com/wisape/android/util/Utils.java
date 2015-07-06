package com.wisape.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utils
 * Created by Xugm on 15/6/16.
 */
public class Utils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isEmail(String value) {
        return value.matches("[_a-z\\d\\-\\./]+@[_a-z\\d\\-]+(\\.[_a-z\\d\\-]+)*(\\.(info|biz|com|edu|gov|net|am|bz|cn|cx|hk|jp|tw|vc|vn))$");
    }

    public static String getAuthString(String openid) {
        String result = "wisape" + openid + "2015612";
        return SecurityUtils.md5(result);
    }

    public static void dumpThrowable(Throwable ex, StringBuilder builder){
        builder.append(ex.toString()).append("\r\n");
        builder.append("Stack Trace:\r\n");
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for(StackTraceElement element : stackTraceElements){
            builder.append(element).append("\r\n");
        }

        Throwable cause = ex.getCause();
        if(null != cause){
            builder.append("Cause:\r\n");
            dumpThrowable(cause, builder);
        }
    }

    public static long acquireUTCTimestamp(){
        DateFormat df = DateFormat.getTimeInstance();
        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yy/MM/dd hh:mm:ss.SSS");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date currentDate = new Date();
        String utcTime = utcDateFormat.format(currentDate);
        Log.d("Utils", "#acquireUTCTimestamp current timestamp:" + currentDate.getTime());
        SimpleDateFormat normalDateFormat = new SimpleDateFormat("yy/MM/dd hh:mm:ss.SSS");
        long utcTimestamp;
        try{
            Date date = normalDateFormat.parse(utcTime);
            utcTimestamp = date.getTime();
        }catch (ParseException e){
            Log.e("Utils", "#acquireUTCTimestamp ", e);
            throw new IllegalStateException(e);
        }
        Log.d("Utils", "#acquireUTCTimestamp UTC timestamp:" + utcTimestamp);
        return utcTimestamp;
    }
}
