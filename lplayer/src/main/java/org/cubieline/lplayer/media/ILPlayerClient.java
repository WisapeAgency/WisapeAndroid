/**
 * 
 */
package org.cubieline.lplayer.media;

/**
 * @author LeiGuoting
 *
 */
public interface ILPlayerClient<Track> extends ILPlayerLightClient<Track> {
	
	public int CLIENT_STATUS_PLAYING	= 0x01;
	
	public int CLIENT_STATUS_PASUED		= 0x02;
	
	public int CLIENT_STATUS_BUFFERING	= 0x04;
	
	/*package*/ int CLIENT_STATUS_IDLE	= 0x06;
	
	public int switch2NextMode();
	
	public void switchMode(int mode);
	
	public int getMode();
	
	public int getNextMode();
	
	public void start();
	
	public void start(int trackIndex);
	
	public void pause();
	
	public void next();
	
	public void previous();
	
	/**
	 * Seeks to specified time position.
	 * @param seconds the offset in seconds from the start to seek to.
	 */
	public void seekTo(int seconds);
	
	public Track [] obtainTracks();
	
	public int getClientStatus();
	
	public int[] getDurationAndPosition();
}
