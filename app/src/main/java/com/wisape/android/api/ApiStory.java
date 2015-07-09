package com.wisape.android.api;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.ServerInfo;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.network.WWWConfig;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class ApiStory extends ApiBase{
    private static final String TAG = ApiStory.class.getSimpleName();

    public static ApiStory instance(){
        return new ApiStory();
    }

    private ApiStory(){}

    public StoryInfo updateStory(Context context, AttrStoryInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_story_create));
        Log.d(TAG, "#updateStory uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.MultiPartFile multiPartFile[] = {new Requester.MultiPartFile(AttrStoryInfo.ATTR_STORY, attr.story)};
        Requester.ServerMessage message = requester.postMultiPart(uri, attr.convert(), multiPartFile, tag);
        return convert(message);
    }

    @Override
    protected StoryInfo convert(Requester.ServerMessage message) {
        return (StoryInfo)super.convert(message);
    }

    @Override
    protected ServerInfo onConvert(JSONObject json) {
        return StoryInfo.fromJsonObject(json);
    }

    @Override
    protected ServerInfo onConvertError() {
        return new StoryInfo();
    }

    public static class AttrStoryInfo extends AttributeInfo{
        public static final String STORY_STATUS_RELEASE = "A";       //发布
        public static final String STORY_STATUS_DELETE = "D";        //删除
        public static final String STORY_STATUS_TEMPORARY = "T";     //草稿

        public static final String ATTR_UID = "uid";
        public static final String ATTR_DESCRIPTION = "description";

        /**
         * Story状态
         * See
         * {@link #STORY_STATUS_RELEASE}
         * {@link #STORY_STATUS_DELETE}
         * {@link #STORY_STATUS_TEMPORARY}
         */
        public static final String ATTR_STORY_STATUS = "rec_status";

        public static final String ATTR_STORY_THUMB = "small_img";
        public static final String ATTR_STORY_NAME = "story_name";
        public static final String ATTR_STORY = "zip_file";

        public long userId;
        public String storyName;
        public String storyDescription;
        public String storyStatus;

        /**
         * Base64
         */
        public String storyThumb;
        public Uri story;

        public Uri attrStoryThumb;

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_UID, Long.toString(userId));
            params.put(ATTR_STORY_NAME, storyName);
            params.put(ATTR_DESCRIPTION, storyDescription);
            params.put(ATTR_STORY_STATUS, storyStatus);
            params.put(ATTR_STORY_THUMB, storyThumb);
        }

        @Override
        protected int acquireAttributeNumber() {
            return 5;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.userId);
            dest.writeString(this.storyDescription);
            dest.writeString(this.storyStatus);
            dest.writeString(this.storyThumb);
            dest.writeParcelable(this.story, 0);
        }

        public AttrStoryInfo() {
        }

        protected AttrStoryInfo(Parcel in) {
            this.userId = in.readLong();
            this.storyDescription = in.readString();
            this.storyStatus = in.readString();
            this.storyThumb = in.readString();
            this.story = in.readParcelable(Uri.class.getClassLoader());
        }

        public static final Creator<AttrStoryInfo> CREATOR = new Creator<AttrStoryInfo>() {
            public AttrStoryInfo createFromParcel(Parcel source) {
                return new AttrStoryInfo(source);
            }

            public AttrStoryInfo[] newArray(int size) {
                return new AttrStoryInfo[size];
            }
        };
    }
}
