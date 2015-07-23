package org.cubieline.lplayer.common;

import android.util.Log;

import org.cubieline.lplayer.media.ILPlayer;

/**
 * Created by LeiGuoting on 14/11/19.
 */


public class PlayerModeConfigHelper {

    public static int[] configMode(String[] configsFromClient){
        int size = 4;
        boolean hasConfig = false;
        if(null != configsFromClient && 0 < configsFromClient.length){
            size = configsFromClient.length;
            hasConfig = true;
        }

        int [] modeArray = null;
        if(hasConfig){
            int index = 0;
            modeArray = new int[size];
            for(String modeStr : configsFromClient){
                Log.d("ModeConfigHelper", "@configMode mode:" + modeStr + ", index:" + index);
                if(ILPlayer.MODE_CONFIG_LOOP.equals(modeStr)){
                    modeArray[index ++] = ILPlayer.MODE_PLAY_LOOP;
                }

                else if(ILPlayer.MODE_CONFIG_ORDER.equals(modeStr)){
                    modeArray[index ++] = ILPlayer.MODE_PLAY_ORDER;
                }

                else if(ILPlayer.MODE_CONFIG_RANDOM.equals(modeStr)){
                    modeArray[index ++] = ILPlayer.MODE_PLAY_RANDOM;
                }

                else if(ILPlayer.MODE_CONFIG_SINGLE_LOOP.equals(modeStr)){
                    modeArray[index ++] = ILPlayer.MODE_PLAY_SINGLE_LOOP;
                }

                else{
                    hasConfig = false;
                    break;
                }
            }
        }

        if(!hasConfig){
            /*default*/
            modeArray = new int[]{ILPlayer.MODE_PLAY_LOOP, ILPlayer.MODE_PLAY_ORDER, ILPlayer.MODE_PLAY_SINGLE_LOOP, ILPlayer.MODE_PLAY_RANDOM};
        }
        return modeArray;
    }
}