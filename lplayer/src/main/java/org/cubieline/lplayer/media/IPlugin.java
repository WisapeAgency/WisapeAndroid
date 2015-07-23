package org.cubieline.lplayer.media;

/**
 * Created by tonyley on 15/1/9.
 */
/*package*/ interface IPlugin<Track> {
    public int ERROR_CODE_FILE_NOT_FOUND = 0x10404;

    public int ERROR_CODE_UNKNOWN = 0x10701;

    public int PLUGIN_CODE_DEFAULT = 0x00;

    public void onPrepare(Track track, int mode);

    public void onStart();

    public void onPause();

    public void onStop();

    public void onResume();

    /**
     * Seeks to specified time position.
     * @param milliseconds the offset in milliseconds from the start to seek to.
     */
    public void onSeekTo(int milliseconds);

    public void setLooping(boolean isLooping);

    public void setVolume(float leftVolume, float rightVolume);

    /**
     * Release the plugin, if LPlayer do not use.
     */
    public void onRelease();

    public interface OnPluginListener {

        public void onPluginPrepareError(Throwable e);

        public void onPluginPrepared(ILPlayerPlugin plugin);

        public void onPluginCompleted(ILPlayerPlugin plugin);

        public void onPluginBufferingStart();

        public void onPluginBufferingCompleted();

        public void onPluginSeekCompleted(ILPlayerPlugin plugin);

        public void onPluginError(int errorCode);
    }
}
