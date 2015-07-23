package org.cubieline.lplayer.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by LeiGuoting on 14/11/18.
 */
public class LPlayerReceiver extends BroadcastReceiver {

    private OnLPlayerBroadcastReceiverListener onReceiverListener;

    private volatile boolean isDestroyed;

    public LPlayerReceiver(OnLPlayerBroadcastReceiverListener OnReceiverListener){
        if(null == OnReceiverListener){
            throw new IllegalArgumentException("The OnLPlayerBroadcastReceiverListener can not be null.");
        }

        this.onReceiverListener = OnReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(isDestroyed){
            return ;
        }

        onReceiverListener.onLPlayerReceive(context, intent);
    }

    public void onDestroy(){
        isDestroyed = true;
        onReceiverListener = null;
    }


    public interface OnLPlayerBroadcastReceiverListener{
        public void onLPlayerReceive(Context context, Intent intent);
    }
}
