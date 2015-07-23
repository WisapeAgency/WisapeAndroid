package org.cubieline.lplayer.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by LeiGuoting on 14/11/10.
 */
public abstract class MediaPlayerPlugin implements ILPlayerPlugin<Track>, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener{
    private static final String TAG = MediaPlayerPlugin.class.getSimpleName();

    protected Track track;
    protected Track cancelPrepareTrack;
    protected Context context;

    protected OnPluginListener onPlayerPluginListener;

    public MediaPlayerPlugin(Context context, OnPluginListener onPlayerPluginListener){
        if(null == onPlayerPluginListener){
            throw new IllegalArgumentException("The OnLPlayerPluginListener can not is null.");
        }

        this.context = context;
        this.onPlayerPluginListener = onPlayerPluginListener;
    }

    protected abstract MediaPlayer getMediaPlayer();

    protected abstract void prepareForStart(int mode);

    protected void setMediaPlayerListener(MediaPlayer mediaPlayer){
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        //mediaPlayer.setOnBufferingUpdateListener(this);
    }

    protected void setMediaPlayerSetting(MediaPlayer mediaPlayer){
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    @Override
    public void onPrepare(Track track, int mode) {
        this.track = track;
        prepareForStart(mode);
    }

    @Override
    public void onStart() {
        getMediaPlayer().start();
    }

    @Override
    public void onPause() {
        getMediaPlayer().pause();
    }

    @Override
    public void onStop() {
        getMediaPlayer().stop();
    }

    @Override
    public void onResume() {
        getMediaPlayer().start();
    }

    @Override
    public void onRelease() {
        final MediaPlayer player = getMediaPlayer();
        if(null != player){
            if(player.isPlaying()){
                player.stop();
            }
            player.setOnCompletionListener(null);
            player.setOnPreparedListener(null);
            player.setOnBufferingUpdateListener(null);
            player.setOnErrorListener(null);
            player.setOnInfoListener(null);
            player.setOnSeekCompleteListener(null);
            player.setOnVideoSizeChangedListener(null);
            player.release();
        }
        context = null;
        onPlayerPluginListener = null;
        track = null;
    }

    @Override
    public void onSeekTo(int milliseconds) {
        getMediaPlayer().seekTo(milliseconds);
    }



    /**
     * Implementing from android.media.MediaPlayer.OnPreparedListener
     *
     * Called when the media file is ready for playback.
     *
     * @param mediaPlayer the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        final Track cancelTrack = cancelPrepareTrack;
        if(null != cancelPrepareTrack){
            cancelPrepareTrack = null;
        }

        if(null == cancelTrack || cancelTrack.getTrackId() != track.getTrackId()){
            onPlayerPluginListener.onPluginPrepared(this);
        }
    }

    /**
     * Implementing from android.media.MediaPlayer.OnCompletionListener
     *
     * Called when the end of a media source is reached during playback.
     *
     * @param mediaPlayer the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        onPlayerPluginListener.onPluginCompleted(this);
    }

    /**
     * Implementing from android.media.MediaPlayer.OnBufferingUpdateListener
     *
     * Called to update status in buffering a media stream received through
     * progressive HTTP download. The received buffering percentage
     * indicates how much of the content has been buffered or played.
     * For example a buffering update of 80 percent when half the content
     * has already been played indicates that the next 30 percent of the
     * content to play has been buffered.
     *
     * @param mp      the MediaPlayer the update pertains to
     * @param percent the percentage (0-100) of the content
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        //Do nothing
    }

    /**
     * Implementing from android.media.MediaPlayer.OnSeekCompleteListener
     *
     * Called to indicate the completion of a seek operation.
     *
     * @param mp the MediaPlayer that issued the seek operation
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        onPlayerPluginListener.onPluginSeekCompleted(this);
    }

    /**
     * Implementing from android.media.MediaPlayer.OnErrorListener
     *
     * Called to indicate an error.
     * @return True if the method handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "@onPluginError what[" + what + "], extra[" + extra + "]");

        switch (what) {
            default:
                onPlayerPluginListener.onPluginError(ERROR_CODE_UNKNOWN);
                break;

            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                switch(extra){
                    default : break;

                    case -2147483648 : //file not found
                        onPlayerPluginListener.onPluginError(ERROR_CODE_FILE_NOT_FOUND);
                        break;
                }

                break;
        }
        return true;
    }

    /**
     * Implementing from android.media.MediaPlayer.OnInfoListener
     *
     * Called to indicate an info or a warning.
     *
     * @param mp    the MediaPlayer the info pertains to.
     * @param what  the type of info or warning.
     * @param extra an extra code, specific to the info. Typically
     *              implementation dependent.
     * @return True if the method handled the info, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the info to be discarded.
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "@onInfo what:" + what + ", extra:" + extra);

        switch (what) {
            default:
                return false;

            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                // buffering
                onPlayerPluginListener.onPluginBufferingStart();
                return true;

            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                // buffered
                onPlayerPluginListener.onPluginBufferingCompleted();
                return true;
        }
    }

    /**
     * @return units: millisecond
     */
    @Override
    public int getCurrentPosition() {
        return getMediaPlayer().getCurrentPosition();
    }

    /**
     * @return units: millisecond
     */
    @Override
    public int getDuration() {
        return getMediaPlayer().getDuration();
    }

    @Override
    public boolean isLooping() {
        return getMediaPlayer().isLooping();
    }

    @Override
    public void setLooping(boolean isLooping) {
        getMediaPlayer().setLooping(isLooping);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        getMediaPlayer().setVolume(leftVolume, rightVolume);
    }

    @Override
    public void cancelPrepare(Track track) {
        cancelPrepareTrack = track;
    }
}
