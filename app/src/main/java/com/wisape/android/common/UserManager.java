package com.wisape.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by LeiGuoting on 15/6/18.
 */
public class UserManager {
    public final static int USER_INFO_CHANGED = 1;
    public final static int USER_LOG_OUT = 2;
    public final static int USER_LOG_IN = 3;

    public static final String SIGN_UP_WITH_EMAIL = "1";
    public static final String SIGN_UP_WITH_FACE_BOOK = "2";
    public static final String SIGN_UP_WITH_TWITTER = "3";
    public static final String SIGN_UP_WITH_GOOGLE_PLUS = "4";

    private static final String EXTRA_USER_INFO = "user_info";
    private static final String PREFERENCES_NAME = "_user_preferences";
    private static SharedPreferences mSp;
    private static Gson mGson;

    private static UserManager sInstance;
    private static ArrayList<WeakReference<UserInfoListener>> sWeakRefListeners;

    private static UserInfo mUserEntity;

    private UserInfo user;
    private UserManager() {
    }

    public static UserManager getInstance(Context context) {
        if (sInstance == null) {
            mSp = PreferenceManager.getDefaultSharedPreferences(context);
            mGson = new Gson();
            sInstance = new UserManager();
            sWeakRefListeners = new ArrayList();
        }
        return sInstance;
    }

    private static WeakReference<UserManager> ref;

    public static UserManager instance(){
        UserManager manager;
        if(null == ref || null == (manager = ref.get())){
            synchronized (UserManager.class){
                if(null == ref || null == (manager = ref.get())){
                    manager = new UserManager();
                    ref = new WeakReference(manager);
                }
            }
        }
        return manager;
    }

    public UserInfo signIn(Context context){
        if(Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalStateException("The saveUser can not be invoked in UI thread");
        }

        return loadUser(context);
    }

    /**
     * @param context
     * @return maybe return null if user did not login
     */
    private UserInfo loadUser(Context context){
        UserInfo user;
        synchronized (this){
            user = this.user;
        }

        if(null == user){
            synchronized (this){
                if(null == this.user){
                    SharedPreferences preferences = getPreferences(context);
                    String decode = preferences.getString(EXTRA_USER_INFO, "");
                    if(0 != decode.length()){
                        try{
                            String json = new String(Base64.decode(decode.getBytes(), Base64.DEFAULT));
                            Log.d("UserManager", "#loadUser json:" + json + "\r\n decode:" + decode);
                            JSONObject jsonObject = new JSONObject(json);
                            user = UserInfo.fromJsonObject(jsonObject);
                            this.user = user;
                        }catch (JSONException e){
                            preferences.edit().clear().commit();
                            user = null;
                        }
                    }else{
                        preferences.edit().clear().commit();
                        user = null;
                    }
                }
            }
        }
        return user;
    }

    /**
     * Can not be invoked in UI thread
     * @param context
     * @param user
     */
    public void saveUser(Context context, UserInfo user){
        if(Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalStateException("The saveUser can not be invoked in UI thread");
        }

        synchronized (this){
            this.user = user;
        }
        String json = new Gson().toJson(user);
        String encode = Base64.encodeToString(json.getBytes(), Base64.DEFAULT);
        SharedPreferences preferences = getPreferences(context);
        Log.e("UserManager", "#saveUser json:" + json + "\r\n encode:" + encode);
        preferences.edit().putString(EXTRA_USER_INFO, encode).apply();
    }

    public void clearUser(Context context){
       getPreferences(context).edit().clear().commit();
    }

    private SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences(PREFERENCES_NAME, context.MODE_PRIVATE);
    }

    public String acquireAccessToken(Context context){
        if(Looper.getMainLooper() == Looper.myLooper()){
            throw new IllegalStateException("The acquireAccessToken can not be invoked in UI thread");
        }

        UserInfo user = loadUser(context);
        return null == user ? "" : user.access_token;
    }

    public void registerUserInfoListener(UserInfoListener listener) {
        sWeakRefListeners.add(new WeakReference(listener));
    }

    public void unregisterUserInfoListerner(UserInfoListener listener) {
        Iterator<WeakReference<UserInfoListener>> iter = sWeakRefListeners.iterator();
        while (iter.hasNext()) {
            WeakReference<UserInfoListener> ref = iter.next();
            if (ref.get() == null) {
                iter.remove();
            }
            if (ref.get() == listener) {
                iter.remove();
                break;
            }
        }
    }

    public void onUserInfoChanged(int type, UserInfo entity) {
        Iterator<WeakReference<UserInfoListener>> iter = sWeakRefListeners.iterator();
        while (iter.hasNext()) {
            WeakReference<UserInfoListener> ref = iter.next();
            final UserInfoListener listener = ref.get();
            if (listener == null) {
                iter.remove();
            } else {
                listener.onUserInfoChanged(type, entity);
            }
        }
    }

    public interface UserInfoListener {
        void onUserInfoChanged(int type, UserInfo entity);
    }

    public UserInfo getUserInfo() {
        String jsonString = mSp.getString(EXTRA_USER_INFO, "");
        UserInfo entity = null;
        try {
            entity = UserInfo.fromJsonObject(new JSONObject(jsonString));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return entity;
    }

    public void saveUserInfo(final UserInfo entity) {
        new AsyncTask<UserInfo, Void, UserInfo>() {

            @Override
            protected UserInfo doInBackground(UserInfo... params) {
//                sHelper.saveUserInfo(entity);
                String jsonString = mGson.toJson(params[0]);
                mSp.edit().putString(EXTRA_USER_INFO, jsonString).apply();
                return entity;
            }

            @Override
            protected void onPostExecute(UserInfo entity) {
                onUserInfoChanged(USER_LOG_IN, entity);
            }
        }.execute(entity);
    }

    /**
     * Connect with Facebook
     * Must be called on UI-Thread
     *
     * @param token
     */
    public void connectWithFacebook(final String token, final Context context, final ServerAPI.APICallback callback) {
        ServerAPI.getAPI(context).loadFacebookProfile(token, new ServerAPI.APICallback() {
            @Override
            public void onSucces(Object result) {
                mUserEntity = new UserInfo();
                JSONObject resultObj = null;
                try {
                    resultObj = new JSONObject((String)result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mUserEntity.unique_str = resultObj.optString("id");
                mUserEntity.nick_name = resultObj.optString("name");

                ServerAPI.getAPI(context).loadFacebookIco(token, new ServerAPI.APICallback() {
                    @Override
                    public void onSucces(Object result) {
                        try {
                            mUserEntity.user_ico_n = (new JSONObject((String)result)).optJSONObject("data").optString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        createByThirdLogin(context, callback, SIGN_UP_WITH_FACE_BOOK);
                    }

                    @Override
                    public void onFail(int errorCode, String errorMessage) {
                        callback.onFail(0, "Load profile fail");
                    }
                });
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                callback.onFail(0, "Load profile fail");
            }
        });
    }

    /**
     * Connect With Twitter
     * Must be called on UI-Thread
     *
     * @param token
     * @param secret
     * @param response
     */
    public void connectWithTwitter(String token, String secret, String response, final Context context, final ServerAPI.APICallback callback) {
        ServerAPI.getAPI(context).loadTwitterProfile(context, token, secret, response, new ServerAPI.APICallback() {
            @Override
            public void onSucces(Object result) {
                UserInfo entity = (UserInfo) result;
                mUserEntity = entity;

                createByThirdLogin(context, callback, SIGN_UP_WITH_TWITTER);
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                callback.onFail(0, "Load profile fail");
            }
        });
    }

    public void connectWithGoogleplus(String token, final Context context, final ServerAPI.APICallback callback) {
        ServerAPI.getAPI(context).loadGoogleplusProfile(token, new ServerAPI.APICallback() {
            @Override
            public void onSucces(Object result) {
                try {
                    JSONObject object = new JSONObject((String) result);
                    mUserEntity = new UserInfo();
                    mUserEntity.unique_str = object.optString("id");
                    mUserEntity.user_email = object.optString("email");
                    mUserEntity.nick_name = object.optString("name");
                    mUserEntity.user_ico_n = object.optString("picture");

                    createByThirdLogin(context, callback, SIGN_UP_WITH_GOOGLE_PLUS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                callback.onFail(0, "Load profile fail");
            }
        });
    }

    private void createByThirdLogin(Context context, final ServerAPI.APICallback callback, String userType) {
        ServerAPI.getAPI(context).createByThirdlogin(new ServerAPI.APICallback() {
            @Override
            public void onSucces(Object result) {
                UserInfo entity = null;
                JSONObject object = null;
                int status = 0;
                try {
                    object = new JSONObject((String) result);
                    status = object.optInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                switch (status) {
                    case 1:
                        entity = UserInfo.fromJsonObject(object.optJSONObject("info"));
                        break;
                    default:
                        break;
                }

                saveUserInfo(entity);
                callback.onSucces(null);
            }

            @Override
            public void onFail(int errorCode, String errorMessage) {
                callback.onFail(0, "Create user in our plfm with third user fail!");
            }
        }, mUserEntity, userType);
    }
}
