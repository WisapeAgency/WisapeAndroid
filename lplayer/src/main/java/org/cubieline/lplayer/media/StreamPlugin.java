package org.cubieline.lplayer.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import static org.cubieline.lplayer.media.ILPlayer.MODE_PLAY_SINGLE_LOOP;

/**
 * Created by LeiGuoting on 14/11/10.
 */
public class StreamPlugin extends MediaPlayerPlugin {

    private static final String TAG = "StreamPlugin";

    private MediaPlayer mediaPlayer;

    public StreamPlugin(Context context, OnPluginListener onPlayerPluginListener){
        super(context, onPlayerPluginListener);
        mediaPlayer = new MediaPlayer();
        setMediaPlayerSetting(mediaPlayer);
        setMediaPlayerListener(mediaPlayer);
    }

    @Override
    public int getPluginCode() {
        return PLUGIN_CODE_DEFAULT;
    }

    @Override
    protected MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    protected void prepareForStart(int mode) {
        final MediaPlayer mediaPlayer = getMediaPlayer();
        mediaPlayer.reset();

        String dataSource = parseDataSource(track);
        if(null == dataSource || 0 == dataSource.length()){
            onPlayerPluginListener.onPluginError(ILPlayerPlugin.ERROR_CODE_FILE_NOT_FOUND);
            return;
        }

        final Track cancelTrack = cancelPrepareTrack;
        if(null != cancelTrack && cancelTrack.getTrackId() == track.getTrackId()){
            cancelPrepareTrack = null;
            return;
        }

        try{
            mediaPlayer.setDataSource(dataSource);
        }catch(IOException e){
            onPlayerPluginListener.onPluginPrepareError(e);
            return;
        }catch(IllegalArgumentException e){
            onPlayerPluginListener.onPluginPrepareError(e);
            return;
        }catch(SecurityException e){
            onPlayerPluginListener.onPluginPrepareError(e);
            return;
        }catch(IllegalStateException e){
            onPlayerPluginListener.onPluginPrepareError(e);
            return;
        }

        if (MODE_PLAY_SINGLE_LOOP == mode) {
            mediaPlayer.setLooping(true);
        }
        mediaPlayer.prepareAsync();
    }

    protected String parseDataSource(Track track){
        return track.getDataSource();
    }

    @Override
    public void onRelease() {
        super.onRelease();
        mediaPlayer = null;
    }
}