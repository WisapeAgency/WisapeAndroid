package com.wisape.android.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by LeiGuoting on 5/6/15.
 */
public class VolleyHelper{
    private static RequestQueue queue;
    private static volatile boolean initialized;

    public static void initialize(Context context){
        if(initialized){
            return;
        }
        queue = Volley.newRequestQueue(context.getApplicationContext(), new OkUrlHttpStack());
        initialized = true;
    }

    public static RequestQueue getRequestQueue(){
        if(!initialized){
            throw new IllegalStateException("The RequestQueue don't initialize.");
        }
        return queue;
    }
}
