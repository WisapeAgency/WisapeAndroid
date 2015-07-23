package org.cubieline.lplayer.media;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import org.cubieline.lplayer.LPlayerExceptionListener;
import org.cubieline.lplayer.app.LPlayerService;
import org.cubieline.lplayer.common.PlayerModeConfigHelper;
import org.cubieline.lplayer.common.PluginInstanceHelper;

import java.util.Random;

/**
 * Created by LeiGuoting on 14/11/10.
 */
public class LPlayer extends AbsLPlayer implements ILPlayerPlugin.OnPluginListener, IClientNotificationManager, ITracksManager<Track>,
        LPlayPositionTracker.PositionTrackerCallback, LPlayPositionTracker.OnPositionTrackerListener, ILPlayerStatusManager, IAudioFocusManager, ILPlayerPluginManager.OnPluginExceptionListener{

    private static final String TAG = "LPlayer";
    private static final int WHAT_START = 0x01;
    private static final int WHAT_START_WITH_INDEX = 0x02;
    private static final int WHAT_PAUSE = 0x03;
    private static final int WHAT_NEXT = 0x04;
    private static final int WHAT_PREVIOUS = 0x05;
    private static final int WHAT_SEEK_TO = 0x06;
    private static final int WHAT_STOP = 0x07;
    private static final int WHAT_PLUGIN_PREPARE_ERROR = 0x08;
    private static final int WHAT_PLUGIN_PREPARED = 0x09;
    private static final int WHAT_PLUGIN_COMPLETED = 0x10;
    private static final int WHAT_PLUGIN_BUFFERING_START = 0x11;
    private static final int WHAT_PLUGIN_BUFFERING_COMPLETED = 0x12;
    private static final int WHAT_PLUGIN_SEEK_COMPLETED = 0x13;
    private static final int WHAT_PLUGIN_ERROR = 0x14;
    private static final int WHAT_PLUGIN_EXCEPTION_PREPARE = 0x15;
    private static final int WHAT_PLUGIN_EXCEPTION_START = 0x16;
    private static final int WHAT_PLUGIN_EXCEPTION_PAUSE = 0x17;
    private static final int WHAT_PLUGIN_EXCEPTION_STOP = 0x18;
    private static final int WHAT_PLUGIN_EXCEPTION_RESUME = 0x19;
    private static final int WHAT_PLUGIN_EXCEPTION_SEEK_TO = 0x20;

    private volatile int clientStatus = CLIENT_STATUS_PASUED;
    private volatile int status = STATUS_IDLE;
    private volatile int duration; // units : second
    private volatile int position; // units : second
    private volatile int continuousError;
    private volatile long bufferingId;
    private volatile int audioFocusStatus;
    private volatile boolean hasVolumeChanged;
    private volatile boolean isPauseFromAudioFocus;
    private volatile boolean isRunning;

    private final Random random;
    private final AudioManager audioManager;
    private final HandlerThread playerThread;
    private final ILPlayerPluginManager pluginManager;
    private final LPlayPositionTracker positionTracker;
    private final IPersistenceManager persistenceManager;
    private final LocalBroadcastManager broadcastManager;

    private LPlayerStartCallback startCallback;
    private LPlayerExceptionListener playerExceptionListener;

    public LPlayer(Context context, HandlerThread thread) throws PackageManager.NameNotFoundException {
        super(thread.getLooper());
        playerThread = thread;

        random = new Random();
        broadcastManager = LocalBroadcastManager.getInstance(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        positionTracker = new LPlayPositionTracker(this, this);

        ComponentName playerServiceComponent = new ComponentName(context, LPlayerService.class);
        final Bundle metaData = context.getPackageManager().getServiceInfo(playerServiceComponent, PackageManager.GET_META_DATA).metaData;
        String[] plugins = null;
        Resources resources = context.getResources();
        if (metaData.containsKey(META_DATA_PLUGIN_CUSTOMS)) {
            plugins = resources.getStringArray(metaData.getInt(META_DATA_PLUGIN_CUSTOMS));
        }

        boolean hasDefault = true;
        if (metaData.containsKey(META_DATA_PLUGIN_DEFAULT)) {
            hasDefault = metaData.getBoolean(META_DATA_PLUGIN_DEFAULT);
        }

        /*Instance plugin*/
        SparseArray<ILPlayerPlugin> pluginMapping = PluginInstanceHelper.loadPlugins(plugins, hasDefault, context, this);
        HandlerThread pluginThread = new HandlerThread("L-player-plugin", Thread.MIN_PRIORITY);
        pluginThread.start();
        pluginManager = new LPlayerPluginManagerImpl(pluginThread, pluginMapping, this);

        persistenceManager = new PersistenceManagerImpl(context);
        Track[] tracks = persistenceManager.restoreTracksState();
        int length = (null == tracks ? 0 : tracks.length);
        if(0 < length){
            this.tracks = tracks;
        }

        int [] modeArray = persistenceManager.restoreModeArray();
        int modeArrayLength = (null == modeArray ? 0 : modeArray.length);
        if(0 < modeArrayLength){
            this.modeArray = modeArray;
            this.modeIndex = persistenceManager.restoreModeIndex();
        }else{
            /*config mode*/
            String[] modeStr = null;
            if (metaData.containsKey(META_DATA_MODE)) {
                modeStr = resources.getStringArray(metaData.getInt(META_DATA_MODE));
            }
            this.modeArray = PlayerModeConfigHelper.configMode(modeStr);
            persistenceManager.saveModeArray(this.modeArray);
            persistenceManager.saveModeIndex(this.modeIndex);
        }

        tracksFrom = persistenceManager.restoreTrackFrom();
        tracksSecondId = persistenceManager.restoreSecondId();
        tracksIndex = persistenceManager.restoreTrackIndex();
        tracksSize = persistenceManager.restoreTrackSize();
    }

    @Override
    public void dispatchMessage(Message msg) {
        isRunning = true;
        switch(msg.what){
            default : break;
            case WHAT_START :
                doStart();
                break;

            case WHAT_START_WITH_INDEX :
                int trackIndex = msg.arg1;
                doStart(trackIndex);
                break;

            case WHAT_PAUSE :
                doPause();
                break;

            case WHAT_NEXT :
                doNext();
                break;

            case WHAT_PREVIOUS :
                doPrevious();
                break;

            case WHAT_STOP :
                doStop();
                break;

            case WHAT_SEEK_TO :
                int seconds = msg.arg1;
                doSeekTo(seconds);
                break;

            case WHAT_PLUGIN_PREPARED :
                ILPlayerPlugin plugin = (ILPlayerPlugin) msg.obj;
                doPluginPrepared(plugin);
                break;

            case WHAT_PLUGIN_PREPARE_ERROR :
                Throwable throwable = (Throwable) msg.obj;
                doPluginPrepareError(throwable);
                break;

            case WHAT_PLUGIN_COMPLETED :
                plugin = (ILPlayerPlugin) msg.obj;
                doPluginCompleted(plugin);
                break;

            case WHAT_PLUGIN_BUFFERING_START :
                doPluginBufferingStart();
                break;

            case WHAT_PLUGIN_BUFFERING_COMPLETED :
                doPluginBufferingCompleted();
                break;

            case WHAT_PLUGIN_SEEK_COMPLETED :
                plugin = (ILPlayerPlugin) msg.obj;
                doPluginSeekCompleted(plugin);
                break;

            case WHAT_PLUGIN_ERROR :
                int errorCode = msg.arg1;
                doPluginError(errorCode);
                break;

            case WHAT_PLUGIN_EXCEPTION_PREPARE :
                int pluginCode = msg.arg1;
                PluginExceptionParams params = (PluginExceptionParams) msg.obj;
                doPluginPrepareException(pluginCode, params.plugin, params.throwable);
                break;

            case WHAT_PLUGIN_EXCEPTION_START :
                pluginCode = msg.arg1;
                params = (PluginExceptionParams) msg.obj;
                doPluginStartException(pluginCode, params.plugin, params.throwable);
                break;

            case WHAT_PLUGIN_EXCEPTION_PAUSE :
                pluginCode = msg.arg1;
                params = (PluginExceptionParams) msg.obj;
                doPluginPauseException(pluginCode, params.plugin, params.throwable);
                break;

            case WHAT_PLUGIN_EXCEPTION_STOP :
                pluginCode = msg.arg1;
                params = (PluginExceptionParams) msg.obj;
                doPluginStopException(pluginCode, params.plugin, params.throwable);
                break;

            case WHAT_PLUGIN_EXCEPTION_RESUME :
                pluginCode = msg.arg1;
                params = (PluginExceptionParams) msg.obj;
                doPluginResumeException(pluginCode, params.plugin, params.throwable);
                break;

            case WHAT_PLUGIN_EXCEPTION_SEEK_TO :
                pluginCode = msg.arg1;
                params = (PluginExceptionParams) msg.obj;
                doPluginSeekToException(pluginCode, params.plugin, params.throwable);
                break;
        }
        isRunning = false;
    }

    public void setLPlayerStartCallback(LPlayerStartCallback startCallback){
        this.startCallback = startCallback;
    }

    public void setExceptionListener(LPlayerExceptionListener exceptionListener){
        this.playerExceptionListener = exceptionListener;
    }

    @Override
    public int switch2NextMode() {
        int mode = super.switch2NextMode();
        persistenceManager.saveModeIndex(this.modeIndex);
        return mode;
    }

    @Override
    public void switchMode(int mode) {
        super.switchMode(mode);
        persistenceManager.saveModeIndex(this.modeIndex);
    }

    @Override
    public void notifyStatusChanged(int clientStatus) {
        this.clientStatus = clientStatus;
        Bundle args = new Bundle();
        args.putInt(_DURATION, duration);
        args.putInt(_POSITION, position);
        sendBroadcast(CMD_CLIENT_STATUS_CHANGED, args, ACTION_PLAYER_STATUS_CHANGED);
    }

    @Override
    public void notifyTrackListChanged(int source) {
        //TODO
    }

    @Override
    public void notifyTrackChanged(int messageSource) {
        Bundle args = new Bundle();
        args.putInt(_MESSAGE_SOURCE, 1);
        args.putInt(_TRACKS_SIZE, getTracksSize());
        sendBroadcast(CMD_TRACK_CHANGED, args, ACTION_TRACK_CHANGED);
    }

    @Override
    public void notifyTrackPositionChanged(int duration, int position) {
        Bundle args = new Bundle();
        args.putInt(_POSITION, position);
        args.putInt(_DURATION, duration);
        sendBroadcast(CMD_UPDATE_POSITION, args, ACTION_TRACK_POSITION_CHANGED);
    }

    private void sendBroadcast(int cmd, Bundle extras, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(_CMD, cmd);
        intent.putExtra(_TRACK, getTrack());
        intent.putExtra(_MODE, getMode());
        intent.putExtra(_TRACK_INDEX, getTrackIndex());
        intent.putExtra(_CLIENT_STATUS, getClientStatus());
        intent.putExtra(_FROM, getFrom());
        intent.putExtra(_SECOND_ID, getSecondId());

        if (null != extras) {
            intent.putExtras(extras);
        }
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public int getClientStatus() {
        return clientStatus;
    }

    @Override
    public int[] getDurationAndPosition() {
        return new int[]{duration, position};
    }

    /**
     * Implementing from IPlayerPlugin.OnPlayerPluginListener
     */
    @Override
    public void onPluginPrepared(ILPlayerPlugin plugin) {
        obtainMessage(WHAT_PLUGIN_PREPARED, plugin).sendToTarget();
    }

    private void doPluginPrepared(ILPlayerPlugin plugin){
        final int copyStatus = obtainStatus();
        if (STATUS_PREPARE == copyStatus) {
            float copyPosition = plugin.getCurrentPosition();
            if (0 < copyPosition) {
                position = Math.round(copyPosition / 1000f);
            }

            duration = positionTracker.start();
            updateStatus(STATUS_PLAYING);
            notifyStatusChanged(CLIENT_STATUS_PLAYING);

            pluginManager.onStart();
        } else if (STATUS_STOPED == copyStatus) {
            plugin.onStop();
        } else if (STATUS_PAUSED == copyStatus) {
            plugin.onPause();
        }
    }

    /**
     * Implementing from IPlayerPlugin.OnPlayerPluginListener
     */
    @Override
    public void onPluginCompleted(ILPlayerPlugin plugin) {
        obtainMessage(WHAT_PLUGIN_COMPLETED, plugin).sendToTarget();
    }

    private void doPluginCompleted(ILPlayerPlugin plugin) {
        continuousError = 0;
        int copyStatus = obtainStatus();
        if (positionTracker.isTracking()) {
            positionTracker.stop();
        }

        duration = 0;
        if (!plugin.isLooping()) {
            //non-single-loop
            if (STATUS_PAUSED != copyStatus && STATUS_STOPED != copyStatus) {
                updateStatus(STATUS_COMPLETED);
                updateClientStatus(CLIENT_STATUS_PASUED);
                innerNext(false);
            }
        }
    }

    /**
     * Implementing from IPlayerPlugin.OnPlayerPluginListener
     */
    @Override
    public void onPluginPrepareError(Throwable e) {
        obtainMessage(WHAT_PLUGIN_PREPARE_ERROR, e).sendToTarget();
    }

    private void doPluginPrepareError(Throwable e) {
        Log.e(TAG, "", e);
        innerNext(false);
    }

    @Override
    public void onPluginSeekCompleted(ILPlayerPlugin plugin) {
        obtainMessage(WHAT_PLUGIN_SEEK_COMPLETED, plugin).sendToTarget();
    }

    private void doPluginSeekCompleted(ILPlayerPlugin plugin){
        int copyStatus = obtainStatus();
        if (STATUS_PLAYING == copyStatus) {
            positionTracker.start();
            updateClientStatus(CLIENT_STATUS_PLAYING);
        }
    }

    @Override
    public void onPluginBufferingStart() {
        obtainMessage(WHAT_PLUGIN_BUFFERING_START).sendToTarget();
    }

    private void doPluginBufferingStart() {
        bufferingId = getTrack().getTrackId();
        updateStatus(STATUS_BUFFERING);
        updateClientStatus(CLIENT_STATUS_BUFFERING);
    }

    @Override
    public void onPluginBufferingCompleted() {
        obtainMessage(WHAT_PLUGIN_BUFFERING_COMPLETED).sendToTarget();
    }

    private void doPluginBufferingCompleted() {
        long currentId = getTrack().getTrackId();
        if (bufferingId == currentId) {
            int copyStatus = obtainStatus();
            if (STATUS_PAUSED == copyStatus || STATUS_STOPED == copyStatus) {
                pluginManager.onPause();
                updateClientStatus(CLIENT_STATUS_PASUED);
            } else {
                updateStatus(STATUS_PLAYING);
                updateClientStatus(CLIENT_STATUS_PLAYING);
            }
        }
        bufferingId = 0;
    }

    @Override
    public void onPluginError(final int errorCode) {
        obtainMessage(WHAT_PLUGIN_ERROR, errorCode, 0).sendToTarget();
    }

    private void doPluginError(final int errorCode) {
        Log.e(TAG, "@onPluginError _____________  errorCode[" + errorCode + "] __________");
        switch (errorCode) {
            default:
                stop();
                break;

            case ILPlayerPlugin.ERROR_CODE_FILE_NOT_FOUND:
                continuousError++;
                stop();
                innerNext(false);
                break;
        }
    }

    @Override
    public void start() {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_START).sendToTarget();
    }

    private void doStart(){
        Log.d(TAG, "_____________ doStart ___");
        final int copyStatus = obtainStatus();
        if (STATUS_PLAYING == copyStatus) {
            return;
        }

        if (STATUS_PAUSED == copyStatus) {
            innerResume();
        } else {
            continuousError = 0;
            innerStart(getTrackIndex());
        }
    }

    @Override
    public void start(int trackIndex) {
        if (0 == tracksSize) {
            return;
        }

        if (0 > trackIndex || trackIndex >= tracksSize) {
            throw new IllegalArgumentException("The trackIndex[" + trackIndex + "] must is equal or greater than 0 and less than trackSize[" + tracksSize + "]");
        }

        obtainMessage(WHAT_START_WITH_INDEX, trackIndex, 0).sendToTarget();
    }

    private void doStart(int trackIndex){
        Log.d(TAG, "_____________ start with track index ___");
        final int copyStatus = obtainStatus();
        final boolean isSameTrack = trackIndex == getTrackIndex();
        if (STATUS_PLAYING == copyStatus) {
            pause();
        }

        if (STATUS_PAUSED == copyStatus && isSameTrack) {
            innerResume();
            return;
        }
        continuousError = 0;
        innerStart(trackIndex);
    }

    @Override
    public void pause() {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_PAUSE).sendToTarget();
    }

    private void doPause(){
        Log.d(TAG, "_____________ doPause ___");
        int copyStatus = obtainStatus();
        if (STATUS_PLAYING == copyStatus || STATUS_BUFFERING == copyStatus) {
            pluginManager.onPause();
            positionTracker.stop();
        }
        updateStatus(STATUS_PAUSED);
        updateClientStatus(CLIENT_STATUS_PASUED);
    }

    @Override
    public void next() {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_NEXT).sendToTarget();
    }

    private void doNext(){
        Log.d(TAG, "_____________ next ___");
        innerStop(obtainStatus());
        innerNext(true);
    }

    @Override
    public void previous() {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_PREVIOUS).sendToTarget();
    }

    private void doPrevious(){
        Log.d(TAG, "_____________ previous ___");
        innerStop(obtainStatus());
        innerStart(getTracksPreviousIndex());
    }

    @Override
    public void seekTo(int seconds) {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_SEEK_TO, seconds, 0).sendToTarget();
    }

    private void doSeekTo(int seconds){
        Log.d(TAG, "_____________ seekTo ___");
        int copyStatus = obtainStatus();
        if (STATUS_PLAYING == copyStatus) {
            updateClientStatus(CLIENT_STATUS_BUFFERING);
            position = seconds;
            positionTracker.stop();
            pluginManager.onSeekTo(seconds * 1000);
        } else if (STATUS_PAUSED == copyStatus) {
            position = seconds;
            pluginManager.onSeekTo(seconds * 1000);
        }
    }

    @Override
    public void stop() {
        if (0 == tracksSize) {
            return;
        }

        obtainMessage(WHAT_STOP).sendToTarget();
    }

    private void doStop(){
        Log.d(TAG, "_____________ stop ___");
        int copyStatus = obtainStatus();
        updateStatus(STATUS_STOPED);
        updateClientStatus(CLIENT_STATUS_PASUED);
        if (STATUS_PLAYING == copyStatus || STATUS_PAUSED == copyStatus || STATUS_BUFFERING == copyStatus) {
            releaseAudioFocus();
            positionTracker.stop();
            pluginManager.onStop();
        }
    }

    @Override
    @TargetApi(18)
    public void release() {
        tracks = null;
        tracksIndex = 0;
        tracksSize = 0;
        tracksFrom = 0;
        tracksSecondId = 0;

        positionTracker.onDestroy();
        if(18 <= Build.VERSION.SDK_INT){
            playerThread.quitSafely();
        }else{
            playerThread.quit();
        }
    }

    private void innerStart(int trackIndex) {
        if(null != startCallback && !startCallback.canPlay()){
            updateClientStatus(CLIENT_STATUS_PASUED);
            return;
        }

        int copyStatus = obtainStatus();
        Log.d(TAG, "@innerStart ___ position:" + trackIndex + ", status:" + copyStatus);
        if (isPauseFromAudioFocus) {
            isPauseFromAudioFocus = false;
        }

        Track track = changeTrack(trackIndex);
        updateStatus(STATUS_PREPARE);
        updateClientStatus(CLIENT_STATUS_BUFFERING);

        tryToGetAudioFocus();
        persistenceManager.saveTrackIndex(trackIndex);
        pluginManager.onPrepare(track, getMode());
    }

    private void innerPause() {
        int copyStatus = obtainStatus();
        if (STATUS_PLAYING != copyStatus) {
            return;
        }

        pluginManager.onPause();
        positionTracker.stop();
        updateStatus(STATUS_PAUSED);
    }

    private void innerResume() {
        if(null != startCallback && !startCallback.canPlay()){
            pause();
            return;
        }

        Log.d(TAG, "@innerResume ____");
        if (isPauseFromAudioFocus) {
            isPauseFromAudioFocus = false;
        }

        if (hasVolumeChanged) {
            pluginManager.setVolume(1.0f, 1.0f);
            hasVolumeChanged = false;
        }
        updateClientStatus(CLIENT_STATUS_PLAYING);
        updateStatus(STATUS_PLAYING);
        positionTracker.start();
        pluginManager.onResume();
    }

    private void innerStop(int status) {
        if (STATUS_PLAYING == status || STATUS_PAUSED == status || STATUS_BUFFERING == status || STATUS_PREPARE == status) {
            pluginManager.onStop();
            positionTracker.stop();
        }
        updateClientStatus(CLIENT_STATUS_PASUED);
    }

    private void innerNext(boolean fromUser) {
        int nextIndex = 0;
        boolean hasNext = true;
        int index = getTrackIndex();
        switch (getMode()) {
            case MODE_PLAY_SINGLE_LOOP:
                if(!fromUser){
                    return;
                }

                nextIndex = getTrackIndex();
                break;

            case MODE_PLAY_LOOP:
                if(!fromUser && index == (tracksSize - 1) && 0 < continuousError){
                    return;
                }

                nextIndex = getTracksNextIndex();
                break;

            case MODE_PLAY_RANDOM:
                if(!fromUser && continuousError == (tracksSize - 1)){
                    return;
                }

                if (1 == tracksSize) {
                    nextIndex = 0;
                } else {
                    nextIndex = random.nextInt(tracksSize);
                }
                break;

            case MODE_PLAY_ORDER:
                if(!fromUser && index == (tracksSize - 1)){
                    return;
                }

                Log.d(TAG, "@innerNext -->> MODE_PLAY_ORDER tracksIndex:" + tracksIndex + ", tracksSize:" + tracksSize + ", fromUser:" + fromUser);
                if (tracksIndex + 1 == tracksSize && !fromUser) {
                    hasNext = false;
                } else {
                    nextIndex = getTracksNextIndex();
                }
                break;
        }

        if (hasNext) {
            innerStart(nextIndex);
        }
    }

    /**
     * @return units: millisecond
     */
    @Override
    public int getCurrentPosition() {
        return pluginManager.getCurrentPosition();
    }

    /**
     * @return units: millisecond
     */
    @Override
    public int getDuration() {
        return pluginManager.getDuration();
    }

    @Override
    public boolean isLooping() {
        return pluginManager.isLooping();
    }

    @Override
    protected void setLooping(int mode) {
        if (MODE_PLAY_SINGLE_LOOP == mode) {
            pluginManager.setLooping(true);
        } else if (pluginManager.isLooping()) {
            pluginManager.setLooping(false);
        }
    }

    @Override
    public void onPositionChanged(int duration, int position) {
        //Player position changed
        this.position = position;
        notifyTrackPositionChanged(duration, position);
    }

    @Override
    public void onSingleLoopingStart(int position) {
        //Player single looping start
        this.position = position;
        notifyStatusChanged(CLIENT_STATUS_PLAYING);
    }

    @Override
    public void updateStatus(int status) {
        this.status = status;
        Log.d(TAG, "@updateStatus ___ status:" + this.status);
    }

    @Override
    public void updateClientStatus(int clientStatus) {
        this.clientStatus = clientStatus;
        notifyStatusChanged(clientStatus);
    }

    @Override
    public int obtainStatus() {
        Log.d(TAG, "@obtainStatus ___ status:" + status);
        return status;
    }

    /**
     * Called on the listener to notify it the audio focus for this listener has been changed.
     * The focusChange value indicates whether the focus was gained,
     * whether the focus was lost, and whether that loss is transient, or whether the new focus
     * holder will hold it for an unknown amount of time.
     * When losing focus, listeners can use the focus change information to decide what
     * behavior to adopt when losing focus. A music player could for instance elect to lower
     * the volume of its music stream (duck) for transient focus losses, and pause otherwise.
     *
     * @param focusChange the type of focus change, one of {@link android.media.AudioManager#AUDIOFOCUS_GAIN},
     *                    {@link android.media.AudioManager#AUDIOFOCUS_LOSS}, {@link android.media.AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}
     *                    and {@link android.media.AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}.
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d(TAG, "@onAudioFocusChange focusChange:" + focusChange);
        switch (focusChange) {
            default:
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                audioFocusStatus = AUDIO_FOCUS_FOCUSED;
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                audioFocusStatus = AUDIO_FOCUS_NO_FOCUS_NO_DUCK;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                audioFocusStatus = AUDIO_FOCUS_NO_FOCUS_CAN_DUCK;
                break;
        }

        if (AUDIO_FOCUS_NO_FOCUS_NO_DUCK == audioFocusStatus) {
            pauseFromAudioFocus();
        } else if (AUDIO_FOCUS_NO_FOCUS_CAN_DUCK == audioFocusStatus) {
            hasVolumeChanged = true;
            pluginManager.setVolume(0.1f, 0.1f);
        } else /*if (FOCUSED == audioFocusStatus)*/ {
            if (hasVolumeChanged) {
                hasVolumeChanged = false;
                pluginManager.setVolume(1.0f, 1.0f);
            }
            startFromAudioFocus();
        }
    }

    @Override
    public void pauseFromAudioFocus() {
        final int copyStatus = obtainStatus();
        if (STATUS_PLAYING == copyStatus || STATUS_BUFFERING == copyStatus || STATUS_PREPARE == copyStatus) {
            isPauseFromAudioFocus = true;
            innerPause();
        }
    }

    @Override
    public void startFromAudioFocus() {
        if (isPauseFromAudioFocus) {
            start();
        }
    }

    @Override
    public void tryToGetAudioFocus() {
        if (AUDIO_FOCUS_FOCUSED == audioFocusStatus) {
            return;
        }

        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {
            audioFocusStatus = AUDIO_FOCUS_FOCUSED;
        }
    }

    @Override
    public void releaseAudioFocus() {
        if (AUDIO_FOCUS_FOCUSED != audioFocusStatus) {
            return;
        }

        int result = audioManager.abandonAudioFocus(this);
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result) {
            audioFocusStatus = AUDIO_FOCUS_NO_FOCUS_NO_DUCK;
        }
    }

    @Override
    public Track changeTrack(int trackIndex) {
        if (0 > trackIndex || trackIndex > tracksSize) {
            throw new IllegalArgumentException("The trackIndex should be (0 < trackIndex[" + trackIndex + "] < tracksSize[" + tracksSize + "])");
        }

        tracksIndex = trackIndex;
        position = 0;
        duration = 0;
        notifyTrackChanged(0);
        return tracks[trackIndex];
    }

    @Override
    public boolean addTrack(Track track, int from, long secondId) {
        boolean added = super.addTrack(track, from, secondId);
        persistenceManager.saveTracksState(tracks, tracksFrom, tracksSecondId, tracksIndex, tracksSize);
        return added;
    }

    @Override
    public boolean addTracks(Track[] tracks, int position, int from, long secondId) {
        boolean added = super.addTracks(tracks, position, from, secondId);
        persistenceManager.saveTracksState(tracks, tracksFrom, tracksSecondId, tracksIndex, tracksSize);
        return added;
    }

    /**
     * Implementing from ITracksClient
     */
    @Override
    public void addTrackAndPlay(Track track, int from, long secondId) {
        if (!addTrack(track, from, secondId)) {
            return;
        }

        Log.d(TAG, "_____________ addTrackAndPlay ___");
        int copyStatus = obtainStatus();
        updateStatus(STATUS_IDLE);
        innerStop(copyStatus);
        start();
    }

    /**
     * Implementing from ITracksClient
     */
    @Override
    public void addTracksAndPlay(Track[] tracks, int position, int from, long secondId) {
        if (!addTracks(tracks, position, from, secondId)) {
            return;
        }

        Log.d(TAG, "_____________ addTracksAndPlay ___");
        int copyStatus = obtainStatus();
        updateStatus(STATUS_IDLE);
        innerStop(copyStatus);
        start();
    }

    @Override
    public void logout() {
        stop();
        persistenceManager.clear();
        release();
    }

    @Override
    public void onPluginPrepareException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_PREPARE, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    @Override
    public void onPluginStartException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_START, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    @Override
    public void onPluginPauseException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_PAUSE, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    @Override
    public void onPluginStopException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_STOP, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    @Override
    public void onPluginResumeException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_RESUME, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    @Override
    public void onPluginSeekToException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        obtainMessage(WHAT_PLUGIN_EXCEPTION_SEEK_TO, pluginCode, 0, new PluginExceptionParams(plugin, e)).sendToTarget();
    }

    private void doPluginPrepareException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        Log.e(TAG, "doPluginPrepareException", e);
        if(null != playerExceptionListener){
                boolean hasNext = playerExceptionListener.onPrepareException(pluginCode, e);
                if(hasNext){
                    innerNext(false);
                    return;
                }
        }

        releaseAudioFocus();
        updateStatus(STATUS_STOPED);
        updateClientStatus(CLIENT_STATUS_PASUED);
    }

    private void doPluginStartException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        if (null != playerExceptionListener) {
            boolean isPlayNext = playerExceptionListener.onStartException(pluginCode, e);
            if (isPlayNext) {
                innerNext(false);
                return;
            }
        }

        releaseAudioFocus();
        innerStop(obtainStatus());
    }

    private void doPluginPauseException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        //do nothing
    }

    private void doPluginStopException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        //do nothing
    }

    private void doPluginResumeException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        //do nothing
    }

    private void doPluginSeekToException(int pluginCode, ILPlayerPlugin plugin, Throwable e) {
        //do nothing
    }

    private static final class PluginExceptionParams{
        ILPlayerPlugin plugin;
        Throwable throwable;
        PluginExceptionParams(ILPlayerPlugin plugin, Throwable throwable){
            this.plugin = plugin;
            this.throwable = throwable;
        }
    }
}