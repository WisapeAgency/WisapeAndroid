package com.wisape.android.api;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wisape.android.R;
import com.wisape.android.model.AttributeInfo;
import com.wisape.android.model.ServerInfo;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.StoryMusicInfo;
import com.wisape.android.model.StoryMusicTypeInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.network.WWWConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class ApiStory extends ApiBase{
    private static final String TAG = ApiStory.class.getSimpleName();
    private static final int WHAT_LIST_STORY_MUSIC = 0x01;
    private static final int WHAT_LIST_STORY_TEMPLATE = 0x02;
    private static final int WHAT_LIST_STORY_TEMPLATE_TYPE = 0x03;
    private static final int WHAT_LIST_STORY_MUSIC_TYPE = 0x04;
    private static final int WHAT_LIST_STORY_FONT = 0x05;

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
        Log.d(TAG, "#delete uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return convert(message);
    }

    public StoryInfo[] listStory(Context context, AttrStoryListInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_story_list));
        Log.d(TAG, "#listStory uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return convertArray(message);
    }

    public StoryMusicInfo[] listStoryMusic(Context context, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_music_list));
        Log.d(TAG, "#listStoryMusic uri:" + uri.toString());

        Requester requester = Requester.instance();
        AttributeInfoImpl attr = new AttributeInfoImpl();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        StoryMusicInfo storyMusicArray[] = (StoryMusicInfo[])convertArray(WHAT_LIST_STORY_MUSIC, message);
        return storyMusicArray;
    }

    public StoryFontInfo[] listStoryFont(Context context, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_font_list));
        Log.d(TAG, "#listStoryFont uri:" + uri.toString());

        Requester requester = Requester.instance();
        AttributeInfoImpl attr = new AttributeInfoImpl();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        StoryFontInfo storyFontArray[] = (StoryFontInfo[])convertArray(WHAT_LIST_STORY_FONT, message);
        return storyFontArray;
    }

    public StoryTemplateInfo[] listStoryTemplate(Context context, AttrTemplateInfo attrInfo, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_template_list));
        Log.d(TAG, "#listStoryTemplate uri:" + uri.toString());

        Requester requester = Requester.instance();
        setAccessToken(context, attrInfo);
        Requester.ServerMessage message = requester.post(uri, attrInfo.convert(), tag);
        StoryTemplateInfo storyTemplateArray[] = (StoryTemplateInfo[])convertArray(WHAT_LIST_STORY_TEMPLATE, message);
        return storyTemplateArray;
    }

    public StoryTemplateTypeInfo[] listStoryTemplateType(Context context, Object tag){
        Requester.ServerMessage message = doListStoryTemplateType(context, tag);
        StoryTemplateTypeInfo templateTypeArray[] = (StoryTemplateTypeInfo[]) convertArray(WHAT_LIST_STORY_TEMPLATE_TYPE, message);
        return templateTypeArray;
    }

    public JSONArray listStoryTemplateTypeJson(Context context, Object tag){
        Requester.ServerMessage message = doListStoryTemplateType(context, tag);
        Object data = message.data;
        if(data instanceof JSONArray){
            JSONArray jsonArray = (JSONArray) data;
            return jsonArray;
        }
        return null;
    }

    public Requester.ServerMessage getStoryTemplateUrl(Context context, ApiStory.AttrTemplateInfo attr, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_template_download));
        Log.d(TAG, "#getStoryTemplateUrl uri:" + uri.toString());
        Requester requester = Requester.instance();
        setAccessToken(context, attr);
        return requester.post(uri, attr.convert(), tag);

    }

    private Requester.ServerMessage doListStoryTemplateType(Context context, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_template_gettype));
        Log.d(TAG, "#listStoryTemplateType uri:" + uri.toString());

        Requester requester = Requester.instance();
        AttributeInfoImpl attr = new AttributeInfoImpl();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return message;
    }

    public StoryMusicTypeInfo[] listStoryMusicType(Context context, Object tag){
        Uri uri = WWWConfig.acquireUri(context.getString(R.string.uri_music_gettype));
        Log.d(TAG, "#listStoryMusicType uri:" + uri.toString());

        Requester requester = Requester.instance();
        AttributeInfoImpl attr = new AttributeInfoImpl();
        setAccessToken(context, attr);
        Requester.ServerMessage message = requester.post(uri, attr.convert(), tag);
        return (StoryMusicTypeInfo[]) convertArray(WHAT_LIST_STORY_MUSIC_TYPE, message);
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
    protected ServerInfo[] onConvertArray(int what, JSONArray jsonArray, int status) {
        final int length = (null == jsonArray ? 0 :jsonArray.length());
        if(0 == length){
            return null;
        }

        ServerInfo infoArray[];
        switch (what){
            default :
                infoArray = new StoryInfo[length];
                JSONObject jsonObj;
                int index = 0;
                for(int i = 0; i < length; i++){
                    jsonObj = jsonArray.optJSONObject(i);
                    if(null == jsonObj){
                        continue;
                    }

                    infoArray[index ++] = StoryInfo.fromJsonObject(jsonObj);
                }

                if(index < length){
                    int newLength = index;
                    StoryInfo[] newArray = new StoryInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;

            case WHAT_LIST_STORY_MUSIC :
                infoArray = new StoryMusicInfo[length];
                index = 0;
                StoryMusicInfo music;
                for(int i = 0; i < length; i ++){
                    jsonObj = jsonArray.optJSONObject(i);
                    music = StoryMusicInfo.fromJsonObject(jsonObj);
                    if(null == music){
                        continue;
                    }

                    infoArray[index ++] = music;
                }

                if(index < length){
                    int newLength = index;
                    StoryMusicInfo[] newArray = new StoryMusicInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;
            case WHAT_LIST_STORY_FONT :
                infoArray = new StoryFontInfo[length];
                index = 0;
                StoryFontInfo font;
                for(int i = 0; i < length; i ++){
                    jsonObj = jsonArray.optJSONObject(i);
                    font = StoryFontInfo.fromJsonObject(jsonObj);
                    if(null == font){
                        continue;
                    }

                    infoArray[index ++] = font;
                }

                if(index < length){
                    int newLength = index;
                    StoryMusicInfo[] newArray = new StoryMusicInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;
            case WHAT_LIST_STORY_TEMPLATE :
                infoArray = new StoryTemplateInfo[length];
                index = 0;
                StoryTemplateInfo template;
                for(int i = 0; i < length; i ++){
                    jsonObj = jsonArray.optJSONObject(i);
                    template = StoryTemplateInfo.fromJsonObject(jsonObj);
                    if(null == template){
                        continue;
                    }
                    infoArray[index ++] = template;
                }

                if(index < length){
                    int newLength = index;
                    StoryTemplateInfo[] newArray = new StoryTemplateInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;

            case WHAT_LIST_STORY_TEMPLATE_TYPE :
                infoArray = new StoryTemplateTypeInfo[length];
                index = 0;
                StoryTemplateTypeInfo templateType;
                for(int i = 0; i < length; i ++){
                    jsonObj = jsonArray.optJSONObject(i);
                    templateType = StoryTemplateTypeInfo.fromJsonObject(jsonObj);
                    if(null == templateType){
                        continue;
                    }
                    infoArray[index ++] = templateType;
                }

                if(index < length){
                    int newLength = index;
                    StoryTemplateTypeInfo[] newArray = new StoryTemplateTypeInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;

            case WHAT_LIST_STORY_MUSIC_TYPE :
                infoArray = new StoryMusicTypeInfo[length];
                index = 0;
                StoryMusicTypeInfo musicType;
                for(int i = 0; i < length; i ++){
                    jsonObj = jsonArray.optJSONObject(i);
                    musicType = StoryMusicTypeInfo.fromJsonObject(jsonObj);
                    if(null == musicType){
                        continue;
                    }
                    infoArray[index ++] = musicType;
                }

                if(index < length){
                    int newLength = index;
                    StoryMusicTypeInfo[] newArray = new StoryMusicTypeInfo[newLength];
                    System.arraycopy(infoArray, 0, newArray, 0, newLength);
                    infoArray = newArray;
                }
                break;
        }
        return infoArray;
    }

    @Override
    protected ServerInfo[] onConvertArrayError(int what) {
        return null;
    }

    public static class AttrTemplateInfo extends AttrStoryInfo{
        public static final String ATTR_ID = "id";
        public static final String ATTR_TYPE = "type";

        public int id;
        public int type;

        public AttrTemplateInfo(int id, int type){
            this.id = id;
            this.type = type;
        }

        @Override
        protected void onConvert(Map<String, String> params) {
            if(0 < id){
                params.put(ATTR_ID, Integer.toString(id));
            }
            if(0 < type){
                params.put(ATTR_TYPE, Integer.toString(type));
            }
        }

        @Override
        protected int acquireAttributeNumber() {
            int count = 0;
            if(0 < id){
                count ++;
            }

            if(0 < type){
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
            super.writeToParcel(dest, flags);
            dest.writeInt(this.id);
            dest.writeInt(this.type);
        }

        public AttrTemplateInfo() {
        }

        protected AttrTemplateInfo(Parcel in) {
            super(in);
            this.id = in.readInt();
            this.type = in.readInt();
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
        public static final String STORY_DEFAULT = "B";  //默认story

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
        public static final String ATTR_MUSIC = "bg_music";
        public static final String ATTR_PREFIX = "img_prefix";
        public static final String ATTR_STORY_ID = "sid";

        public long userId;
        public String storyName;
        public String storyDescription;
        public String storyStatus;
        public String bgMusic;
        public String imgPrefix;
        public long sid;

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
            params.put(ATTR_MUSIC,bgMusic);
            params.put(ATTR_PREFIX,imgPrefix);
            params.put(ATTR_STORY_ID,Long.toString(sid));
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
            dest.writeString(this.bgMusic);
            dest.writeString(this.imgPrefix);
            dest.writeParcelable(this.story, 0);
        }

        public AttrStoryInfo() {
        }

        protected AttrStoryInfo(Parcel in) {
            this.userId = in.readLong();
            this.storyDescription = in.readString();
            this.storyStatus = in.readString();
            this.storyThumb = in.readString();
            this.bgMusic = in.readString();
            this.imgPrefix = in.readString();
            this.story = in.readParcelable(Uri.class.getClassLoader());
        }
    }
}
