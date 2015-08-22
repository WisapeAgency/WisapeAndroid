package com.wisape.android.Message;

import org.w3c.dom.Text;

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
