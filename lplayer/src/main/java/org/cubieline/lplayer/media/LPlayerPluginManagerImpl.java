package org.cubieline.lplayer.media;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by tonyley on 15/1/8.
 */
/*package*/ final class LPlayerPluginManagerImpl extends Handler implements ILPlayerPluginManager<Track>{
    private static final int STATUS_PREPARE = 0x01;
    private static final int STATUS_START = 0x02;
    private static final int STATUS_PAUSE = 0x03;
    private static final int STATUS_STOP = 0x04;
    private static final int WHAT_PREPARE = 0x101;
    private static final int WHAT_START = 0x102;
    private static final int WHAT_PAUSE = 0x103;
    private static final int WHAT_STOP = 0x104;
    private static final int WHAT_RESUME = 0x105;
    private static final int WHAT_SEEK_TO = 0x106;

    private static final String TAG = LPlayerPluginManagerImpl.class.getSimpleName();

    private int pluginCode;
    private volatile int status;
    private volatile Track workingTrack;
    private volatile boolean isRunning;
    private final HandlerThread thread;
    private volatile ILPlayerPlugin plugin;
    private final SparseArray<ILPlayerPlugin> pluginMapping;
    private final OnPluginExceptionListener pluginExceptionListener;

    LPlayerPluginManagerImpl(HandlerThread handlerThread, SparseArray<ILPlayerPlugin> pluginMapping, OnPluginExceptionListener onPluginExceptionListener){
        super(handlerThread.getLooper());
        if(null == onPluginExceptionListener){
            throw new IllegalArgumentException("The OnPluginExceptionListener can not be null");
        }

        thread = handlerThread;
        this.pluginMapping = pluginMapping;
        pluginExceptionListener = onPluginExceptionListener;
    }

    @Override
    public void dispatchMessage(Message msg) {
        isRunning = true;
        switch (msg.what){
            default : break;

            case WHAT_PREPARE :
                Track track = (Track)msg.obj;
                int mode = msg.arg1;
                doPrepare(track, mode);
                break;

            case WHAT_START :
                doStart();
                break;

            case WHAT_PAUSE :
                doPause();
                break;

            case WHAT_STOP :
                doStop();
                break;

            case WHAT_RESUME :
                doResume();
                break;

            case WHAT_SEEK_TO :
                int milliseconds = msg.arg1;
                doSeekTo(milliseconds);
                break;
        }
        isRunning = false;
    }

    private void cancel(){
        removeMessages(WHAT_PREPARE);
        removeMessages(WHAT_START);
        removeMessages(WHAT_PAUSE);
        removeMessages(WHAT_STOP);
        removeMessages(WHAT_RESUME);
        removeMessages(WHAT_SEEK_TO);
        thread.interrupt();
    }

    @Override
    public void onPrepare(Track track, int mode) {
        Log.d(TAG, "@onPrepare isRunning:" + isRunning);
        if(isRunning){
            if(STATUS_PREPARE == status){
                plugin.cancelPrepare(workingTrack);
            }
            cancel();
        }
        obtainMessage(WHAT_PREPARE, mode, 0, track).sendToTarget();
    }

    private void doPrepare(Track track, int mode){
        workingTrack = track;
        if (null == plugin || pluginCode != track.getPluginCode()) {
            pluginCode = track.getPluginCode();
            plugin = pluginMapping.get(pluginCode);
        }

        status = STATUS_PREPARE;
        Log.d(TAG, "@doPrepare begin prepare, url:" + track.getDataSource());
        try{
            plugin.onPrepare(track, mode);
        } catch(Throwable e){
            if(e instanceof InterruptedException){
                Log.e(TAG, "@doPrepare ", e);
            }else{
                pluginExceptionListener.onPluginPrepareException(pluginCode, plugin, e);
            }
        }
        Log.d(TAG, "@doPrepare prepared");
    }

    @Override
    public void onStart() {
        if(isRunning){
            cancel();
        }
        obtainMessage(WHAT_START).sendToTarget();
    }

    private void doStart(){
        status = STATUS_START;
        try{
            plugin.onStart();
        }catch (Throwable e){
            pluginExceptionListener.onPluginStartException(pluginCode, plugin, e);
        }
    }

    @Override
    public void onPause() {
        if(isRunning){
            cancel();
        }
        obtainMessage(WHAT_PAUSE).sendToTarget();
    }

    private void doPause(){
        status = STATUS_PAUSE;
        try{
            plugin.onPause();
        }catch (Throwable e){
            pluginExceptionListener.onPluginPauseException(pluginCode, plugin, e);
        }
    }

    @Override
    public void onStop() {
        if(isRunning){
            cancel();
        }
        obtainMessage(WHAT_STOP).sendToTarget();
    }

    private void doStop(){
        status = STATUS_STOP;
        try{
            plugin.onStop();
        }catch (Throwable e){
            pluginExceptionListener.onPluginStopException(pluginCode, plugin, e);
        }
    }

    @Override
    public void onResume() {
        if(isRunning){
            cancel();
        }
        obtainMessage(WHAT_RESUME).sendToTarget();
    }

    private void doResume(){
        status = STATUS_START;
        try{
            plugin.onResume();
        }catch (Throwable e){
            pluginExceptionListener.onPluginResumeException(pluginCode, plugin, e);
        }
    }

    @Override
    public void onSeekTo(int milliseconds) {
        if(isRunning){
            cancel();
        }
        obtainMessage(WHAT_SEEK_TO, milliseconds, 0).sendToTarget();
    }

    private void doSeekTo(int milliseconds){
        try{
            plugin.onSeekTo(milliseconds);
        }catch (Throwable e){
            pluginExceptionListener.onPluginSeekToException(pluginCode, plugin, e);
        }
    }

    @Override
    public void setLooping(boolean isLooping) {
        if(null != plugin){
            plugin.setLooping(isLooping);
        }
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if(null != plugin){
            plugin.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    @TargetApi(18)
    public void onRelease() {
        final int size = pluginMapping.size();
        ILPlayerPlugin plugin;
        for (int i = 0; i < size; i++) {
            plugin = pluginMapping.valueAt(i);
            plugin.onRelease();
            pluginMapping.removeAt(i);
        }

        if(18 <= Build.VERSION.SDK_INT){
            thread.quitSafely();
        }else{
            thread.quit();
        }
    }

    @Override
    public int getPluginCode() {
        return null == plugin ? -1 : plugin.getPluginCode();
    }

    @Override
    public int getCurrentPosition() {
        return null == plugin ? -1 : plugin.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return null == plugin ? -1 : plugin.getDuration();
    }

    @Override
    public boolean isLooping() {
        return null == plugin ? false : plugin.isLooping();
    }
}