package com.wisape.android.model;

/**
 * 消息信息
 * Created by huangmeng on 15/8/20.
 */
public class MessageInfo {

    private int id;
    private String user_email;
    private String title;
    private String subject;
    private String user_message;
    private String parsetime;
    private String createtime;
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getUser_message() {
        return user_message;
    }

    public void setUser_message(String user_message) {
        this.user_message = user_message;
    }

    public String getParsetime() {
        return parsetime;
    }

    public void setParsetime(String parsetime) {
        this.parsetime = parsetime;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
