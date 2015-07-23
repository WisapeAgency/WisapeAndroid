/**
 * 
 */
package org.cubieline.lplayer.media;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * @author LeiGuoting
 *
 */
/*package*/ final class LPlayPositionTracker {

	private OnPositionTrackerListener onPositionTrackListener;

    private PositionTrackerCallback trackerCallback;
	
	private volatile int oldPosition;	//units: second
	
	private Handler handler;
	
	private volatile boolean isTrack;
	
	private volatile int duration;
	
	private static final int WHAT_TRACKING = 0x01;
	
	private static final int WHAT_STOP     = 0x02;
	
	LPlayPositionTracker(PositionTrackerCallback trackerCallback, OnPositionTrackerListener onPositionTrackListener){
		this.trackerCallback = trackerCallback;
		this.onPositionTrackListener = onPositionTrackListener;
		
		HandlerThread handlerThread = new HandlerThread("L-player-position-tracker");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()){
			@Override
			public void dispatchMessage(Message msg) {
				
				switch(msg.what){
				default : return ;
				
				case WHAT_TRACKING :
					run();
					if(isTrack){
						sendMessageDelayed(obtainMessage(WHAT_TRACKING), 1000);				
					}
					return ;
					
				case WHAT_STOP :
					removeMessages(WHAT_TRACKING);
					return;
				}
			}
		};		
	}
	
	public boolean isTracking(){
		return isTrack;
	}

    /**
     * @return duration
     */
	public int start(){
		isTrack = true;
		oldPosition = 0;
        float copyDuration = trackerCallback.getDuration();
		duration = Math.round(copyDuration / 1000f);
		handler.sendMessageDelayed(handler.obtainMessage(WHAT_TRACKING), 1000);
        return duration;
	}
	
	public void stop(){
		isTrack = false;
		handler.sendMessage(handler.obtainMessage(WHAT_STOP));
	}

	private void run() {
		float position = trackerCallback.getCurrentPosition(); //millisecond
		int second = Math.round(position / 1000f);
		if(trackerCallback.isLooping() && 1 >= second){      //for single loop
			oldPosition = 0;
			if(null != onPositionTrackListener){
				onPositionTrackListener.onSingleLoopingStart(second);
			}
		}
		
		if(-1 != duration && second > oldPosition){
			oldPosition = second;
			if(null != onPositionTrackListener){
				onPositionTrackListener.onPositionChanged(duration, second);
			}
		}
	}

    @TargetApi(18)
	public void onDestroy(){
        if(18 <= Build.VERSION.SDK_INT){
            handler.getLooper().quitSafely();
        }else{
            handler.getLooper().quit();
        }
        trackerCallback = null;
		onPositionTrackListener = null;
	}
	
	public interface OnPositionTrackerListener {
		public void onPositionChanged(int duration, int second);
		
		public void onSingleLoopingStart(int position);
	}

    public interface PositionTrackerCallback{

        /**
         * @return units: millisecond
         */
        public int getCurrentPosition();

        /**
         * @return units: millisecond
         */
        public int getDuration();

        public boolean isLooping();
    }
}
