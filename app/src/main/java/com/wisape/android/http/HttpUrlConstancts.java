package com.wisape.android.http;

/**
 * http请求地址常量
 * Created by huangmeng on 15/9/2.
 */
public class HttpUrlConstancts {

    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_EXCEPTION = Integer.MIN_VALUE;

    private static final String SERVER_ADDRESS = "http://106.75.197.204/index.php/v1";

    private static final String SEPERATOR = "/";

    /**
     * 用户登录
     */
    public static final String USER_LOGIN = SERVER_ADDRESS + SEPERATOR + "user/login";

    /**
     * 重置密码
     */
    public static final String PASSWORD_RESET = SERVER_ADDRESS + SEPERATOR + "user/forget";

    /**
     * 获取默认story信息
     */
    public static final String GET_DEFAULT_STORY_INTO = SERVER_ADDRESS + SEPERATOR + "story/default";

    /**
     * 从服务器获取用户story信息
     */
    public static final String GET_USER_STORY_FROM_SERVER = SERVER_ADDRESS + SEPERATOR +"story/list";
    /**
     * 修改用户信息
     */
    public static final String UPDATE_PROFILE = SERVER_ADDRESS + SEPERATOR + "user/editprofile";

    /**
     * 消息列表
     */
    public static final String MESSAGE_LIST = SERVER_ADDRESS + SEPERATOR + "message/list";

    /**
     * 单条消息
     */
    public static final String  MESSAGE_READ = SERVER_ADDRESS + SEPERATOR + "message/read";

    /**
     * 用户活动列表
     */
    public static final String USER_ACTIVE = SERVER_ADDRESS + SEPERATOR + "user/active";
}
