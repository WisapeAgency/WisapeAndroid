package org.cubieline.lplayer.media;

/**
 * Created by tonyley on 14/12/25.
 */
public interface IPersistenceManager {

    public void saveTracksState(Track[] tracks, int tracksFrom, long tracksSecondId, int tracksIndex, int tracksSize);

    public Track[] restoreTracksState();

    public int restoreTrackFrom();

    public long restoreSecondId();

    public int restoreTrackIndex();

    public int restoreTrackSize();

    public void saveTrackIndex(int tracksIndex);

    public void saveModeArray(int[] modeArray);

    public int[] restoreModeArray();

    public void saveModeIndex(int modeIndex);

    public int restoreModeIndex();

    public void clear();
}
