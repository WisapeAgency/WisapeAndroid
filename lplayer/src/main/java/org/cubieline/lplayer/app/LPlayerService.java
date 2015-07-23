/**
 *
 */
package org.cubieline.lplayer.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.cubieline.lplayer.LPlayerExceptionListener;
import org.cubieline.lplayer.NotificationCallback;
import org.cubieline.lplayer.RemoteControlCallback;
import org.cubieline.lplayer.content.LPlayerReceiver;
import org.cubieline.lplayer.media.ILPlayerClient;
import org.cubieline.lplayer.media.ITracksClient;
import org.cubieline.lplayer.media.LPlayer;
import org.cubieline.lplayer.media.LPlayerStartCallback;
import org.cubieline.lplayer.media.Track;

import static org.cubieline.lplayer.media.IClientNotificationManager.ACTION_PLAYER_STATUS_CHANGED;
import static org.cubieline.lplayer.media.IClientNotificationManager.ACTION_TRACK_CHANGED;
import static org.cubieline.lplayer.media.ICommand.CMD_CLOSE;
import static org.cubieline.lplayer.media.ICommand.CMD_LOGOUT;
import static org.cubieline.lplayer.media.ICommand.CMD_NEXT;
import static org.cubieline.lplayer.media.ICommand.CMD_PAUSE;
import static org.cubieline.lplayer.media.ICommand.CMD_PREVIOUS;
import static org.cubieline.lplayer.media.ICommand.CMD_START;
import static org.cubieline.lplayer.media.IKey._CMD;


/**
 * @author LeiGuoting
 */
public class LPlayerService extends Service implements LPlayerReceiver.OnLPlayerBroadcastReceiverListener, LPlayerStartCallback{
	public static final String ACTION_CONTROL	= "org.cubieline.lplayer.action.ACTION_PLAYER_CONTROL";
	private static final String TAG = LPlayerService.class.getSimpleName();

    private int foregroundNotificationId;
    private int remoteControlFlagId;

    private LPlayerBinder binder;
    private LPlayerReceiver playerReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private NotificationCallback notificationCallback;
    private RemoteControlCallback remoteControlCallback;
    private HeadsetPlugBroadcastReceiver headsetPlugReceiver;
    private LPlayerStartCallback startCallback;
    private volatile boolean isDestroyed;

	@Override
	public void onCreate() {
		super.onCreate();
		headsetPlugReceiver = new HeadsetPlugBroadcastReceiver();
		registerReceiver(headsetPlugReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        playerReceiver = new LPlayerReceiver(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
	    IntentFilter filter = new IntentFilter(ACTION_PLAYER_STATUS_CHANGED);
		filter.addAction(ACTION_TRACK_CHANGED);
		localBroadcastManager.registerReceiver(playerReceiver, filter);

        try {
            HandlerThread playerServiceThread = new HandlerThread("L-player-service", Thread.MIN_PRIORITY);
            playerServiceThread.start();
            LPlayer player = new LPlayer(getApplicationContext(), playerServiceThread);
            player.setLPlayerStartCallback(this);
            binder = new LPlayerBinder(this, player);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null == intent){
            return START_STICKY;
        }

        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return START_STICKY;
        }

        if(ACTION_CONTROL.equals(action) && null != binder){
            int cmd = intent.getIntExtra(_CMD, 0);
            Log.d(TAG,"---> cmd = " + cmd);
            switch(cmd){
                default : break;

                case CMD_NEXT :
                    binder.next();
                    break;

                case CMD_PAUSE :
                    binder.pause();
                    break;

                case CMD_START :
                    binder.start();
                    break;

                case CMD_PREVIOUS :
                    binder.previous();
                    break;

                case CMD_CLOSE :
                    binder.stop();
                    clearPlayerWidget();
                    break;

                case CMD_LOGOUT:
                    binder.logout();
                    //stopSelf();
                    clearPlayerWidget();
            }
        }
        return START_STICKY;
    }

    private void clearPlayerWidget(){
        if(0 != foregroundNotificationId){
            foregroundNotificationId = 0;
            stopForeground(true);
        }
        if (remoteControlFlagId != 0){
            remoteControlFlagId = 0;
        }
    }

    @Override
    public void onLPlayerReceive(Context context, Intent intent) {
        if(isDestroyed){
            return;
        }

        Log.d(TAG, "@onLPlayerReceive isShowingWidget:" + binder.isShowingWidget());
        if(!binder.isShowingWidget()){
            return;
        }

        String action = intent.getAction();
        if(ACTION_TRACK_CHANGED.equals(action)){
            if(null != notificationCallback){
                foregroundNotificationId = notificationCallback.onStartForeground(this, intent.getExtras(),foregroundNotificationId);
            }
        }

        else if(ACTION_PLAYER_STATUS_CHANGED.equals(action)){
            if(null != notificationCallback){
                foregroundNotificationId = notificationCallback.onStartForeground(this, intent.getExtras(),foregroundNotificationId);
            }
        }

        if (remoteControlCallback != null){
            remoteControlFlagId =  remoteControlCallback.onStart(intent.getExtras(), remoteControlFlagId);
        }
    }

    @Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    public void registerNotificationCallback(NotificationCallback callback){
        notificationCallback = callback;
    }

    public void registerRemoteControlCallback(RemoteControlCallback callback){
        remoteControlCallback = callback;
    }

    public void registerLPlayerStartCallback(LPlayerStartCallback startCallback){
        this.startCallback = startCallback;
    }

    public void setExceptionListener(LPlayerExceptionListener playerExceptionListener){
        binder.setPlayerExceptionListener(playerExceptionListener);
    }

    @Override
    public boolean canPlay() {
        if(null != startCallback){
            return startCallback.canPlay();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        isDestroyed = true;

        unregisterReceiver(headsetPlugReceiver);
        localBroadcastManager.unregisterReceiver(playerReceiver);
        headsetPlugReceiver = null;
        playerReceiver = null;
        localBroadcastManager = null;
        notificationCallback = null;
        remoteControlCallback = null;
        headsetPlugReceiver = null;
        startCallback = null;
        binder = null;
    }

    public static final class LPlayerBinder extends Binder implements ILPlayerClient<Track>, ITracksClient<Track>{
        private volatile boolean isShowingWidget;
        private LPlayer player;
        private LPlayerService service;

        /*package*/ LPlayerBinder(LPlayerService service, LPlayer player){
            this.player = player;
            this.service = service;
            this.isShowingWidget = true;
        }

        @Override
        public int switch2NextMode() {
            return player.switch2NextMode();
        }

        @Override
        public void switchMode(int mode) {
            player.switchMode(mode);
        }

        @Override
        public int getMode() {
            return player.getMode();
        }

        @Override
        public int getNextMode() {
            return getNextMode();
        }

        @Override
        public void start() {
            Log.d(TAG, "__@_____  start ___");
            isShowingWidget = true;
            player.start();
        }

        @Override
        public void start(final int trackIndex) {
            Log.d(TAG, "__@_____  start with track index ___");
            isShowingWidget = true;
            player.start(trackIndex);
        }

        @Override
        public void pause() {
            Log.d(TAG, "__@_____  pause ___");
            player.pause();
        }

        @Override
        public void next() {
            Log.d(TAG, "__@_____  next ___");
            player.next();
        }

        @Override
        public void previous() {
            Log.d(TAG, "__@_____  previous ___");
            player.previous();
        }

        /**
         * Seeks to specified time position.
         *
         * @param seconds the offset in seconds from the start to seek to.
         */
        @Override
        public void seekTo(final int seconds) {
            player.seekTo(seconds);
        }

        @Override
        public Track[] obtainTracks() {
            return player.obtainTracks();
        }

        @Override
        public int getClientStatus() {
            return player.getClientStatus();
        }

        @Override
        public int[] getDurationAndPosition() {
            return player.getDurationAndPosition();
        }

        @Override
        public int getFrom() {
            return player.getFrom();
        }

        @Override
        public long getSecondId() {
            return player.getSecondId();
        }

        @Override
        public Track getTrack() {
            return player.getTrack();
        }

        @Override
        public int getTrackIndex() {
            return player.getTrackIndex();
        }

        @Override
        public int getTracksSize() {
            return player.getTracksSize();
        }

        @Override
        public boolean addTracks(Track[] tracks, int position, int from, long secondId) {
            return player.addTracks(tracks, position, from, secondId);
        }

        @Override
        public boolean addTrack(Track track, int from, long secondId) {
            return player.addTrack(track, from, secondId);
        }

        @Override
        public void addTracksAndPlay(Track[] tracks, int position, int from, long secondId) {
            Log.d(TAG, "__@_____  addTracksAndPlay ___");
            isShowingWidget = true;
            player.addTracksAndPlay(tracks, position, from, secondId);
        }

        @Override
        public void addTrackAndPlay(Track track, int from, long secondId) {
            Log.d(TAG, "__@_____  addTrackAndPlay ___");
            isShowingWidget = true;
            player.addTrackAndPlay(track, from, secondId);
        }

        public LPlayerService getService(){
            return service;
        }

        /*package*/ void setPlayerExceptionListener(LPlayerExceptionListener playerExceptionListener){
            player.setExceptionListener(playerExceptionListener);
        }

        /*package*/ void stop(){
            isShowingWidget = false;
            player.stop();
        }

        /*package*/ void logout(){
            isShowingWidget = false;
            player.logout();
        }

        /*package*/ boolean isShowingWidget(){
            return isShowingWidget;
        }
    }

    private final class HeadsetPlugBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("state")) {
				int state = intent.getIntExtra("state", 0);
				Log.d(TAG, "@HeadsetPlugBroadcastReceiver.onReceive  state:" + state);
				if (!isDestroyed && 0 == state) {
                    binder.pause();
				}
			}
		}
	}
}
