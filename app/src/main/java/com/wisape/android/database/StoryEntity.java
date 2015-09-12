package com.wisape.android.database;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.wisape.android.model.StoryInfo;

/**
 * Created by LeiGuoting on 9/7/15.
 */
@DatabaseTable(tableName = "_story")
public class StoryEntity extends BaseEntity implements Parcelable{

    @DatabaseField()
    public long storyServerId;

    @DatabaseField()
    public String storyName;

    @DatabaseField()
    public String storyDesc;

    @DatabaseField(dataType= DataType.STRING)
    public String storyThumbUri;

    @DatabaseField(dataType= DataType.STRING)
    public String storyUri;

    @DatabaseField(dataType = DataType.STRING)
    public String storyMusicLocal;

    @DatabaseField(dataType = DataType.STRING)
    public String storyMusicName;

    @DatabaseField
    public long musicServerId;

    @DatabaseField(dataType = DataType.STRING)
    public String storyGestor;

    /**
     * Story status
     * One of
     * {@link com.wisape.android.api.ApiStory.AttrStoryInfo#STORY_STATUS_DELETE}
     * {@link com.wisape.android.api.ApiStory.AttrStoryInfo#STORY_STATUS_RELEASE}
     * {@link com.wisape.android.api.ApiStory.AttrStoryInfo#STORY_STATUS_TEMPORARY}
     */
    @DatabaseField()
    public String status;

    @DatabaseField()
    public long userId;

    @DatabaseField()
    public int likeNum;

    @DatabaseField()
    public int viewNum;

    @DatabaseField()
    public int shareNum;

    @DatabaseField(dataType= DataType.STRING)
    public String storyLocal;

    public static StoryEntity transform(StoryInfo info){
        StoryEntity entity = new StoryEntity();
        entity.storyServerId = info.id;
        entity.storyName = info.story_name;
        entity.storyDesc = info.description;
        entity.storyThumbUri = info.small_img;
        entity.storyUri = info.story_url;
        entity.status = info.rec_status;
        entity.likeNum = info.like_num;
        entity.viewNum = info.view_num;
        entity.shareNum = info.share_num;
        entity.userId = info.uid;
        entity.createAt = Long.parseLong(info.createtime);
        entity.updateAt = SystemClock.uptimeMillis();
        return entity;
    }

    public static StoryInfo convert(StoryEntity entity){
        StoryInfo info = new StoryInfo();
        info.id = (0 < entity.storyServerId ? entity.storyServerId : entity.id);
        info.story_name = entity.storyName;
        info.description = entity.storyDesc;
        info.small_img = entity.storyThumbUri;
        info.story_url = entity.storyUri;
        info.rec_status = entity.status;
        info.uid = entity.userId;
        info.like_num = entity.likeNum;
        info.view_num = entity.viewNum;
        info.share_num = entity.shareNum;
        info.createtime = entity.createAt+"";
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.storyServerId);
        dest.writeString(this.storyName);
        dest.writeString(this.storyDesc);
        dest.writeString(this.storyThumbUri);
        dest.writeString(this.storyUri);
        dest.writeString(this.status);
        dest.writeLong(this.userId);
        dest.writeInt(this.likeNum);
        dest.writeInt(this.viewNum);
        dest.writeInt(this.shareNum);
        dest.writeString(this.storyLocal);
    }

    public StoryEntity() {
    }

    protected StoryEntity(Parcel in) {
        this.storyServerId = in.readLong();
        this.storyName = in.readString();
        this.storyDesc = in.readString();
        this.storyThumbUri = in.readString();
        this.storyUri = in.readString();
        this.status = in.readString();
        this.userId = in.readLong();
        this.likeNum = in.readInt();
        this.viewNum = in.readInt();
        this.shareNum = in.readInt();
        this.storyLocal = in.readString();
    }

    public static final Creator<StoryEntity> CREATOR = new Creator<StoryEntity>() {
        public StoryEntity createFromParcel(Parcel source) {
            return new StoryEntity(source);
        }

        public StoryEntity[] newArray(int size) {
            return new StoryEntity[size];
        }
    };
}
