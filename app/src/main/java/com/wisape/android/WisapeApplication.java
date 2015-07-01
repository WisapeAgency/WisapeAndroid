package com.wisape.android;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.network.VolleyHelper;

/**
 * @author Duke
 */
public class WisapeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();
        VolleyHelper.initialize(context);
        Fresco.initialize(context);
        Resources res = getResources();
        Mobihelp.init(this, new MobihelpConfig(res.getString(R.string.freshdesk_domain), res.getString(R.string.freshdesk_key), res.getString(R.string.freshdesk_secret)));
    }
}
