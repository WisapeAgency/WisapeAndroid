package org.cubieline.lplayer;

/**
 * Created by tonyley on 15/1/6.
 */
public interface LPlayerExceptionListener {

    /**
     *
     * @param pluginCode
     * @param e
     * @return true will play next track
     */
    public boolean onPrepareException(int pluginCode, Throwable e);

    /**
     * 
     * @param pluginCode
     * @param e
     * @return true will play next track
     */
    public boolean onStartException(int pluginCode, Throwable e);
}
