package org.cubieline.lplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.cubieline.lplayer.app.LPlayerService;
import org.cubieline.lplayer.media.ILPlayerClient;
import org.cubieline.lplayer.media.ILPlayerLightClient;
import org.cubieline.lplayer.media.ITracksClient;
import org.cubieline.lplayer.media.LPlayerStartCallback;
import org.cubieline.lplayer.media.Track;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by LeiGuoting on 14/11/10.
 */
public class PlayerProxy implements ServiceConnection, ILPlayerClient<Track>, ITracksClient<Track>, LPlayerExceptionListener{

    private static final String TAG = PlayerProxy.class.getSimpleName();

    private static PlayerProxy proxy;
    private static NotificationCallback notificationCallback;
    private static RemoteControlCallback remoteControlCallback;
    private static LPlayerStartCallback playerStartCallback;
    private static final List<LPlayerExceptionListener> playerExceptionListenerList = new ArrayList(3);

    private LPlayerService.LPlayerBinder serviceProxy;
    private volatile boolean isServiceConnected;

    private PlayerProxy(){}

    public static void launch(Context context){
        if(isLaunched()){
            return;
        }

        proxy = new PlayerProxy();
        Intent intent = new Intent(context, LPlayerService.class);
        context.bindService(intent, proxy, Context.BIND_AUTO_CREATE);
    }

    public static void registerNotificationCallback(NotificationCallback callback){
        notificationCallback = callback;
    }

    public static void registerRemoteControlCallback(RemoteControlCallback callback){
        remoteControlCallback = callback;
    }

    public static void registerPlayerStartCallback(LPlayerStartCallback callback){
        playerStartCallback = callback;
    }

    public static void registerPlayerExceptionListener(LPlayerExceptionListener exceptionListener){
        if(!playerExceptionListenerList.contains(exceptionListener)){
            playerExceptionListenerList.add(exceptionListener);
        }
    }

    public static void unregisterPlayerExceptionListener(LPlayerExceptionListener exceptionListener){
        playerExceptionListenerList.remove(exceptionListener);
    }

    public static boolean isLaunched(){
        return null != proxy && proxy.isServiceConnected;
    }

    public static void destroy(Context context){
        if(!isLaunched()){
            throw new IllegalStateException("The PlayerProxy had be destroyed.");
        }

        proxy.isServiceConnected = false;
        notificationCallback = null;
        remoteControlCallback = null;
        playerStartCallback = null;
        context.unbindService(proxy);
        proxy = null;
    }

    public static ILPlayerClient getLPlayerClient() throws IllegalStateException {
        if(!isLaunched()){
            throw new IllegalStateException("The PlayerProxy did not initialized, You need to call launch method for initialize PlayerProxy");
        }

        return proxy;
    }

    public static ILPlayerLightClient getLPlayerLightClient() throws IllegalStateException {
        if(!isLaunched()){
            throw new IllegalStateException("The PlayerProxy did not initialized, You need to call launch method for initialize PlayerProxy");
        }

        return proxy;
    }

    public static ITracksClient getTracksClient() throws IllegalStateException {
        if(!isLaunched()){
            throw new IllegalStateException("The PlayerProxy did not initialized, You need to call launch method for initialize PlayerProxy");
        }

        return proxy;
    }

    /**
     * Called when a connection to the Service has been established, with
     * the {@link android.os.IBinder} of the communication channel to the
     * Service.
     *
     * @param componentName    The concrete component name of the service that has
     *                been connected.
     * @param service The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Log.d(TAG, "@onServiceConnected componentName:" + componentName.toShortString());
        Log.d(TAG, "@onServiceConnected LPlayerService.Name:" + LPlayerService.class.getName());
        Log.d(TAG, "@onServiceConnected componentName.ClassName:" + componentName.getClassName());
        if(!LPlayerService.class.getName().equals(componentName.getClassName())){
            return;
        }

        if(service instanceof LPlayerService.LPlayerBinder){
            serviceProxy = (LPlayerService.LPlayerBinder) service;

            LPlayerService playerService = serviceProxy.getService();
            playerService.setExceptionListener(this);
            if(null != notificationCallback){
                playerService.registerNotificationCallback(notificationCallback);
            }

            if (remoteControlCallback !=null){
                playerService.registerRemoteControlCallback(remoteControlCallback);
            }

            if(null != playerStartCallback){
                playerService.registerLPlayerStartCallback(playerStartCallback);
            }

            isServiceConnected = true;
            Log.d(TAG, "@onServiceConnected  LPlayerService bind !");
        }
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     *             connection has been lost.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "@onServiceDisconnected __________ The " + name.getClassName() + " disconnected !!");
        if(LPlayerService.class.getSimpleName().equals(name.getClassName())){
            isServiceConnected = false;
            serviceProxy = null;
            proxy = null;
        }
    }

    @Override
    public int switch2NextMode() {
        return serviceProxy.switch2NextMode();
    }

    @Override
    public void switchMode(int mode) {
        serviceProxy.switchMode(mode);
    }

    @Override
    public int getMode() {
        return serviceProxy.getMode();
    }

    @Override
    public int getNextMode() {
        return serviceProxy.getNextMode();
    }

    @Override
    public void start() {
        serviceProxy.start();
    }

    @Override
    public void start(int position) {
        serviceProxy.start(position);
    }

    @Override
    public void pause() {
        serviceProxy.pause();
    }

    @Override
    public void next() {
        serviceProxy.next();
    }

    @Override
    public void previous() {
        serviceProxy.previous();
    }

    /**
     * Seeks to specified time position.
     *
     * @param seconds the offset in milliseconds from the start to seek to.
     */
    @Override
    public void seekTo(int seconds) {
        serviceProxy.seekTo(seconds);
    }

    @Override
    public Track[] obtainTracks() {
        return serviceProxy.obtainTracks();
    }

    @Override
    public int getClientStatus() {
        return serviceProxy.getClientStatus();
    }

    @Override
    public int[] getDurationAndPosition() {
        return serviceProxy.getDurationAndPosition();
    }

    @Override
    public int getFrom() {
        return serviceProxy.getFrom();
    }

    @Override
    public long getSecondId() {
        return serviceProxy.getSecondId();
    }

    @Override
    public Track getTrack() {
        return serviceProxy.getTrack();
    }

    @Override
    public int getTrackIndex() {
        return serviceProxy.getTrackIndex();
    }

    @Override
    public int getTracksSize() {
        return serviceProxy.getTracksSize();
    }

    @Override
    public boolean addTracks(Track[] tracks, int position, int from, long secondId) {
        return serviceProxy.addTracks(tracks, position, from, secondId);
    }

    @Override
    public boolean addTrack(Track track, int from, long secondId) {
        return serviceProxy.addTrack(track, from, secondId);
    }

    @Override
    public void addTracksAndPlay(Track[] tracks, int position, int from, long secondId) {
        serviceProxy.addTracksAndPlay(tracks, position, from, secondId);
    }

    @Override
    public void addTrackAndPlay(Track track, int from, long secondId) {
        serviceProxy.addTrackAndPlay(track, from, secondId);
    }

    @Override
    public boolean onPrepareException(final int pluginCode, final Throwable e) {
        final int size = playerExceptionListenerList.size();
        if(0 == size){
            return false;
        }

        final Message returnValMsg = Message.obtain();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                boolean isPlayNext;
                boolean returnVal = false;
                LPlayerExceptionListener[] exceptionListenerArray = playerExceptionListenerList.toArray(new LPlayerExceptionListener[size]);
                for(LPlayerExceptionListener exceptionListener : exceptionListenerArray){
                    isPlayNext = exceptionListener.onPrepareException(pluginCode, e);
                    if(isPlayNext && !returnVal){
                        returnVal = true;
                    }
                }
                returnValMsg.arg1 = (returnVal ? 1 : 0);
            }
        });
        return 1 == returnValMsg.arg1;
    }

    @Override
    public boolean onStartException(final int pluginCode, final Throwable e) {
        final int size = playerExceptionListenerList.size();
        if(0 == size){
            return false;
        }

        final Message returnValMsg = Message.obtain();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                boolean isPlayNext;
                boolean returnVal = false;
                LPlayerExceptionListener[] exceptionListenerArray = playerExceptionListenerList.toArray(new LPlayerExceptionListener[size]);
                for(LPlayerExceptionListener exceptionListener : exceptionListenerArray){
                    isPlayNext = exceptionListener.onStartException(pluginCode, e);
                    if(isPlayNext && !returnVal){
                        returnVal = true;
                    }
                }
                returnValMsg.arg1 = (returnVal ? 1 : 0);
            }
        });

        return 1 == returnValMsg.arg1;
    }
}
