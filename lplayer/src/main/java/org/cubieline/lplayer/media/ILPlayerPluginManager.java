package org.cubieline.lplayer.media;

/**
 * Created by tonyley on 15/1/8.
 */
/*package*/ interface ILPlayerPluginManager<Track> extends IPlugin<Track>, IPluginCode, LPlayPositionTracker.PositionTrackerCallback{

    /*package*/ interface OnPluginExceptionListener{
        public void onPluginPrepareException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
        public void onPluginStartException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
        public void onPluginPauseException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
        public void onPluginStopException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
        public void onPluginResumeException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
        public void onPluginSeekToException(int pluginCode, ILPlayerPlugin plugin, Throwable e);
    }
}
