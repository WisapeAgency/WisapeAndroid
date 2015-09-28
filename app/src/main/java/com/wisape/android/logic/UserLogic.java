package com.wisape.android.logic;

import android.content.SharedPreferences;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;
import com.wisape.android.WisapeApplication;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.http.OkhttpUtil;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.FileUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户相关业务逻辑
 * Created by LeiGuoting on 3/7/15.
 */
public class UserLogic {
    private static final String TAG = UserLogic.class.getSimpleName();

    private static UserLogic userLogic = new UserLogic();

    private static final String ATTR_TYPE = "type";
    private static final String ATTR_EMAIL = "user_email";
    private static final String ATTR_PASSWORD = "user_pwd";
    private static final String ATTR_USER_ICON = "user_ico";
    private static final String ATTR_NICK_NAME = "nick_name";
    private static final String ATTR_UNIQUE_STR = "unique_str";
    private static final String ATTR_INSTALL_ID = "install_id";
    private static final String ATTR_ACCESS_TOKEN = "access_token";

    private static final String EXTRA_USER_INFO = "user_info";

    private Map<String, String> params = new HashMap<>();

    public static UserLogic instance() {
        return userLogic;
    }

    private UserLogic() {
    }


    /**
     * 第三方登录
     *
     * @param type      登录类型
     * @param email     邮箱
     * @param userIcon  用户头像
     * @param nickName  昵称
     * @param uniqueStr 唯一
     * @param installId parses使用
     * @return 信息
     */
    public Message signUpWith(String type, String email, String userIcon, String nickName, String uniqueStr, String installId) {
        params.clear();
        params.put(ATTR_TYPE, type + "");
        params.put(ATTR_EMAIL, email);
        params.put(ATTR_INSTALL_ID, installId);
        params.put(ATTR_NICK_NAME, nickName);
        params.put(ATTR_USER_ICON, userIcon);
        params.put(ATTR_UNIQUE_STR, uniqueStr);

        return singUp(params);
    }

    /**
     * 邮箱登录
     *
     * @param type  登录类型
     * @param email 登录email
     * @param pwd   登录密码
     */
    public Message signUp(String type, String email, String pwd, String installId) {
        params.clear();
        params.put(ATTR_TYPE, type);
        params.put(ATTR_EMAIL, email);
        params.put(ATTR_PASSWORD, pwd);
        params.put(ATTR_INSTALL_ID, installId);

        return singUp(params);
    }

    private Message singUp(Map<String, String> params) {
        Message message = Message.obtain();
        try {
            UserInfo userInfo = OkhttpUtil.execute(HttpUrlConstancts.USER_LOGIN, params, UserInfo.class);
            saveUserToSharePrefrence(userInfo);
            message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = userInfo;
        } catch (IOException e) {
            message.arg1 =  HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }
        return message;
    }

    /**
     * 重置密码
     *
     * @param email 用户的邮箱
     */
    public Message passwordRest(String email) {
        params.clear();
        params.put(ATTR_EMAIL, email);

        Message message = Message.obtain();
        try {
            OkhttpUtil.execute(HttpUrlConstancts.PASSWORD_RESET, params);
            message.arg1 =  HttpUrlConstancts.STATUS_SUCCESS;
        } catch (Exception e) {
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }
        return message;
    }

    /**
     * 从本地读取用户信息
     */
    public UserInfo loaderUserFromLocal() {
        UserInfo userInfo = null;
        SharedPreferences sharedPreferences = WisapeApplication.getInstance().getSharePrefrence();
        String decode = sharedPreferences.getString(EXTRA_USER_INFO, "");
        if (0 != decode.length()) {
            String gson = new String(Base64.decode(decode, Base64.DEFAULT));
            userInfo = new Gson().fromJson(gson, UserInfo.class);
        }
        return userInfo;
    }


    /**
     * 将用户信息保存到shareprefrence
     *
     * @param userInfo 加密后的用户信息
     */
    public void saveUserToSharePrefrence(UserInfo userInfo) {
        Log.w(TAG, "saveUserToSahrePrefrence:" + new Gson().toJson(userInfo));
        String userEncode = new Gson().toJson(userInfo);
        WisapeApplication.getInstance().getSharePrefrence().edit()
                .putString(EXTRA_USER_INFO, Base64.encodeToString(userEncode.getBytes(), Base64.DEFAULT)).apply();

    }

    /**
     * 清除用户信息
     */
    public void clearUserInfo() {
        WisapeApplication.getInstance().getSharePrefrence().edit().clear().apply();
    }


    /**
     * 更新用户信息
     *
     * @param nickName    昵称
     * @param filePath    用户图标地址
     * @param userEmail   用户邮箱
     * @param accessToken 唯一标志符
     */
    public Message updateProfile(String nickName, String filePath, String userEmail, String accessToken) {
        String iconBase64 = "";
        if (null != filePath && !"".equals(filePath)) {
            iconBase64 = FileUtils.base64ForImage(filePath);
        }
        RequestBody formBody = new FormEncodingBuilder()
                .add(ATTR_NICK_NAME, nickName)
                .add(ATTR_EMAIL, userEmail)
                .add(ATTR_ACCESS_TOKEN, accessToken)
                .add(ATTR_USER_ICON,iconBase64)
                .build();
        Message message = Message.obtain();
        try {
            UserInfo userInfo = OkhttpUtil.executePost(HttpUrlConstancts.UPDATE_PROFILE, formBody, UserInfo.class);
            saveUserToSharePrefrence(userInfo);
            message.arg1 =  HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = userInfo;
        } catch (Exception e) {
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }
        return message;
    }
}
