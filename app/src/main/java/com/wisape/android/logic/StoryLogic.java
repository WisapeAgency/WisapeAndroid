package com.wisape.android.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.StoryMusicActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryMusicTypeEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.http.FileDownloadListener;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.http.OkhttpUtil;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.StoryMusicInfo;
import com.wisape.android.model.StoryMusicTypeInfo;
import com.wisape.android.model.StorySettingsInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.Requester;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.util.ZipUtils;
import com.wisape.android.widget.StoryMusicAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_DELETE;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
import static com.wisape.android.api.ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryLogic {
    private static final String TAG = StoryLogic.class.getSimpleName();
    private static final String SUFFIX_STORY_COMPRESS = "wis";
    public static final String PREFERENCES = "_story";
    private static final String EXTRA_STORY_TEMPLATE_TYPE = "_story_template_type";

    private static final String ATTR_ACCESS_TOKEN = "access_token";
    private static final String ATTR_STORY_ID = "sid";

    public static StoryLogic instance() {
        return new StoryLogic();
    }

    private StoryLogic() {
    }

    public StoryEntity update(Context context, ApiStory.AttrStoryInfo attr, Object tag) {
        final String storyStatus = attr.storyStatus;
        if (null == storyStatus || 0 == storyStatus.length() || STORY_STATUS_DELETE.equals(storyStatus)) {
            return null;
        }


        Uri storyUri = attr.story;
        Log.d(TAG, "#update story' uri:" + storyUri);

        StoryInfo story;
        if (STORY_STATUS_RELEASE.equals(storyStatus)) {
            try {
                String zipName = String.format(Locale.US, "%1$s.%2$s", storyUri.getLastPathSegment(), SUFFIX_STORY_COMPRESS);
                Log.d(TAG, "#update zipName:" + zipName);
                Uri storyZip = ZipUtils.zip(storyUri, EnvironmentUtils.getAppTemporaryDirectory(), zipName);
                attr.story = storyZip;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            if (new File(attr.attrStoryThumb.toString()).exists()) {
                String thumb = Utils.base64ForImage(attr.attrStoryThumb);
                attr.storyThumb = thumb;
            }

            ApiStory api = ApiStory.instance();
            story = api.update(context, attr, tag);
        } else {
            story = new StoryInfo();
            story.createtime = System.currentTimeMillis() + "";
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
        StoryEntity entity = WisapeApplication.getInstance().getStoryEntity();

        try {
            Dao<StoryEntity, Long> storyDao = helper.getDao(StoryEntity.class);
            boolean createIfNeed = true;
            if (STORY_STATUS_RELEASE.equals(storyStatus)) {
                QueryBuilder<StoryEntity, Long> qb = storyDao.queryBuilder();
                Where<StoryEntity, Long> where = qb.where();
                where.eq("id", entity.id).or().eq("storyServerId", entity.storyServerId);
                List<StoryEntity> entities = where.query();
                int size = (null == entities ? 0 : entities.size());
                if (1 == size) {
                    entity = entities.get(0);
                    entity.storyServerId = story.id;
                    entity.createAt = Long.parseLong(Utils.isEmpty(story.createtime) ? "0" : story.createtime);
                    entity.updateAt = System.currentTimeMillis();
                    entity.storyThumbUri = story.small_img;
                    entity.storyUri = story.story_url;
                    entity.storyName = story.story_name;
                    entity.storyDesc = story.description;
                    entity.likeNum = story.like_num;
                    entity.viewNum = story.view_num;
                    entity.shareNum = story.share_num;
                    entity.status = story.rec_status;
                    entity.storyLocal = attr.story.toString();
                    storyDao.update(entity);
                    createIfNeed = false;
                } else if (1 < size) {
                    storyDao.delete(entities);
                }
            }

            if (createIfNeed) {
                entity = StoryEntity.transform(story);
                entity.storyLocal = attr.story.toString();
                entity = storyDao.createIfNotExists(entity);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return entity;
    }


    public StoryInfo[] listReleaseStory(Context context, Object tag) {
        return null;
    }

    public StoryInfo[] listStory(Context context, Object tag) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryInfo[] storyArray = null;
        try {
            Dao<StoryEntity, Long> storyDao = helper.getDao(StoryEntity.class);
            QueryBuilder<StoryEntity, Long> builder = storyDao.queryBuilder();
            Where<StoryEntity, Long> where = builder.where();
            where.eq("status", ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE);
            where.eq("status", ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY);
            builder.orderBy("status", false);
            List<StoryEntity> storyList = builder.query();
            int size = (null == storyList ? 0 : storyList.size());
            if (0 < size) {
                storyArray = new StoryInfo[size];
                int index = 0;
                for (StoryEntity entity : storyList) {
                    storyArray[index] = StoryEntity.convert(entity);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return storyArray;
    }

    public StoryEntity getStoryLocalById(Context context, int id) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            Dao<StoryEntity, Integer> storyDao = helper.getDao(StoryEntity.class);
            return storyDao.queryForId(id);
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public boolean saveStoryLocal(Context context, StoryEntity story) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            Dao<StoryEntity, Integer> storyDao = helper.getDao(StoryEntity.class);
            Dao.CreateOrUpdateStatus status = storyDao.createOrUpdate(story);
            return status.isCreated();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public StoryMusicEntity[] listStoryMusicLocal(Context context) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryMusicEntity storyMusicArray[];
        Dao<StoryMusicEntity, Long> dao;
        try {
            dao = helper.getDao(StoryMusicEntity.class);
            List<StoryMusicEntity> storyMusicList = dao.queryForAll();
            int count = (null == storyMusicList ? 0 : storyMusicList.size());
            if (0 == count) {
                storyMusicArray = null;
            } else {
                storyMusicArray = new StoryMusicEntity[count];
                storyMusicArray = storyMusicList.toArray(storyMusicArray);
            }
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return storyMusicArray;
    }

    public StoryMusicEntity getStoryMusicLocalById(Context context, int id) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicEntity, Integer> dao;
        try {
            dao = helper.getDao(StoryMusicEntity.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public StoryMusicTypeEntity[] listStoryMusicTypeLocal(Context context) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryMusicTypeEntity storyMusicTypeArray[];
        Dao<StoryMusicTypeEntity, Long> dao;
        try {
            dao = helper.getDao(StoryMusicTypeEntity.class);
            List<StoryMusicTypeEntity> storyMusicTypeList = dao.queryForAll();
            int count = (null == storyMusicTypeList ? 0 : storyMusicTypeList.size());
            if (0 == count) {
                storyMusicTypeArray = null;
            } else {
                storyMusicTypeArray = new StoryMusicTypeEntity[count];
                storyMusicTypeArray = storyMusicTypeList.toArray(storyMusicTypeArray);
            }
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return storyMusicTypeArray;
    }

    public StoryMusicAdapter.StoryMusicDataInfo[] listMusicUIDataLocal(Context context) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicTypeEntity, Long> musicTypeDao;
        Dao<StoryMusicEntity, Long> musicDao;

        StoryMusicAdapter.StoryMusicDataInfo[] musicDataArray;
        try {
            musicTypeDao = helper.getDao(StoryMusicTypeEntity.class);

            List<StoryMusicTypeEntity> storyMusicTypeList = musicTypeDao.queryForAll();
            int count = (null == storyMusicTypeList ? 0 : storyMusicTypeList.size());
            if (0 == count) {
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
            for (StoryMusicTypeEntity musicType : storyMusicTypeList) {
                queryBuilder = musicDao.queryBuilder();
                queryBuilder.where().eq("type", musicType.serverId);
                queryBuilder.orderBy("name", true);
                musicList = queryBuilder.query();
                storyMusicDataList.add(musicType);
                musicCount = (null == musicList ? 0 : musicList.size());
                if (0 < musicCount) {
                    for (StoryMusicEntity music : musicList) {
                        if (StoryMusicEntity.STATUS_DOWNLOADING == music.status) {
                            if (!Downloader.containsDownloader(music.getDownloadUrl())) {
                                music.status = StoryMusicEntity.STATUS_NONE;
                                music.musicLocal = "";
                                musicDao.update(music);
                            } else {
                                StoryManager.addAction(StoryMusicActivity.ACTION_DOWNLOAD_MUSIC);
                                music.setProgress(10);
                            }
                        }
                    }
                    storyMusicDataList.addAll(musicList);
                    musicList.clear();
                }
                queryBuilder.reset();
            }

            musicDataArray = new StoryMusicAdapter.StoryMusicDataInfo[storyMusicDataList.size()];
            musicDataArray = storyMusicDataList.toArray(musicDataArray);
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return musicDataArray;
    }

    /**
     * List music and music's type form server
     *
     * @param context
     * @param tag
     * @return maybe is null if no data update.
     */
    public StoryMusicAdapter.StoryMusicDataInfo[] listMusicAndType(Context context, Object tag) {
        ApiStory api = ApiStory.instance();
        StoryMusicTypeInfo[] storyMusicTypeArray = api.listStoryMusicType(context, tag);
        int count = (null == storyMusicTypeArray ? 0 : storyMusicTypeArray.length);
        if (0 == count) {
            return null;
        }

        StoryMusicInfo[] storyMusicArray = api.listStoryMusic(context, tag);
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicTypeEntity, Long> musicTypeDao;
        Dao<StoryMusicEntity, Long> musicDao;
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean hasUpdate = false;
        db.beginTransaction();
        try {
            musicTypeDao = helper.getDao(StoryMusicTypeEntity.class);
            long updateAt = System.currentTimeMillis();
            StoryMusicTypeEntity musicTypeEntity;
            QueryBuilder queryBuilder = musicTypeDao.queryBuilder();
            List<StoryMusicTypeEntity> musicTypeList;
            int size;
            for (StoryMusicTypeInfo musicTypeInfo : storyMusicTypeArray) {
                queryBuilder.reset();
                musicTypeList = queryBuilder.where().eq("serverId", musicTypeInfo.id).query();
                size = (null == musicTypeList ? 0 : musicTypeList.size());
                if (1 == size) {
                    musicTypeEntity = musicTypeList.get(0);
                    if (!musicTypeEntity.equals(musicTypeInfo)) {
                        musicTypeEntity.update(musicTypeInfo);
                        musicTypeEntity.updateAt = updateAt;
                        musicTypeDao.update(musicTypeEntity);
                        hasUpdate = true;
                    }
                    continue;
                } else if (1 < size) {
                    musicTypeDao.delete(musicTypeList);
                }

                musicTypeEntity = StoryMusicTypeEntity.transform(musicTypeInfo);
                musicTypeEntity.createAt = updateAt;
                musicTypeEntity.updateAt = updateAt;
                musicTypeDao.createIfNotExists(musicTypeEntity);
                hasUpdate = true;
            }

            musicDao = helper.getDao(StoryMusicEntity.class);
            count = (null == storyMusicArray ? 0 : storyMusicArray.length);
            if (0 < count) {
                StoryMusicEntity musicEntity;
                queryBuilder = musicDao.queryBuilder();
                List<StoryMusicEntity> musicList;
                for (StoryMusicInfo storyMusic : storyMusicArray) {
                    queryBuilder.reset();
                    musicList = queryBuilder.where().eq("serverId", storyMusic.id).query();
                    size = (null == musicList ? 0 : musicList.size());
                    if (1 == size) {
                        musicEntity = musicList.get(0);
                        if (!musicEntity.equals(storyMusic)) {
                            musicEntity.update(storyMusic);
                            musicEntity.updateAt = updateAt;
                            musicDao.update(musicEntity);
                            hasUpdate = true;
                        }
                        continue;
                    } else if (1 < size) {
                        musicDao.delete(musicList);
                    }

                    musicEntity = StoryMusicEntity.transform(storyMusic);
                    musicEntity.createAt = updateAt;
                    musicEntity.updateAt = updateAt;
                    musicDao.createIfNotExists(musicEntity);
                    hasUpdate = true;
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }

        StoryMusicAdapter.StoryMusicDataInfo[] dataArray = null;
        if (hasUpdate) {
            dataArray = listMusicUIDataLocal(context);
        }
        return dataArray;
    }

    public StoryFontInfo[] listFont(Context context, Object tag) {
        ApiStory api = ApiStory.instance();
        return api.listStoryFont(context, tag);
    }

    public StoryTemplateEntity[] listStoryTemplateLocal(Context context) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        StoryTemplateEntity storyTemplateArray[];
        Dao<StoryTemplateEntity, Long> dao;
        try {
            dao = helper.getDao(StoryTemplateEntity.class);
            List<StoryTemplateEntity> storyTemplateList = dao.queryForAll();
            int count = (null == storyTemplateList ? 0 : storyTemplateList.size());
            if (0 == count) {
                storyTemplateArray = null;
            } else {
                storyTemplateArray = storyTemplateList.toArray(new StoryTemplateEntity[count]);
            }
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return storyTemplateArray;
    }

    public List<StoryTemplateInfo> listStoryTemplateLocalByType(Context context, long typeId) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        List<StoryTemplateInfo> storyTemplateInfoList = new ArrayList<>();
        Dao<StoryTemplateEntity, Long> dao;
        try {
            dao = helper.getDao(StoryTemplateEntity.class);
            QueryBuilder<StoryTemplateEntity, Long> builder = dao.queryBuilder();
            List<StoryTemplateEntity> storyTemplateList = builder.where().eq("type", typeId).query();
            if (storyTemplateList == null || storyTemplateList.size() == 0) {
                return storyTemplateInfoList;
            }
            for (StoryTemplateEntity entity : storyTemplateList) {
                File file = new File(StoryManager.getStoryTemplateDirectory(), entity.name + "/thumb.jpg");
                entity.thumbLocal = file.getAbsolutePath();
                File zipFile = new File(StoryManager.getStoryTemplateDirectory(),entity.name + ".zip");
                if(zipFile.exists()){
                    entity.recStatus = "1";
                }else{
                    entity.recStatus = "0";
                }
                StoryTemplateInfo storyTemplateInfo = StoryTemplateEntity.convert(entity);
                storyTemplateInfoList.add(storyTemplateInfo);

            }
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return storyTemplateInfoList;
    }

    public StoryTemplateEntity getStoryTemplateLocalById(Context context, int id) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryTemplateEntity, Integer> dao;
        try {
            dao = helper.getDao(StoryTemplateEntity.class);
            return dao.queryForId(id);
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            OpenHelperManager.releaseHelper();
        }
    }

    public StoryTemplateEntity[] listStoryTemplate(Context context, ApiStory.AttrTemplateInfo attrInfo, Object tag) {
        ApiStory api = ApiStory.instance();
        StoryTemplateInfo[] storyTemplateInfos = api.listStoryTemplate(context, attrInfo, tag);
        int count = (null == storyTemplateInfos ? 0 : storyTemplateInfos.length);
        if (0 == count) {
            return new StoryTemplateEntity[]{};
        }

        //insert into database
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryTemplateEntity, Long> dao;

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        StoryTemplateEntity storyTemplateArray[] = null;
        try {
            dao = helper.getDao(StoryTemplateEntity.class);

            QueryBuilder<StoryTemplateEntity, Long> builder = dao.queryBuilder();
            List<StoryTemplateEntity> tempEntities;
            tempEntities = builder.where().eq("type", attrInfo.type).query();

            int num;
            storyTemplateArray = new StoryTemplateEntity[count];
            int index = 0;
            long updateAt = System.currentTimeMillis();
            StoryTemplateEntity oldEntity;
            for (StoryTemplateInfo info : storyTemplateInfos) {
                StoryTemplateEntity entity = StoryTemplateEntity.transform(info);
                builder = dao.queryBuilder();
                List<StoryTemplateEntity> entities = builder.where().eq("serverId", entity.serverId).query();
                num = (null == entities ? 0 : entities.size());
                if (0 < num) {
                    oldEntity = entities.get(0);
                    dao.delete(entities);
                    entity.updateAt = updateAt;
                    entity.createAt = oldEntity.createAt;
                    entity.templateLocal = oldEntity.templateLocal;
                    entity.thumbLocal = oldEntity.thumbLocal;
                    if (tempEntities != null) {
                        tempEntities.remove(entity);
                    }
                } else {
                    entity.createAt = updateAt;
                    entity.updateAt = updateAt;
                }
                entity = dao.createIfNotExists(entity);
                storyTemplateArray[index++] = entity;
            }
            if (tempEntities != null) {
                for (StoryTemplateEntity entity : tempEntities) {
                    DeleteBuilder deleteBuilder = dao.deleteBuilder();
                    deleteBuilder.where().eq("serverId", entity.serverId);
                    deleteBuilder.delete();
                }
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return storyTemplateArray;
    }

    public void updateStoryTemplate(Context context, StoryTemplateEntity entity) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryTemplateEntity, Long> dao;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            dao = helper.getDao(StoryTemplateEntity.class);
            dao.update(entity);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    public void updateStoryMusic(Context context, StoryMusicEntity music) {
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryMusicEntity, Long> dao;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            dao = helper.getDao(StoryMusicEntity.class);
            dao.update(music);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    public JSONArray listStoryTemplateTypeLocal(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        String templateTypeString = preferences.getString(EXTRA_STORY_TEMPLATE_TYPE, null);
        if (null == templateTypeString) {
            return new JSONArray();
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(templateTypeString);
        } catch (JSONException e) {
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }
        return jsonArray;
    }

    public JSONArray listStoryTemplateType(Context context, Object tag) {
        ApiStory api = ApiStory.instance();
        JSONArray templateTypeJson = api.listStoryTemplateTypeJson(context, tag);
        if (null == templateTypeJson) {
            return new JSONArray();
        }

        //save to local
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit().putString(EXTRA_STORY_TEMPLATE_TYPE, templateTypeJson.toString()).commit();
        return templateTypeJson;
    }

    public Requester.ServerMessage getStoryTemplateUrl(Context context, ApiStory.AttrTemplateInfo attrInfo, Object tag) {
        ApiStory api = ApiStory.instance();
        return api.getStoryTemplateUrl(context, attrInfo, tag);
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }


    /**
     * 获取用户所有的story
     *
     * @param access_token 用户唯一标示
     * @return 返回信息
     */
    public Message getUserStory(String access_token) {

        /*返回的所有的story集合*/
        List<StoryEntity> storyEntitYList = new ArrayList<>();

        StoryInfo defaultStoryInfo = getDefaultStoryInfo();
        if (null != defaultStoryInfo) {
            storyEntitYList.add(0, StoryEntity.transform(defaultStoryInfo));
            downLoadStory(defaultStoryInfo);
        }

        /*服务器端story*/
        List<StoryInfo> serverStoryList = getUserStoryFromServer(access_token);
        if (null != serverStoryList) {
            storyEntitYList.addAll(serverStoryToLocalStory(serverStoryList));
        }

        /*获取本地草稿story并且进行实体转换*/
        List<StoryEntity> storyLocalEntityList = getUserStoryFromLocal(WisapeApplication.getInstance()
                .getApplicationContext());
        storyEntitYList.addAll(storyLocalEntityList);


        Message message = Message.obtain();
        message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
        message.obj = storyEntitYList;

        return message;
    }

    private List<StoryEntity> serverStoryToLocalStory(List<StoryInfo> storyInfoList) {
        List<StoryEntity> localStoryEntyList = new ArrayList<>();
        if (null != storyInfoList && storyInfoList.size() > 0) {
            for (StoryInfo storyInfo : storyInfoList) {
                StoryEntity storyEntity = StoryEntity.transform(storyInfo);
                storyEntity.storyLocal = StoryManager.getStoryDirectory().getAbsolutePath() + "/" + storyEntity.storyName;
                localStoryEntyList.add(storyEntity);
            }
        }
        return localStoryEntyList;
    }

    /**
     * 获取默认story信息
     */
    private StoryInfo getDefaultStoryInfo() {
        try {
            return OkhttpUtil.execute(HttpUrlConstancts.GET_DEFAULT_STORY_INTO, null, StoryInfo.class);
        } catch (Exception e) {
            Log.e(TAG, "获取默认story失败:" + e.getMessage());
            return null;
        }
    }

    /**
     * 从数据库查询查询用户所有的story
     *
     * @param context
     */
    private List<StoryEntity> getUserStoryFromLocal(Context context) {
        long userId = WisapeApplication.getInstance().getUserInfo().user_id;
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryEntity, Log> dao;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            dao = databaseHelper.getDao(StoryEntity.class);
            return dao.queryBuilder().where().eq("userId", userId).and().eq("status", ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY).query();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    /**
     * 从服务器查询用户的个人story
     */
    private List<StoryInfo> getUserStoryFromServer(String access_token) {

        Map<String, String> params = new HashMap<>();
        params.put(ATTR_ACCESS_TOKEN, access_token);
        try {
            return OkhttpUtil.execute(params, HttpUrlConstancts.GET_USER_STORY_FROM_SERVER, StoryInfo.class);
        } catch (Exception e) {
            Log.e(TAG, "从服务器获取story失败:" + e.getMessage());
            return null;
        }
    }

    /**
     * 下载story压缩包并且解压
     **/
    public void downLoadStory(final StoryInfo storyInfo) {
        final File file = new File(StoryManager.getStoryDirectory(), storyInfo.story_name + ".zip");
        if (!file.exists()) {
            Log.e(TAG, "开始下载story:" + storyInfo.story_name);
            OkhttpUtil.downLoadFile(storyInfo.story_url, new FileDownloadListener() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (file.exists()) {
                        file.delete();
                    }
                    FileUtils.saveByteToFile(bytes, file.getAbsolutePath());

                    try {
                        ZipUtils.unzip(Uri.fromFile(file),
                                new File(StoryManager.getStoryDirectory(), storyInfo.story_name));
                    } catch (IOException e) {
                        Log.e(TAG, "解压story文件失败:" + storyInfo.story_name + ":" +
                                file.getAbsolutePath());
                    }
                }

                @Override
                public void onError(String msg) {
                    Log.e(TAG, "下载story文件失败:" + storyInfo.story_name + ":" + msg);
                }
            });
        }
    }

    /**
     * 删除服务器与本地数据库story
     *
     * @param context
     * @param storyEntity
     * @param access_token
     * @param isServer
     * @return
     */
    public Message deleteStory(Context context, StoryEntity storyEntity, String access_token, boolean isServer) {
        Message message = Message.obtain();
        if (isServer) {
            Map<String, String> params = new HashMap<>();
            params.put(ATTR_ACCESS_TOKEN, access_token);
            params.put(ATTR_STORY_ID, storyEntity.storyServerId + "");
            try {
                OkhttpUtil.execute(HttpUrlConstancts.DELETE_USER_STORY,
                        params, StoryInfo.class);
            } catch (IOException e) {
                Log.e(TAG, "删除服务器上用户story失败");
                message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            }
        }
        deleteLocalStroy(context, storyEntity);
        message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
        return message;
    }

    /**
     * 删除本地数据库story
     *
     * @param storyEntity
     * @return
     */
    private void deleteLocalStroy(Context context, StoryEntity storyEntity) {
        storyEntity.status = ApiStory.AttrStoryInfo.STORY_STATUS_DELETE;
        long userId = WisapeApplication.getInstance().getUserInfo().user_id;
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryEntity, Log> dao;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            dao = databaseHelper.getDao(StoryEntity.class);
            int result = dao.update(storyEntity);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, "", e);
        } finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    /**
     * 更新本地数据story信息
     *
     * @param context
     * @param storyEntity
     * @return
     */
    public StoryEntity updateStory(Context context, StoryEntity storyEntity) {
        storyEntity.status = ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        Dao<StoryEntity, Integer> dao;
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            dao = databaseHelper.getDao(StoryEntity.class);
            StoryEntity entity = dao.createIfNotExists(storyEntity);
            database.setTransactionSuccessful();
            return entity;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            database.endTransaction();
            OpenHelperManager.releaseHelper();
        }
    }

    /**
     * 发布story更新状态
     *
     * @param context
     * @param storyEntity
     */
    public void publish(Context context, StoryEntity storyEntity) {
//        String status = storyEntity.status;
//        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
//        Dao<StoryEntity, Integer> dao;
//        SQLiteDatabase database = databaseHelper.getWritableDatabase();
//        database.beginTransaction();
//        storyEntity.status = "1";
//        try {
//            dao = databaseHelper.getDao(StoryEntity.class);
//            int result = dao.update(storyEntity);
//            database.setTransactionSuccessful();
//        } catch (SQLException e) {
//            Log.e(TAG, e.getMessage());
//        } finally {
//            database.endTransaction();
//            OpenHelperManager.releaseHelper();
//        }
//        storyEntity.status = status;
    }


    /**
     * 更新story设置
     *
     * @param storyId
     * @param storyName
     * @param filePath
     * @param desc
     */
    public Message updateStorySetting(long storyId, String storyName, String filePath, String desc) {
        String iconBase64 = "";
        if (null != filePath && !"".equals(filePath)) {
            iconBase64 = FileUtils.base64ForImage(filePath);
        }
        RequestBody formBody = new FormEncodingBuilder()
                .add(ATTR_STORY_NAME, storyName)
                .add(ATTR_STORY_DESC, desc)
                .add(ATTR_STORY_ID, storyId + "")
                .add(ATTR_STORY_IMG, iconBase64)
                .build();
        Message message = Message.obtain();

        try {
            StoryInfo storyInfo = OkhttpUtil.executePost(HttpUrlConstancts.STORY_SETTING, formBody, StoryInfo.class);
            message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = storyInfo;
        } catch (Exception e) {
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }
        return message;
    }

    private static final String ATTR_STORY_NAME = "story_name";
    private static final String ATTR_STORY_DESC = "description";
    private static final String ATTR_STORY_IMG = "small_img";

}
