package com.wisape.android.logic;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.wisape.android.Message.UserProfileErrorMessage;
import com.wisape.android.Message.UserProfileMessage;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.ProfileRequester;
import com.wisape.android.common.UserManager;
import com.wisape.android.database.BaseEntity;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.UserActivityEntity;
import com.wisape.android.database.UserMessageEntity;
import com.wisape.android.model.UserActivityInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.api.ApiUser;
import com.wisape.android.model.UserMessageInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.util.Utils;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class UserLogic {
    private static final String TAG = UserLogic.class.getSimpleName();
    private static WeakReference<UserLogic> ref;

    public static UserLogic instance(){
        UserLogic logic;
        if(null == ref || null == (logic = ref.get())){
            synchronized (UserLogic.class){
                if(null == ref || null == (logic = ref.get())){
                    logic = new UserLogic();
                    ref = new WeakReference(logic);
                }
            }
        }
        return logic;
    }

    private UserLogic(){}

    public UserInfo signUp(Context context, ApiUser.AttrSignUpInfo attrInfo, Object cancelableTag){
        ApiUser api = ApiUser.instance();
        UserInfo user = api.signUp(context, attrInfo, cancelableTag);
        if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
            UserManager.instance().saveUser(context, user);
        }
        return user;
    }

    /**
     * 重置密码
     * @param context
     * @param attrInfo
     * @param cancleableTag
     * @return 服务器信息
     */
    public Requester.ServerMessage resetPassword(Context context,ApiUser.AttrResetPasswordInfo attrInfo,Object cancleableTag){
        ApiUser api = ApiUser.instance();
        return api.resetPassword(context, attrInfo, cancleableTag);
    }

    public UserInfo signUpWith(Context context, ProfileRequester.ProfileInfo profile, Object cancelableTag){
        ApiUser.AttrSignUpInfo attr = new ApiUser.AttrSignUpInfo();
        attr.type = profile.platform;
        attr.email = profile.email;
        attr.userIcon = profile.icon;
        attr.nickName = profile.nickName;
        attr.uniqueStr = profile.uniqueStr;
        return signUp(context, attr, cancelableTag);
    }

    /**
     * We will send a Local Broadcast if update success.
     *
     * @param context
     * @param profile
     * @param iconUri
     * @param cancelableTag
     * @return
     */
    public UserInfo updateProfile(Context context, ApiUser.AttrUserProfile profile, Uri iconUri, Object cancelableTag){
        String iconBase64 = "";
        if(null != iconUri){
            iconBase64 = Utils.base64ForImage(iconUri);
        }
        profile.userIcon = iconBase64;
        ApiUser api = ApiUser.instance();
        UserInfo user = api.updateProfile(context, profile, cancelableTag);

        if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
            Log.e(TAG,user.toString());
            UserManager.instance().saveUser(context, user);
            WisapeApplication.getInstance().setUserInfo(user);
            EventBus.getDefault().post(new UserProfileMessage());
        }else{
            EventBus.getDefault().post(new UserProfileErrorMessage());
        }
        return user;
    }

    public long obtainNewMessageNum(Context context, Object cancelableTag){
        /*
        * 1. 查询本地数据库，统计当前本地数据库未读信息的数量。
        * */
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        long count = 0;
        try{
            Dao<UserMessageEntity, Long> msgDao = helper.getDao(UserMessageEntity.class);
            QueryBuilder<UserMessageEntity, Long> queryBuilder = msgDao.queryBuilder();
            queryBuilder.where().eq("status", UserMessageEntity.LOCAL_STATUS_NEW);
            count = msgDao.countOf(queryBuilder.prepare());
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            OpenHelperManager.releaseHelper();
        }

        return count;
    }

    public List<UserMessageEntity> listUserMessagesLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        SQLiteDatabase db = helper.getWritableDatabase();
        Dao<UserMessageEntity, Long> msgDao;
        List<UserMessageEntity> entities;

        db.beginTransaction();
        try{
            msgDao = helper.getDao(UserMessageEntity.class);
            QueryBuilder<UserMessageEntity, Long> builder = msgDao.queryBuilder();
            builder.orderBy("status", true);
            entities = builder.query();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return entities;
    }

    public List<UserMessageEntity> listUserMessages(Context context, Object cancelableTag){
        UserMessageInfo[] messages = ApiUser.instance().listUserMessages(context, cancelableTag);
        int length = (null == messages ? 0 : messages.length);
        if(0 == length){
            return null;
        }

        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        SQLiteDatabase db = helper.getWritableDatabase();
        Dao<UserMessageEntity, Long> msgDao;
        db.beginTransaction();
        try{
            msgDao = helper.getDao(UserMessageEntity.class);
            int size;
            UserMessageEntity entity;
            List<UserMessageEntity> entities;
            boolean needCreate;
            for(UserMessageInfo info : messages){
                needCreate = true;
                entities = msgDao.queryForEq("serverId", info.id);
                size = (null == entities ? 0 : entities.size());
                if(1 == size){
                    needCreate = false;
                    entity = entities.get(0);
                    entity.message = info.message;
                    msgDao.update(entity);
                }else if(1 < size){
                    DeleteBuilder delete  = msgDao.deleteBuilder();
                    delete.where().eq("serverId", info.id);
                    delete.delete();
                }

                if(needCreate){
                    msgDao.createOrUpdate(UserMessageEntity.transform(info));
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

        //select
        db.beginTransaction();
        List<UserMessageEntity> entities;
        try{
            QueryBuilder<UserMessageEntity, Long> builder = msgDao.queryBuilder();
            builder.orderBy("status", true);
            entities = builder.query();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return entities;
    }

    public UserActivityInfo[] listUserActivities(Context context, Object cancelableTag){
        ApiUser.AttrActivityInfo attr = new ApiUser.AttrActivityInfo();
        attr.countryIso = Utils.acquireCountryIso(context);
        Log.d(TAG, "#listUserActivities CountryIso:" + attr.countryIso);

        return ApiUser.instance().listUserActivities(context, attr, cancelableTag);
    }

    public List<UserActivityEntity> listUserActivitiesLocal(Context context){
        DatabaseHelper helper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        SQLiteDatabase db = helper.getWritableDatabase();
        Dao<UserActivityEntity, Long> activityDao;

        db.beginTransaction();
        List<UserActivityEntity> activities;
        try{
            activityDao = helper.getDao(UserActivityEntity.class);
            QueryBuilder<UserActivityEntity, Long> builder = activityDao.queryBuilder();
            builder.orderBy("status", true);
            activities = builder.query();
        }catch (SQLException e){
            Log.e(TAG, "", e);
            throw new IllegalStateException(e);
        }finally {
            db.endTransaction();
            OpenHelperManager.releaseHelper();
        }
        return activities;
    }
}
