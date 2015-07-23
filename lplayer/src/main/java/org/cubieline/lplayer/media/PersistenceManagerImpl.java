package org.cubieline.lplayer.media;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by tonyley on 14/12/25.
 */
/*package*/ class PersistenceManagerImpl implements IPersistenceManager {
    private static final String TAG = PersistenceManagerImpl.class.getSimpleName();
    private static final String CONTENT_MARK = "||";
    private static final String FORMAT_TRACK_KEY = "track_item@%1$d";
    private static final String FORMAT_MODE_KEY = "mode_item@%1$d";
    private static final String EXTRA_TRACK_LENGTH = "track_length";
    private static final String EXTRA_MODE_LENGTH = "mode_length";
    private static final String EXTRA_TRACK_FROM = "track_from";
    private static final String EXTRA_TRACK_SECOND_ID = "track_second_id";
    private static final String EXTRA_TRACK_INDEX = "track_index";
    private static final String EXTRA_TRACK_SIZE = "track_size";
    private static final String EXTRA_MODE_INDEX = "mode_index";
    private SharedPreferences preferences;

    PersistenceManagerImpl(Context context) {
        preferences = context.getSharedPreferences("lPlayer", Context.MODE_PRIVATE);
    }

    @Override
    public void clear() {
        preferences.edit().clear().commit();
    }

    @Override
    public void saveTracksState(Track[] tracks, int tracksFrom, long tracksSecondId, int tracksIndex, int tracksSize) {
        int length = (null == tracks ? 0 : tracks.length);
        if (0 == length) {
            return;
        }

        Track track;
        String key;
        String content;
        String className;
        String trackJson;
        final SharedPreferences.Editor editor = preferences.edit();
        clearTrackArray(preferences, editor);
        editor.putInt(EXTRA_TRACK_LENGTH, length);
        for (int i = 0; i < length; i++) {
            track = tracks[i];
            className = track.getClass().getName();
            trackJson = track.toJsonString();
            key = String.format(Locale.US, FORMAT_TRACK_KEY, i);
            content = String.format(Locale.US, "%1$s%2$s%3$s", className, CONTENT_MARK, trackJson);
            editor.putString(key, content);
            Log.d(TAG, "@saveTracksState key[" + key + "], content[" + content + "]");
        }

        editor.putInt(EXTRA_TRACK_FROM, tracksFrom);
        editor.putLong(EXTRA_TRACK_SECOND_ID, tracksSecondId);
        editor.putInt(EXTRA_TRACK_INDEX, tracksIndex);
        editor.putInt(EXTRA_TRACK_SIZE, tracksSize);
        editor.commit();
    }

    private void clearTrackArray(SharedPreferences preferences, SharedPreferences.Editor editor){
        int length = preferences.getInt(EXTRA_TRACK_LENGTH, 0);
        if(0 == length){
            return;
        }

        String key;
        for(int i = 0; i < length; i++){
            key = String.format(Locale.US, FORMAT_TRACK_KEY, i);
            editor.remove(key);
        }
        editor.apply();
    }

    @Override
    public Track[] restoreTracksState() {
        int length = preferences.getInt(EXTRA_TRACK_LENGTH, 0);
        Log.d(TAG, "@restoreTracksState begin ____ length:" + length);
        if (0 == length) {
            return new Track[0];
        }

        String key;
        String content;
        String json;
        String className;
        Track track;
        int index = 0;
        int splitIndex;
        Track[] tracks = new Track[length];
        try {
            for (int i = 0; i < length; i++) {
                Log.d(TAG, "@restoreTracksState for ____ i:" + i);
                key = String.format(Locale.US, FORMAT_TRACK_KEY, i);
                content = preferences.getString(key, null);
                if (null == content || 0 == content.length()) {
                    continue;
                }

                splitIndex = content.indexOf(CONTENT_MARK);
                className = content.substring(0, splitIndex);
                json = content.substring(splitIndex + CONTENT_MARK.length());

                track = (Track) Class.forName(className).newInstance();
                tracks[index++] = track.parseFromJson(new JSONObject(json));
                Log.d(TAG, "@restoreTracksState  className:" + className + ", json:" + json);
            }
        } catch (Throwable e) {
            Log.e(TAG,"", e);
            throw new IllegalStateException(e);
        }

        //truncate
        if (index < length) {
            Track[] realTrackArray = new Track[index];
            System.arraycopy(tracks, 0, realTrackArray, 0, index);
            tracks = realTrackArray;
        }
        return tracks;
    }

    @Override
    public int restoreTrackFrom() {
        return preferences.getInt(EXTRA_TRACK_FROM, 0);
    }

    @Override
    public long restoreSecondId() {
        return preferences.getLong(EXTRA_TRACK_SECOND_ID, 0);
    }

    @Override
    public int restoreTrackIndex() {
        return preferences.getInt(EXTRA_TRACK_INDEX, 0);
    }

    @Override
    public int restoreTrackSize() {
        return preferences.getInt(EXTRA_TRACK_SIZE, 0);
    }


    @Override
    public void saveTrackIndex(int tracksIndex) {
        preferences.edit().putInt(EXTRA_TRACK_INDEX, tracksIndex).commit();
    }

    @Override
    public void saveModeArray(int[] modeArray) {
        int length = (null == modeArray ? 0 : modeArray.length);
        if(0 == length){
            return;
        }

        String key;
        final SharedPreferences.Editor editor = preferences.edit();
        clearModeArray(preferences, editor);
        editor.putInt(EXTRA_MODE_LENGTH, length);
        for(int i = 0; i < length; i ++){
            key = String.format(Locale.US, FORMAT_MODE_KEY, i);
            editor.putInt(key, modeArray[i]);
        }
        editor.commit();
    }

    private void clearModeArray(SharedPreferences preferences, SharedPreferences.Editor editor){
        int length = preferences.getInt(EXTRA_MODE_LENGTH, 0);
        if(0 == length){
            return;
        }

        String key;
        for(int i = 0; i < length; i ++){
            key = String.format(Locale.US, FORMAT_MODE_KEY, i);
            editor.remove(key);
        }
        editor.apply();
    }

    @Override
    public int[] restoreModeArray() {
        int length = preferences.getInt(EXTRA_MODE_LENGTH, 0);
        if(0 == length){
            return new int[0];
        }

        String key;
        int[] modeArray = new int[length];
        for(int i = 0; i < length; i ++){
            key = String.format(Locale.US, FORMAT_MODE_KEY, i);
            modeArray[i] = preferences.getInt(key, 0);
        }
        return modeArray;
    }

    @Override
    public void saveModeIndex(int modeIndex) {
        preferences.edit().putInt(EXTRA_MODE_INDEX, modeIndex).commit();
    }

    @Override
    public int restoreModeIndex() {
        return preferences.getInt(EXTRA_MODE_INDEX, 0);
    }
}
