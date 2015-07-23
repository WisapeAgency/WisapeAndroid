package org.cubieline.lplayer.media;

/**
 * The interface for managing notification notify client.
 *
 * Created by LeiGuoting on 14/11/12.
 */
public interface IClientNotificationManager {

    public String ACTION_PLAYER_STATUS_CHANGED = "org.cubieline.lplayer.action.PLAYER_STATUS_CHANGED";

    public String ACTION_TRACK_POSITION_CHANGED = "org.cubieline.lplayer.action.TRACK_POSITION_CHANGED";

    public String ACTION_TRACK_CHANGED = "org.cubieline.lplayer.action.TRACK_CHANGED";

    public String ACTION_TRACK_LIST_CHANGED = "org.cubieline.lplayer.action.TRACK_LIST_CHANGED";

    /*package*/ void notifyStatusChanged(int clientStatus);

    /*package*/ void notifyTrackChanged(int messageSource);

    /*package*/ void notifyTrackPositionChanged(int duration, int position);

    /*package*/ void notifyTrackListChanged(int source);
}