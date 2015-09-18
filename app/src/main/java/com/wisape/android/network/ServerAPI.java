package com.wisape.android.network;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;

import com.android.volley.Request;
import com.wisape.android.R;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.SecurityUtils;
import com.wisape.android.util.Utils;
//import com.oauth.android.OAuthParams;
//import com.oauth.android.OAuthRequestor;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * ServerAPI
 * Created by Xugm on 15/6/16.
 */
public class ServerAPI {

    private static String SERVER_BASE_URL = "";
    private static ServerAPI sInstance;
    private static Context context;

    private ServerAPI(Context context) {
        ServerAPI.context = context;
    }

    public static ServerAPI getAPI(Context context) {
        if (sInstance == null) {
            sInstance = new ServerAPI(context);
            SERVER_BASE_URL = context.getString(R.string.app_domain);
        }
        return sInstance;
    }

    public interface APICallback {
        /**
         * @param result 调用成功返回的数据（JsonObject/JsonArray/String）
         */
        void onSucces(Object result);

        /**
         * @param errorCode    错误码
         * @param errorMessage 错误信息
         */
        void onFail(int errorCode, String errorMessage);
    }

    /**
     * 调用服务器API的通用方法
     *
     * @param url      请求地址
     * @param method   请求的方式（Request.Method.GET 或者 Request.Method.POST）
     * @param map      参数map
     * @param listener 请求API的回调
     */
    private void callAPI(final String url, final int method, final HashMap<String, String> map,
                         final APICallback listener) {
        final Handler uiHandler = new Handler(context.getMainLooper());
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                NetHelper helper = NetHelper.getInstance();
                RequestListener requestListener = new RequestListener() {
                    @Override
                    public void onComplete(final String respString) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSucces(respString);
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(final int errorCode, final String errorMessage) {
                        //TODO 此处写通用提示
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onFail(errorCode, errorMessage);
                                }
                            }
                        });
                    }
                };

                if (!Utils.isNetworkAvailable(context)) {
                    requestListener.onError(100, "Network is disconnected!");
                    return null;
                }

                switch (method) {
                    case Request.Method.POST:
                        helper.post(url, map, requestListener);
                        break;
                    case Request.Method.GET:
                        helper.get(url, map, requestListener);
                        break;
                }

                return null;
            }
        }.execute();
    }

    public void loginOrRegWithEmail(String emailStr, String passwordStr, final APICallback callback) {
        String url = SERVER_BASE_URL + "/Home/Index/login";
        HashMap<String, String> map = new HashMap();
        map.put("email", emailStr);
        map.put("passwd", SecurityUtils.md5(passwordStr));
        map.put("auth", Utils.getAuthString(emailStr));
        callAPI(url, Request.Method.POST, map, callback);
    }

    public void loadFacebookProfile(String token, final APICallback callback) {
        String url = "https://graph.facebook.com/v2.2/me";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", token);
        callAPI(url, Request.Method.GET, params, callback);
    }

    public void loadFacebookIco(String token, final APICallback callback) {
        String url = "https://graph.facebook.com/v2.2/me/picture";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("access_token", token);
        map.put("redirect", "false");
        map.put("height", "128");
        map.put("width", "128");
        callAPI(url, Request.Method.GET, map, callback);
    }

    public void createByThirdlogin(final APICallback callback, UserInfo userEntity, String type) {
        String url = SERVER_BASE_URL + "/Home/Index/third";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("openid", userEntity.unique_str);
        if (!TextUtils.isEmpty(userEntity.user_email)) {
            map.put("email", userEntity.user_email);
        }
        map.put("user_ext_name", userEntity.nick_name);
        map.put("user_icon_n", userEntity.user_ico_n);
        map.put("third", type);
        map.put("auth", Utils.getAuthString(userEntity.unique_str));
        callAPI(url, Request.Method.POST, map, callback);
    }

    public void loadTwitterProfile(Context context, String token, String secret, String response, final APICallback callback) {
//        Uri uri = Uri.parse("oauth://twitter?" + response);
//        String userId = uri.getQueryParameter("user_id");
//        String screenName = uri.getQueryParameter("screen_name");
//
//        String url = "https://api.twitter.com/1.1/users/show.json";
//        OAuthParams params = new OAuthParams(OAuthParams.VERSION_OAUTH_1,
//                OAuthParams.OAUTH_TWITTER,
//                "",
//                "",
//                context.getString(R.string.twitter_api_key),
//                context.getString(R.string.twitter_api_secret_key),
//                context.getString(R.string.twitter_api_callback_uri));
//        HashMap<String, String> map = new HashMap();
//        map.put("user_id", userId);
//        map.put("screen_name", screenName);
//        String result;
//        try {
//            result = OAuthRequestor.get(params, token, secret, url, map);
//            JSONObject object = new JSONObject(result);
//            UserInfo entity = new UserInfo();
//            entity.unique_str = object.optString("id");
//            entity.nick_name = object.optString("name");
//            entity.user_ico_n = object.optString("profile_image_url");
//            callback.onSucces(entity);
//        } catch (Exception e) {
//            e.printStackTrace();
//            callback.onFail(200, "Oauth twitter error");
//        }
    }

    public void loadGoogleplusProfile(String token, final APICallback callback) {
        String url = "https://www.googleapis.com/oauth2/v1/userinfo";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("alt", "json");
        params.put("access_token", token);
        callAPI(url, Request.Method.GET, params, callback);
    }

    public void test(final APICallback callback) {
        callAPI("http://www.easeusapp.com/api/all", Request.Method.GET, null, callback);
    }

}
