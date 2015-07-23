package org.cubieline.lplayer.media;

/**
 * The interface manage status of LPlayer.
 *
 * Created by LeiGuoting on 14/11/12.
 */
/*package*/ interface ILPlayerStatusManager {

    /*package*/ void updateStatus(int status);

    /*package*/ int obtainStatus();

    /*package*/ void updateClientStatus(int clientStatus);
}
