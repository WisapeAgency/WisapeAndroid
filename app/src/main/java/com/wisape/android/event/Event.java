package com.wisape.android.event;

/**
 * eventbus传递事件的载体
 * Created by huangmeng on 15/8/24.
 */
public class Event<T> {

    private String eventType;
    private String eventMsg;
    private T data;


    public Event(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getEventMsg() {
        return eventMsg;
    }

    public void setEventMsg(String eventMsg) {
        this.eventMsg = eventMsg;
    }

}
