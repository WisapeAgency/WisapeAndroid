/**
 *
 */
package org.cubieline.lplayer.media;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcelable;

/**
 * @author LeiGuoting
 *
 */
public interface Track extends Album, Artist, IPluginCode, Parcelable{

	public int getType();

	public String toJsonString();

	public Track parseFromJson(JSONObject json)throws JSONException;

    public long getTrackId();

    public String getTrackName();

    public String getDataSource();

    public long getDuration();
}
