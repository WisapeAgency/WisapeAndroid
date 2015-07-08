package com.wisape.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.wisape.android.network.NanoServer;

/**
 * Created by LeiGuoting on 8/7/15.
 */
public class NanoService extends IntentService {
    public static final String ACTION_START_NANO_SERVER = "start_nano_server";
    public static final String ACTION_STOP_NANO_SERVER = "stop_nano_server";

    public static void startNanoServer(Context context){
        Intent intent = new Intent(context, NanoService.class);
        intent.setAction(ACTION_START_NANO_SERVER);
        context.startService(intent);
    }

    public static void stopNanoServer(Context context){
        Intent intent = new Intent(context, NanoService.class);
        intent.setAction(ACTION_STOP_NANO_SERVER);
        context.startService(intent);
    }

    private NanoServer server;

    public NanoService() {
        super("nano-http-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return;
        }

        if(ACTION_START_NANO_SERVER.equals(action)){
            server = NanoServer.launch();
        }else if(ACTION_STOP_NANO_SERVER.equals(action)){
            NanoServer nano = this.server;
            if(null == nano){
                nano = NanoServer.launch();
            }else{
                server = null;
            }

            if(nano.isAlive()){
                nano.stop();
            }
        }
    }
}
