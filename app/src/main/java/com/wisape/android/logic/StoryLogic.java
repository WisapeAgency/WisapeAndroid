package com.wisape.android.logic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.UserManager;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.util.ZipUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_DELETE;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryLogic{
    private static final String TAG = StoryLogic.class.getSimpleName();
    private static final String SUFFIX_STORY_COMPRESS = "wis";

    public static StoryLogic instance(){
        return new StoryLogic();
    }

    private StoryLogic(){}

    public StoryInfo update(Context context, ApiStory.AttrStoryInfo attr, Object tag){
        final String storyStatus = attr.storyStatus;
        if(null == storyStatus || 0 == storyStatus.length() || STORY_STATUS_DELETE.equals(storyStatus)){
            return null;
        }

        UserInfo user = UserManager.instance().signIn(context);
        attr.userId = user.user_id;

        Uri storyUri = attr.story;
        Log.d(TAG, "#update story' uri:" + storyUri);

        StoryInfo story;
        if(STORY_STATUS_RELEASE.equals(storyStatus)){
            try {
                String zipName = String.format(Locale.US, "%1$s.%2$s", storyUri.getLastPathSegment(), SUFFIX_STORY_COMPRESS);
                Log.d(TAG, "#update zipName:" + zipName);
                Uri storyZip = ZipUtils.zip(storyUri, EnvironmentUtils.getAppTemporaryDirectory(), zipName);
                attr.story = storyZip;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            String thumb = Utils.base64ForImage(attr.attrStoryThumb);
            attr.storyThumb = thumb;

            ApiStory api = ApiStory.instance();
            story = api.updateStory(context, attr, tag);
        }else{
            story = new StoryInfo();
            story.createtime = System.currentTimeMillis();
            story.small_img = attr.attrStoryThumb.toString();
            story.story_url = attr.story.toString();
            story.story_name = attr.storyName;
            story.description = attr.storyDescription;
            story.rec_status = attr.storyStatus;
            story.uid = attr.userId;
        }

        Log.d(TAG, "#update story:" + story);
        //save to local
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        StoryEntity entity = null;
        try{
            Dao<StoryEntity, Long> storyDao = helper.getDao(StoryEntity.class);
            boolean createIfNeed = true;
            if(STORY_STATUS_RELEASE.equals(storyStatus)){
                QueryBuilder<StoryEntity, Long> qb = storyDao.queryBuilder();
                Where<StoryEntity, Long> where = qb.where();
                where.eq("storyServerId", story.id);
                List<StoryEntity> entities = where.query();
                int size = (null == entities ? 0 : entities.size());
                if(1 == size){
                    entity = entities.get(0);
                    entity.createAt = story.createtime;
                    entity.updateAt = System.currentTimeMillis();
                    entity.storyThumbUri = story.small_img;
                    entity.storyUri = story.story_url;
                    entity.storyName = story.story_name;
                    entity.storyDesc = story.description;
                    entity.likeNum = story.like_num;
                    entity.viewNum = story.view_num;
                    entity.shareNum = story.share_num;
                    entity.status = story.rec_status;
                    storyDao.update(entity);
                    createIfNeed = false;
                }else if(1 < size){
                    storyDao.delete(entities);
                }
            }

            if(createIfNeed){
                entity = StoryEntity.transform(story);
                entity = storyDao.createIfNotExists(entity);
            }
            db.setTransactionSuccessful();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            //do nothing
        }finally {
            db.endTransaction();
        }

        if(STORY_STATUS_TEMPORARY.equals(storyStatus) && null != entity){
            story.id = entity.id;
        }
        return story;
    }

    /**
     * @param context
     * @param story
     * @param tag
     * @return
     */
    public boolean delete(Context context, StoryInfo story, Object tag){
        final String storyStatus = story.rec_status;
        if(STORY_STATUS_DELETE.equals(storyStatus)){
            return false;
        }

        if(STORY_STATUS_RELEASE.equals(story)){

        }

        return true;
    }

    public List<StoryInfo> listReleaseStory(Context context, Object tag){
        return null;
    }
}
