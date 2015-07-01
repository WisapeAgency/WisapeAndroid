package com.wisape.android;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
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
    }
}
