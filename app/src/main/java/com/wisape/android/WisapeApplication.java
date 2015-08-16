package com.wisape.android;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.service.NanoService;

import org.cubieline.lplayer.PlayerProxy;

/**
 * @author Duke
 */
public class WisapeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();
        Fresco.initialize(context);
        WWWConfig.initialize(context);
        NanoService.startNanoServer(context);
        PlayerProxy.launch(context);

        //初始化parse通讯
        Parse.initialize(getApplicationContext(), "L3WrrhBJmbPhRoJ4GYIUDMIErlR8IlvkJuQQJ0Px", "yfC5kFI4jLLeeDaKlepK1hgAGiYJJEHjXfnpaCks");
        PushService.subscribe(this, "", MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
