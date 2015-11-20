package com.wisape.android.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MessageCenterDetailActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date currentDate = new Date();
        String utcTime = utcDateFormat.format(currentDate);
        return utcTime;
    }


    public static String base64ForImage(Uri uri) {

       return FileUtils.base64ForImage(uri.getPath());

//        InputStream inputStream;
//        try {
//            inputStream = new BufferedInputStream(new FileInputStream(uri.getPath()));
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
//
//        ByteArrayOutputStream outputStream = null;
//        try {
//            outputStream = new ByteArrayOutputStream(102400 * 2);
//            byte[] buffer = new byte[102400];
//            int count;
//            for (; 0 < (count = inputStream.read(buffer)); ) {
//                outputStream.write(buffer, 0, count);
//            }
//
//            byte[] data = outputStream.toByteArray();
//
//            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
//            return base64;
//        } catch (IOException e) {
//            return "";
//        } finally {
//            try {
//                if (null != inputStream) {
//                    inputStream.close();
//                }
//
//                if (null != outputStream) {
//                    outputStream.close();
//                }
//            } catch (IOException e) {
//                //do nothing
//            }
//        }
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

    public static String getCountry(Context context) {
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

    public static void sendNotificatio(Context context, Class<? extends BaseActivity> activity, int msgId, String msgTile, String msgSubject) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = getIntent(context, activity, msgId);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notify = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.app_logo)
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

    private static Intent getIntent(Context context, Class<? extends BaseActivity> activity, int messageID) {
        Intent intent = new Intent(context, activity);
        intent.putExtra(MessageCenterDetailActivity.MESSAGE_ID, messageID);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private static final int BLACK = 0xff000000;
    private static final int WITHE = 0xffffffff;

    public static void clipText(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT > 11) {
            ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setPrimaryClip(ClipData.newPlainText("http://www.baiud.com", text));
        } else {
            android.text.ClipboardManager c = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            c.setText(text);
        }
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void loadImg(Context context, String imgPath, ImageView imageView) {
        if (imgPath.contains("http")) {
            Glide.with(context).load(imgPath)
                    .placeholder(R.mipmap.icon_camera)
                    .error(R.mipmap.icon_login_email)
                    .override(600, 800)
                    .into(imageView);
        } else {
//            Picasso.with(context).invalidate(imgPath);
            Glide.with(context).load(new File(imgPath))
                    .override(600, 800)
                    .placeholder(R.mipmap.icon_camera)
                    .error(R.mipmap.icon_login_email)
                    .into(imageView);
        }
    }

}
