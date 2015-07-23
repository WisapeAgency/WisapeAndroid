/**
 * 
 */
package org.cubieline.lplayer.media;

/**
 * @author LeiGuoting
 */
public interface ILPlayer extends ICommand, IKey{

	public int MODE_PLAY_LOOP			= 0x001;
	
	public int MODE_PLAY_RANDOM			= 0x002;
	
	public int MODE_PLAY_SINGLE_LOOP	= 0x003;

    public int MODE_PLAY_ORDER          = 0x004;

    /*package*/ String MODE_CONFIG_LOOP = "MODE_LOOP";
    /*package*/ String MODE_CONFIG_RANDOM = "MODE_RANDOM";
    /*package*/ String MODE_CONFIG_SINGLE_LOOP = "MODE_SINGLE_LOOP";
    /*package*/ String MODE_CONFIG_ORDER = "MODE_ORDER";
	
	/*Inner status of LPlayer*/
	/*package*/ int STATUS_IDLE			= 0x110;

	/*package*/ int STATUS_PREPARE      = 0x101;

	/*package*/ int STATUS_PLAYING		= 0x103;

	/*package*/ int STATUS_PAUSED		= 0x104;
	
	/*package*/ int STATUS_BUFFERING	= 0x105;
	
	/*package*/ int STATUS_COMPLETED	= 0x107;

    /*package*/ int STATUS_STOPED		= 0x106;

    /*package*/ String META_DATA_PLUGIN_CUSTOMS = "org.cubieline.lplayer.plugin.CUSTOMS";

    /*package*/ String META_DATA_PLUGIN_DEFAULT = "org.cubieline.lplayer.plugin.NEED_DEFAULT";

    /*package*/ String META_DATA_MODE = "org.cubieline.lplayer.MODE";

    //public void innerNext();

	public void stop();

    public void logout();
	
	public void release();

	public interface OnPlayerListener<T>{
		/**
		 * @param track
		 * @return true play, false stop
		 */
		public boolean onBegin(T track);
	}
}
