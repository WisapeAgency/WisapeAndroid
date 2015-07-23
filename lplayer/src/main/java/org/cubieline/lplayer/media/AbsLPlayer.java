package org.cubieline.lplayer.media;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Array;

/**
 * Created by LeiGuoting on 14/11/11.
 */
public abstract class AbsLPlayer extends Handler implements ILPlayer, ILPlayerClient<Track>, ITracksClient<Track>{

    protected volatile int modeIndex;
    protected volatile int tracksFrom;
    protected volatile int tracksSize;
    protected volatile Track [] tracks;
    protected volatile int tracksIndex;
    protected volatile int[] modeArray;
    protected volatile long tracksSecondId;

    public AbsLPlayer(Looper looper){
        super(looper);
    }

    protected abstract void setLooping(int mode);

    /**
     * Implementing from ITracksClient
     */
    @Override
    public boolean addTrack(Track track, int from, long secondId) {
        if(null == track){
            return false;
        }

        Log.d("AbsLPlayer", "@addTrack ____ Track[" + track + "]");
        tracks = new Track[]{track};
        tracksFrom = from;
        tracksSecondId = secondId;
        tracksIndex = 0;
        tracksSize = 1;
        return true;
    }

    /**
     * Implementing from ITracksClient
     */
    @Override
    public boolean addTracks(Track[] tracks, int position, int from, long secondId) {
        if(null == tracks || 0 == tracks.length){
            return false;
        }

        this.tracks = tracks;
        tracksFrom = from;
        tracksSecondId = secondId;
        tracksIndex = position;
        tracksSize = tracks.length;
        return true;
    }


    @Override
    public int switch2NextMode() {
        modeIndex = getNextModeIndex();
        int mode = modeArray[modeIndex];
        setLooping(mode);
        return mode;
    }

    @Override
    public void switchMode(int mode) {
        int length = modeArray.length;
        for (int i = 0; i < length; i++) {
            if (mode == modeArray[i]) {
                modeIndex = i;
                break;
            }
        }
    }

    @Override
    public int getMode() {
        return modeArray[getModeIndex()];
    }

    @Override
    public int getNextMode() {
        return modeArray[getNextModeIndex()];
    }

    protected int getModeIndex() {
        if (0 > modeIndex || modeIndex >= modeArray.length) {
            modeIndex = 0;
        }

        return modeIndex;
    }

    protected int getNextModeIndex() {
        int copyModeIndex = modeIndex;
        ++copyModeIndex;
        if (copyModeIndex >= modeArray.length) {
            copyModeIndex = 0;
        }

        return copyModeIndex;
    }

    @Override
    public Track[] obtainTracks() {
        return tracks;
    }

    @Override
    public Track getTrack() {
        if(0 == tracksSize || null == tracks){
            return null;
        }else{
            return tracks[getTrackIndex()];
        }
    }

    @Override
    public int getFrom() {
        return tracksFrom;
    }

    @Override
    public long getSecondId() {
        return tracksSecondId;
    }

    @Override
    public int getTrackIndex() {
        int idx = 0;
        if(0 == tracksSize){
            return idx;
        }

        if(tracksIndex >= tracksSize){
            tracksIndex = 0;
        }

        if(-1 < tracksIndex){
            idx = tracksIndex;
        }

        return idx;
    }

    @Override
    public int getTracksSize() {
        return tracksSize;
    }

    protected int getTracksPreviousIndex(){
        int copyPosition = tracksIndex;
        -- copyPosition;
        if(0 > copyPosition){
            copyPosition = tracksSize - 1; //last track
        }

        return copyPosition;
    }

    protected int getTracksNextIndex(){
        int copyPosition = tracksIndex;
        ++ copyPosition;
        if(copyPosition >= tracksSize){
            copyPosition = 0;     //first track
        }

        return copyPosition;
    }
}