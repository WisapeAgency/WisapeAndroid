package org.cubieline.lplayer.media;

/**
 * Created by LeiGuoting on 14/11/10.
 */
public interface ILPlayerPlugin<Track> extends IPlugin<Track>, IPluginCancelable<Track>, IPluginCode, LPlayPositionTracker.PositionTrackerCallback{
}
