/**
 * 
 */
package org.cubieline.lplayer.media;

/**
 * @author LeiGuoting
 *
 */
public interface ICommand {

	public static final int CMD_START				= 0x04;
	
	public static final int CMD_PAUSE				= 0x05;
	
	public static final int CMD_CLOSE               = 0x06;
	
	public static final int	CMD_UPDATE_POSITION		= 0x09;
	
	public static final int CMD_CLIENT_STATUS_CHANGED = 0x10;
	
	public static final int CMD_TRACK_CHANGED		= 0x11;
	
	public static final int CMD_NEXT				= 0x12;

    public static final int CMD_PREVIOUS            = 0x13;

    public static final int CMD_LOGOUT = 0x14;
}
