package com.wisape.android.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.MessageCenterDetailActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    public static void dumpThrowable(Throwable ex, StringBuilder builder) {
        builder.append(ex.toString()).append("\r\n");
        builder.append("Stack Trace:\r\n");
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            builder.append(element).append("\r\n");
        }

        Throwable cause = ex.getCause();
        if (null != cause) {
            builder.append("Cause:\r\n");
            dumpThrowable(cause, builder);
        }
    }

    public static String acquireUTCTimestamp() {
        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        Date currentDate = new Date();
        String utcTime = utcDateFormat.format(currentDate);
        return utcTime;
    }


    public static String base64ForImage(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(uri.getPath()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream(102400 * 2);
            byte[] buffer = new byte[102400];
            int count;
            for (; 0 < (count = inputStream.read(buffer)); ) {
                outputStream.write(buffer, 0, count);
            }

            byte[] data = outputStream.toByteArray();

            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            return base64;
        } catch (IOException e) {
            return "";
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }

                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    public static String acquireCountryIso(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String code;

        int phoneType = telephonyManager.getPhoneType();
        /*
         * Availability: Only when user is registered to a network. Result may be unreliable on CDMA networks (use getPhoneType() to determine if on a CDMA network).
         */
        if (TelephonyManager.PHONE_TYPE_NONE != phoneType && TelephonyManager.PHONE_TYPE_CDMA != phoneType) {
            code = telephonyManager.getNetworkCountryIso();
            Log.d("Utils", "#acquireCountryIso from TelephonyManager; code:" + code);
        } else {
            code = context.getResources().getConfiguration().locale.getCountry();
            Log.d("Utils", "#acquireCountryIso from Resources.Configuration.locale; code:" + code);
        }
        return code;
    }

    public static String getCountry(Context context){
        return context.getResources().getConfiguration().locale.getCountry();
    }

    public boolean isAppForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }
        return true;
    }

    public static void sendNotificatio(Context context,Class<? extends BaseActivity> activity,int msgId,String msgTile,String msgSubject){

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = getIntent(context, activity, msgId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notify = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.logo)
                .setTicker(msgTile)
                .setContentTitle(msgTile)
                .setContentText(msgSubject)
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .getNotification();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.defaults = Notification.DEFAULT_ALL;
        manager.notify(1, notify);
    }

    private static Intent getIntent(Context context,Class<? extends BaseActivity> activity,int messageID){
        Intent intent = new Intent(context,activity);
        intent.putExtra(MessageCenterDetailActivity.MESSAGE_ID,messageID);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }



}
