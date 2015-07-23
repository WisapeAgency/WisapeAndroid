package com.wisape.android;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.j256.ormlite.android.apptools.OpenHelperManager;
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
    }
}
