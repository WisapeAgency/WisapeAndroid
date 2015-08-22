package com.wisape.android.msg;

/**
 * 更新用户信息
 * Created by huangmeng on 15/8/20.
 */
public class UserProfileMessage extends Message {

    private String userEmail;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
