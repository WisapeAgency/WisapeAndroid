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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
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

    public StoryInfo update(Context context, AttrStoryInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_story_create));
        Log.d(TAG, "#updateStory uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.MultiPartFile multiPartFile[] = {new Requester.MultiPartFile(AttrStoryInfo.ATTR_STORY, attr.story)};
        Requester.ServerMessage message = requester.postMultiPart(uri, attr.convert(), multiPartFile, tag);
        return convert(message);
    }


    public StoryInfo delete(Context context, AttrStoryDeleteInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_story_del));
        Log.d(TAG, "#updateStory uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return convert(message);
    }

    public StoryInfo[] list(Context context, AttrStoryListInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_story_list));
        Log.d(TAG, "#updateStory uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return convertArray(message);
    }

    @Override
    protected StoryInfo convert(Requester.ServerMessage message) {
        return (StoryInfo)super.convert(message);
    }

    @Override
    protected ServerInfo onConvert(int what, JSONObject json) {
        return StoryInfo.fromJsonObject(json);
    }

    @Override
    protected ServerInfo onConvertError(int what) {
        return new StoryInfo();
    }

    @Override
    protected StoryInfo[] convertArray(Requester.ServerMessage message) {
        return (StoryInfo[])super.convertArray(message);
    }

    @Override
    protected StoryInfo[] onConvertArray(int what, JSONArray jsonArray, int status) {
        final int length = (null == jsonArray ? 0 :jsonArray.length());
        if(0 == length){
            return null;
        }

        StoryInfo storyArray[] = new StoryInfo[length];
        JSONObject jsonObj;
        int index = 0;
        for(int i = 0; i < length; i++){
            jsonObj = jsonArray.optJSONObject(i);
            if(null == jsonObj){
                continue;
            }

            storyArray[index ++] = StoryInfo.fromJsonObject(jsonObj);
        }

        if(index < length){
            int newLength = index;
            StoryInfo[] newArray = new StoryInfo[newLength];
            System.arraycopy(storyArray, 0, newArray, 0, newLength);
            storyArray = newArray;
        }
        return storyArray;
    }

    @Override
    protected ServerInfo[] onConvertArrayError(int what) {
        return null;
    }

    public static class AttrStoryListInfo extends AttributeInfo{
        public static final String ATTR_PAGE = "page";
        public static final String ATTR_PAGE_SIZE = "page_size";

        public int page;
        public int pageSize;

        public AttrStoryListInfo(int page, int pageSize){
            this.page = page;
            this.pageSize = pageSize;
        }

        @Override
        protected void onConvert(Map<String, String> params) {
            if(0 < page){
                params.put(ATTR_PAGE, Integer.toString(page));
            }
            if(0 < pageSize){
                params.put(ATTR_PAGE_SIZE, Integer.toString(pageSize));
            }
        }

        @Override
        protected int acquireAttributeNumber() {
            int count = 0;
            if(0 < page){
                count ++;
            }

            if(0 < pageSize){
                count ++;
            }
            return count;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.page);
            dest.writeInt(this.pageSize);
        }

        public AttrStoryListInfo() {
        }

        protected AttrStoryListInfo(Parcel in) {
            this.page = in.readInt();
            this.pageSize = in.readInt();
        }

        public static final Creator<AttrStoryListInfo> CREATOR = new Creator<AttrStoryListInfo>() {
            public AttrStoryListInfo createFromParcel(Parcel source) {
                return new AttrStoryListInfo(source);
            }

            public AttrStoryListInfo[] newArray(int size) {
                return new AttrStoryListInfo[size];
            }
        };
    }

    public static class AttrStoryDeleteInfo extends AttrStoryInfo{
        public static final String ATTR_STORY_ID = "sid";

        public long storyId;

        public AttrStoryDeleteInfo(long storyId){
            this.storyId = storyId;
        }

        @Override
        protected void onConvert(Map<String, String> params) {
            params.put(ATTR_STORY_ID, Long.toString(storyId));
        }

        @Override
        protected int acquireAttributeNumber() {
            return 1;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeLong(this.storyId);
        }

        public AttrStoryDeleteInfo() {
        }

        protected AttrStoryDeleteInfo(Parcel in) {
            super(in);
            this.storyId = in.readLong();
        }

        public static final Creator<AttrStoryDeleteInfo> CREATOR = new Creator<AttrStoryDeleteInfo>() {
            public AttrStoryDeleteInfo createFromParcel(Parcel source) {
                return new AttrStoryDeleteInfo(source);
            }

            public AttrStoryDeleteInfo[] newArray(int size) {
                return new AttrStoryDeleteInfo[size];
            }
        };
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

    }
}
