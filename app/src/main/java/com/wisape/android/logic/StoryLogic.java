package com.wisape.android.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.UserManager;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryMusicTypeEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.StoryMusicInfo;
import com.wisape.android.model.StoryMusicTypeInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.util.ZipUtils;
import com.wisape.android.widget.StoryMusicAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_DELETE;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryLogic{
    private static final String TAG = StoryLogic.class.getSimpleName();
    private static final String SUFFIX_STORY_COMPRESS = "wis";
    public static final String PREFERENCES = "_story";
    private static final String EXTRA_STORY_TEMPLATE_TYPE = "_story_template_type";

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
            story = api.update(context, attr, tag);
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
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
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
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
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

        boolean deleted = false;
        long storyServerId = 0;
        if(STORY_STATUS_RELEASE.equals(story)){
            //delete server
            ApiStory api = ApiStory.instance();
            ApiStory.AttrStoryDeleteInfo attr = new ApiStory.AttrStoryDeleteInfo(story.id);
            StoryInfo info = api.delete(context, attr, tag);
            deleted = (null != info && ApiStory.AttrStoryInfo.STORY_STATUS_DELETE.equals(info.rec_status));
            storyServerId = story.id;
        }

        if(deleted){
            //delete local
            DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
            SQLiteDatabase db = helper.getWritableDatabase();
            db.beginTransaction();
            try{
                Dao<StoryEntity, Long> storyDao = helper.getDao(StoryEntity.class);

            }catch (SQLException e){
                Log.e(TAG, "", e);
                throw new IllegalStateException(e);
            }finally {
                OpenHelperManager.releaseHelper();
            }
        }
        return deleted;
    }

    public StoryInfo[] listReleaseStory(Context context, Object tag){
        return null;
    }

    public StoryInfo[] listStory(Context context, Object tag){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryInfo[] storyArray = null;
        try{
            Dao<StoryEntity, Long> storyDao = helper.getDao(StoryEntity.class);
            QueryBuilder<StoryEntity, Long> builder = storyDao.queryBuilder();
            Where<StoryEntity, Long> where = builder.where();
            where.eq("status", ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE);
            where.eq("status", ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY);
            builder.orderBy("status", false);
            List<StoryEntity> storyList = builder.query();
            int size = (null == storyList ? 0 : storyList.size());
            if(0 < size){
                storyArray = new StoryInfo[size];
                int index = 0;
                for(StoryEntity entity : storyList){
                    storyArray[index] = StoryEntity.convert(entity);
                }
            }
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }
        return storyArray;
    }

    public StoryMusicEntity[] listStoryMusicLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryMusicEntity storyMusicArray[];
        Dao<StoryMusicEntity, Long> dao;
        try{
            dao = helper.getDao(StoryMusicEntity.class);
            List<StoryMusicEntity> storyMusicList = dao.queryForAll();
            int count = (null == storyMusicList ? 0 : storyMusicList.size());
            if(0 == count){
                storyMusicArray = null;
            }else{
                storyMusicArray = new StoryMusicEntity[count];
                storyMusicArray = storyMusicList.toArray(storyMusicArray);
            }
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }
        return storyMusicArray;
    }

    public StoryMusicTypeEntity[] listStoryMusicTypeLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryMusicTypeEntity storyMusicTypeArray[];
        Dao<StoryMusicTypeEntity, Long> dao;
        try{
            dao = helper.getDao(StoryMusicTypeEntity.class);
            List<StoryMusicTypeEntity> storyMusicTypeList = dao.queryForAll();
            int count = (null == storyMusicTypeList ? 0 : storyMusicTypeList.size());
            if(0 == count){
                storyMusicTypeArray = null;
            }else {
                storyMusicTypeArray = new StoryMusicTypeEntity[count];
                storyMusicTypeArray = storyMusicTypeList.toArray(storyMusicTypeArray);
            }
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }
        return storyMusicTypeArray;
    }

    public StoryMusicAdapter.StoryMusicDataInfo[] listMusicUIDataLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicTypeEntity, Long> musicTypeDao;
        Dao<StoryMusicEntity, Long> musicDao;

        StoryMusicAdapter.StoryMusicDataInfo[] musicDataArray;
        try {
            musicTypeDao = helper.getDao(StoryMusicTypeEntity.class);

            List<StoryMusicTypeEntity> storyMusicTypeList = musicTypeDao.queryForAll();
            int count = (null == storyMusicTypeList ? 0 : storyMusicTypeList.size());
            if(0 == count){
                return null;
            }

            musicDao = helper.getDao(StoryMusicEntity.class);
            QueryBuilder<StoryMusicEntity, Long> queryBuilder;
            ArrayList<StoryMusicAdapter.StoryMusicDataInfo> storyMusicDataList = new ArrayList(count * 6);
            List<StoryMusicEntity> musicList;
            int musicCount;
            Collections.sort(storyMusicTypeList, new Comparator<StoryMusicTypeEntity>() {
                @Override
                public int compare(StoryMusicTypeEntity lhs, StoryMusicTypeEntity rhs) {
                    int compare;
                    if (lhs.order < rhs.order) {
                        compare = -1;
                    } else if (lhs.order > rhs.order) {
                        compare = 1;
                    } else {
                        compare = 0;
                    }
                    return compare;
                }
            });
            for(StoryMusicTypeEntity musicType : storyMusicTypeList){
                queryBuilder = musicDao.queryBuilder();
                queryBuilder.where().eq("type", musicType.serverId);
                queryBuilder.orderBy("name", true);
                musicList = queryBuilder.query();
                storyMusicDataList.add(musicType);
                musicCount = (null == musicList ? 0 : musicList.size());
                if(0 < musicCount){
                    storyMusicDataList.addAll(musicList);
                    musicList.clear();
                }
                queryBuilder.reset();
            }

            musicDataArray = new StoryMusicAdapter.StoryMusicDataInfo[storyMusicDataList.size()];
            musicDataArray = storyMusicDataList.toArray(musicDataArray);
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }
        return musicDataArray;
    }

    public StoryMusicAdapter.StoryMusicDataInfo[] listMusicAndType(Context context, Object tag){
        ApiStory api = ApiStory.instance();
        StoryMusicTypeInfo[] storyMusicTypeArray = api.listStoryMusicType(context, tag);
        int count = (null == storyMusicTypeArray ? 0 : storyMusicTypeArray.length);
        if(0 == count){
            return null;
        }

        StoryMusicInfo[] storyMusicArray = api.listStoryMusic(context, tag);
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicTypeEntity, Long> musicTypeDao;
        Dao<StoryMusicEntity, Long> musicDao;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            musicTypeDao = helper.getDao(StoryMusicTypeEntity.class);
            long updateAt = System.currentTimeMillis();
            DeleteBuilder deleteBuilder = musicTypeDao.deleteBuilder();
            //deleteBuilder.where().eq("1", 1);
            deleteBuilder.delete();
            StoryMusicTypeEntity musicTypeEntity;
            for(StoryMusicTypeInfo musicTypeInfo : storyMusicTypeArray){
                musicTypeEntity = StoryMusicTypeEntity.transform(musicTypeInfo);
                musicTypeEntity.createAt = updateAt;
                musicTypeEntity.updateAt = updateAt;
                musicTypeDao.createIfNotExists(musicTypeEntity);
            }

            musicDao = helper.getDao(StoryMusicEntity.class);
            deleteBuilder = musicDao.deleteBuilder();
            //deleteBuilder.where().eq("1", 1);
            deleteBuilder.delete();
            count = (null == storyMusicArray ? 0 : storyMusicArray.length);
            if(0 < count){
                StoryMusicEntity musicEntity;
                for (StoryMusicInfo storyMusic : storyMusicArray){
                    musicEntity = StoryMusicEntity.transform(storyMusic);
                    musicEntity.createAt = updateAt;
                    musicEntity.updateAt = updateAt;
                    musicDao.createIfNotExists(musicEntity);
                }
            }
            db.setTransactionSuccessful();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }

        return listMusicUIDataLocal(context);
    }

    public StoryTemplateEntity[] listStoryTemplateLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryTemplateEntity storyTemplateArray[];
        Dao<StoryTemplateEntity, Long> dao;
        try{
            dao = helper.getDao(StoryTemplateEntity.class);
            List<StoryTemplateEntity> storyTemplateList = dao.queryForAll();
            int count = (null == storyTemplateList ? 0 : storyTemplateList.size());
            if(0 == count){
                storyTemplateArray = null;
            }else{
                storyTemplateArray = storyTemplateList.toArray(new StoryTemplateEntity[count]);
            }
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }
        return storyTemplateArray;
    }

    public StoryTemplateEntity[] listStoryTemplate(Context context, Object tag){
        ApiStory api = ApiStory.instance();
        StoryTemplateInfo[] storyTemplateInfos = api.listStoryTemplate(context, tag);
        int count = (null == storyTemplateInfos ? 0 : storyTemplateInfos.length);
        if(0 == count){
            return null;
        }

        //insert into database
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryTemplateEntity, Long> dao;
        StoryTemplateEntity entity;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        StoryTemplateEntity storyTemplateArray[] = null;
        try{
            dao = helper.getDao(StoryTemplateEntity.class);
            List<StoryTemplateEntity> entities;
            int num;
            storyTemplateArray = new StoryTemplateEntity[count];
            int index = 0;
            long updateAt = System.currentTimeMillis();
            StoryTemplateEntity oldEntity;
            for(StoryTemplateInfo info : storyTemplateInfos){
                entity = StoryTemplateEntity.transform(info);

                QueryBuilder<StoryTemplateEntity, Long> builder = dao.queryBuilder();
                entities = builder.where().eq("serverId", entity.serverId).query();
                num = (null == entities ? 0 : entities.size());
                if(0 < num){
                    oldEntity = entities.get(0);
                    dao.delete(entities);
                    entity.updateAt = updateAt;
                    entity.createAt = oldEntity.createAt;
                    entity.thumbLocal = oldEntity.templateLocal;
                    entity.thumbLocal = oldEntity.thumbLocal;
                }else{
                    entity.createAt = updateAt;
                    entity.updateAt = updateAt;
                }
                entity = dao.createIfNotExists(entity);
                storyTemplateArray[index ++] = entity;
            }
            db.setTransactionSuccessful();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return storyTemplateArray;
    }

    public void updateStoryTemplate(Context context, StoryTemplateEntity entity){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryTemplateEntity, Long> dao;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try{
            dao = helper.getDao(StoryTemplateEntity.class);
            dao.update(entity);
            db.setTransactionSuccessful();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    public void updateStoryMusic(Context context, StoryMusicEntity music){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicEntity, Long> dao;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try{
            dao = helper.getDao(StoryMusicEntity.class);
            dao.update(music);
            db.setTransactionSuccessful();
        }catch (SQLException e){
            Log.e(TAG, "", e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    public JSONArray listStoryTemplateTypeLocal(Context context){
        SharedPreferences preferences = getSharedPreferences(context);
        String templateTypeString = preferences.getString(EXTRA_STORY_TEMPLATE_TYPE, null);
        if(null == templateTypeString){
            return null;
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(templateTypeString);
        }catch (JSONException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }
        return jsonArray;
    }

    public JSONArray listStoryTemplateType(Context context, Object tag){
        ApiStory api = ApiStory.instance();
        JSONArray templateTypeJson= api.listStoryTemplateTypeJson(context, tag);
        if(null == templateTypeJson){
            return null;
        }

        //save to local
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putString(EXTRA_STORY_TEMPLATE_TYPE, templateTypeJson.toString()).commit();
        return templateTypeJson;
    }

    private SharedPreferences getSharedPreferences(Context context){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
