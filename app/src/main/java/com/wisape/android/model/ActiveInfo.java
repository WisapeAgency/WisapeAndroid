package com.wisape.android.model;

/**
 * 活动信息
 * Created by huangmeng on 15/8/21.
 */
public class ActiveInfo {

    private int id;
    private String title;
    private String bg_img;
    private String url;
    private String start_time;
    private String end_time;
    private String counry;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBg_img() {
        return bg_img;
    }

    public void setBg_img(String bg_img) {
        this.bg_img = bg_img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getCounry() {
        return counry;
    }

    public void setCounry(String counry) {
        this.counry = counry;
    }
}
