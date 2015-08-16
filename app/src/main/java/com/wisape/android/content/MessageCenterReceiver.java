package com.wisape.android.content;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wisape.android.R;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.MessageCenterDetailActivity;
import com.wisape.android.fragment.MainMenuFragment;

/**
 * 消息中心，消息接收处理
 * Created by lenovo on 2015/8/15.
 */
public class MessageCenterReceiver extends BroadcastReceiver{

    private static final String MESSAGE_RECEIVER_ACTION = "com.wisape.android.content.MessageCenterReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(null != context && MESSAGE_RECEIVER_ACTION == intent.getAction()){
            sendNotifacation(context);
            MainMenuFragment.updataMsgCount();
        }
    }

    private void sendNotifacation(Context context){
        // 在Android进行通知处理，首先需要重系统哪里获得通知管理器NotificationManager，它是一个系统Service。
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0,
                new Intent(context, MessageCenterDetailActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // 通过Notification.Builder来创建通知，注意API Level
        // API11之后才支持
        Notification notify2 = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.logo) // 设置状态栏中的小图片，尺寸一般建议在24×24，这个图片同样也是在下拉状态栏中所显示，如果在那里需要更换更大的图片，可以使用setLargeIcon(Bitmap
                        // icon)
                .setTicker("TickerText:" + "您有新短消息，请注意查收！")// 设置在status
                        // bar上显示的提示文字
                .setContentTitle("Notification Title")// 设置在下拉status
                        // bar后Activity，本例子中的NotififyMessage的TextView中显示的标题
                .setContentText("This is the notification message")// TextView中显示的详细内容
                .setContentIntent(pendingIntent2) // 关联PendingIntent
                .setNumber(1) // 在TextView的右方显示的数字，可放大图片看，在最右侧。这个number同时也起到一个序列号的左右，如果多个触发多个通知（同一ID），可以指定显示哪一个。
                .getNotification(); // 需要注意build()是在API level
        // 16及之后增加的，在API11中可以使用getNotificatin()来代替
        notify2.flags |= Notification.FLAG_AUTO_CANCEL;
        notify2.defaults = Notification.DEFAULT_ALL;
        manager.notify(1, notify2);
    }

}
