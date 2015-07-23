/**
 * 
 */
package org.cubieline.lplayer.media;

/**
 *
 * @author LeiGuoting
 */
public interface ITracksClient<Track> {
	
	public boolean addTracks(Track[] tracks, int position, int from, long secondId);
	
	public boolean addTrack(Track track, int from, long secondId);
	
	public void addTracksAndPlay(Track[] tracks, int position, int from, long secondId);
	
	public void addTrackAndPlay(Track track, int from, long secondId);
}