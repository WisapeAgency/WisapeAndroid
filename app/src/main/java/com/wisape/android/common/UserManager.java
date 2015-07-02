package com.wisape.android.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ServerAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Xugm on 15/6/18.
 */
public class UserManager {
    public final static int USER_INFO_CHANGED = 1;
    public final static int USER_LOG_OUT = 2;
    public final static int USER_LOG_IN = 3;

    public static final String USER_TYPE_FACE_BOOK = "1";
    public static final String USER_TYPE_TWITTER = "2";
    public static final String USER_TYPE_GOOGLE_PLUS = "3";
    public static final String USER_TYPE_WECHAT = "4";

    private static final String SP_USER_INFO_KEY = "user_info";
    private static SharedPreferences mSp;
    private static Gson mGson;

    private static UserManager sInstance;
    private static ArrayList<WeakReference<UserInfoListener>> sWeakRefListeners;

    private static UserInfo mUserEntity;

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

    public String acquireAccessToken(){
        return "";
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
        String jsonString = mSp.getString(SP_USER_INFO_KEY, "");
        UserInfo entity = null;
        try {
            entity = UserInfo.parse(new JSONObject(jsonString));
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
                mSp.edit().putString(SP_USER_INFO_KEY, jsonString).apply();
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
                mUserEntity.user_ext_id = resultObj.optString("id");
                mUserEntity.nick_name = resultObj.optString("name");

                ServerAPI.getAPI(context).loadFacebookIco(token, new ServerAPI.APICallback() {
                    @Override
                    public void onSucces(Object result) {
                        try {
                            mUserEntity.user_icon_n = (new JSONObject((String)result)).optJSONObject("data").optString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        createByThirdLogin(context, callback, USER_TYPE_FACE_BOOK);
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

                createByThirdLogin(context, callback, USER_TYPE_TWITTER);
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
                    mUserEntity.user_ext_id = object.optString("id");
                    mUserEntity.email = object.optString("email");
                    mUserEntity.nick_name = object.optString("name");
                    mUserEntity.user_icon_n = object.optString("picture");

                    createByThirdLogin(context, callback, USER_TYPE_GOOGLE_PLUS);
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
                callback.onFail(0, "Create user in our platform with third user fail!");
            }
        }, mUserEntity, userType);
    }
}
