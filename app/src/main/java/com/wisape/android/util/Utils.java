package com.wisape.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.memory.PooledByteArrayBufferedInputStream;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        Date currentDate = new Date();
        String utcTime = utcDateFormat.format(currentDate);
        Log.d("Utils", "#acquireUTCTimestamp current timestamp:" + currentDate.getTime());
        SimpleDateFormat normalDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
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

    public static String base64ForImage(Uri uri){
        InputStream inputStream;
        try{
            inputStream = new BufferedInputStream(new FileInputStream(uri.getPath()));
        }catch (IOException e){
            throw new IllegalStateException(e);
        }

        ByteArrayOutputStream outputStream = null;
        try{
            outputStream = new ByteArrayOutputStream(102400 * 2);
            byte [] buffer = new byte[102400];
            int count;
            for(; 0 < (count = inputStream.read(buffer));){
                outputStream.write(buffer, 0, count);
            }

            byte [] data = outputStream.toByteArray();

            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            return base64;
        }catch (IOException e){
            return "";
        }finally {
            try {
                if(null != inputStream){
                    inputStream.close();
                }

                if(null != outputStream){
                    outputStream.close();
                }
            }catch (IOException e){
                //do nothing
            }
        }
    }

    public static String acquireCountryIso(Context context){
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String code;

        int phoneType = telephonyManager.getPhoneType();
        /*
         * Availability: Only when user is registered to a network. Result may be unreliable on CDMA networks (use getPhoneType() to determine if on a CDMA network).
         */
        if(TelephonyManager.PHONE_TYPE_NONE != phoneType && TelephonyManager.PHONE_TYPE_CDMA != phoneType){
            code = telephonyManager.getNetworkCountryIso();
            Log.d("Utils", "#acquireCountryIso from TelephonyManager; code:" + code);
        }else{
            code = context.getResources().getConfiguration().locale.getCountry();
            Log.d("Utils", "#acquireCountryIso from Resources.Configuration.locale; code:" + code);
        }
        return code;
    }
}
