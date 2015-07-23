/**
 * 
 */
package org.cubieline.lplayer.media;

/**
 * @author LeiGuoting
 *
 */
public interface ILPlayerLightClient<Track> {

	public int getFrom();
	
	public long getSecondId();
	
	public Track getTrack();
	
	public int getTrackIndex();

    public int getTracksSize();
}
