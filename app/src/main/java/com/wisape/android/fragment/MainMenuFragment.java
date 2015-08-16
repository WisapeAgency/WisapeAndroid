package com.wisape.android.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.R;
import com.wisape.android.activity.AboutActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.MessageCenterActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.common.UserManager;
import com.wisape.android.content.DynamicBroadcastReceiver;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.FrescoFactory;
import com.wisape.android.widget.ComfirmDialog;

import java.io.File;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class MainMenuFragment extends AbsFragment implements DynamicBroadcastReceiver.OnDynamicBroadcastReceiverListener {
    private static final String TAG = MainMenuFragment.class.getSimpleName();

    @InjectView(R.id.sdv_user_head_image)
    SimpleDraweeView sdvUserHeadImage;
    @InjectView(R.id.tv_name)
    TextView tvName;
    @InjectView(R.id.tv_mail)
    TextView tvMail;
    @InjectView(R.id.message_count)
    TextView tvMsgAccount;

    private UserCallback callback;
    private LocalBroadcastManager localBroadcastManager;
    private DynamicBroadcastReceiver localReceiver;

    @Override
    public void onReceiveBroadcast(Context context, Intent intent) {
        if (isDetached()) {
            return;
        }

        String action = intent.getAction();
        if (null == action || 0 == action.length()) {
            return;
        }

        if (UserProfileActivity.ACTION_PROFILE_UPDATED.equals(action)) {
            UserInfo newUser = intent.getParcelableExtra(MainActivity.EXTRA_USER_INFO);
            refreshUI(newUser);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof UserCallback) {
            callback = (UserCallback) activity;
        }

        if (null == callback) {
            throw new IllegalStateException("The UserCallback can not be null.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mobihelp.init(getActivity(), new MobihelpConfig(getString(R.string.freshdesk_domain), getString(R.string.freshdesk_key), getString(R.string.freshdesk_secret)));
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localReceiver = new DynamicBroadcastReceiver(this);
        localBroadcastManager.registerReceiver(localReceiver, new IntentFilter(UserProfileActivity.ACTION_PROFILE_UPDATED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_menu, null, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        UserInfo user = callback.getUserInfo();
        refreshUI(user);
    }

    private void refreshUI(UserInfo user) {
        String icon = user.user_ico_n;
        Log.d(TAG, "#onViewCreated icon:" + icon);
        if (null != icon && 0 < icon.length()) {
            FrescoFactory.bindImageFromUri(sdvUserHeadImage, icon);
        }

        tvName.setText(user.nick_name);
        tvMail.setText(user.user_email);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != localBroadcastManager) {
            localBroadcastManager.unregisterReceiver(localReceiver);
            localReceiver.destroy();
            localReceiver = null;
            localBroadcastManager = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (null != callback) {
            callback = null;
        }
    }

    @OnClick(R.id.help_center)
    @SuppressWarnings("unused")
    protected void onHelpCenterClick(View view) {
        Mobihelp.showSupport(getActivity());
    }

    @OnClick(R.id.tv_name)
    @SuppressWarnings("unused")
    protected void onNameClicked() {
        UserProfileActivity.launch(this, callback.getUserInfo(), UserProfileActivity.REQUEST_CODE_PROFILE);
    }

    /**
     * 清除缓存
     */
    @OnClick(R.id.clear_buffer)
    @SuppressWarnings("unused")
    protected void onClearBufferClicked() {
        final ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.clear_buffer)
                , getString(R.string.clear_cache_notice_text));
        comfirmDialog.show(getFragmentManager(), "clear");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                clearCache();
                comfirmDialog.dismiss();
            }
        });
    }

    /**
     * 清楚缓存
     */
    private void clearCache(){
        Mobihelp.clearUserData(getActivity());
        File file =  EnvironmentUtils.getAppCacheDirectory();
        FileUtils.deleteFileInDir(file);
    }

    @OnClick(R.id.exit)
    @SuppressWarnings("unused")
    protected void onLogoutClicked() {
        ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.exit_login), getString(R.string.exit_login_notice_text));
        comfirmDialog.show(getFragmentManager(), "logout");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                clearCache();
                UserManager.instance().clearUser(getActivity());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    @OnClick(R.id.about)
    @SuppressWarnings("unused")
    protected void doAboutClicked() {
        AboutActivity.launch(this);
    }

    @OnClick(R.id.message_center)
    @SuppressWarnings("unused")
    protected void onMessageCenterClicked(){
        MessageCenterActivity.launch(getActivity());
    }

    public interface UserCallback {
        UserInfo getUserInfo();
    }

    //测试方法
    public static void updataMsgCount(){
       Log.e(TAG,"更新消息数量");
    }

    private void sendNotifacation(Context context){
        // 在Android进行通知处理，首先需要重系统哪里获得通知管理器NotificationManager，它是一个系统Service。
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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
