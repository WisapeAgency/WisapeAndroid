package com.wisape.android.logic;

import android.os.Message;

import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.http.OkhttpUtil;
import com.wisape.android.model.ActiveInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动逻辑
 * Created by huangmeng on 15/8/26.
 */
public class ActiveLogic {

    private static final String ATTR_COUNTY_CODE = "country_code";
    private static final String ATTR_NOW = "now";

    private static ActiveLogic activeLogic = new ActiveLogic();

    private Map<String, String> params = new HashMap<>();


    public static ActiveLogic getInstance(){
        return activeLogic;
    }

    private ActiveLogic(){}

    /**
     *  获取活动列表
     * @param countryCode 地区编码
     */
    public Message activeList(String countryCode,int now){

        params.clear();
        params.put(ATTR_COUNTY_CODE, countryCode);
        params.put(ATTR_NOW, now + "");

        Message mesasge = Message.obtain();

        try{
            List<ActiveInfo> activeInfoList = OkhttpUtil.execute(params,HttpUrlConstancts.USER_ACTIVE,ActiveInfo.class);
            mesasge.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            mesasge.obj = activeInfoList;
        }catch (Exception e){
            mesasge.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            mesasge.obj = e.getMessage();
        }

        return mesasge;

//        //TODO 时间戳没有生成
//        RestAdapterUtil.getApi(ActiveApi.class).activeList(counryCode, 1, new HttpRequestCallBack<List<ActiveInfo>>() {
//
//            Event<List<ActiveInfo>> event = new Event<>(EventType.GET_ACTIVE_SUCCESS);
//
//            @Override
//            public void onSuccess(List<ActiveInfo> data) {
//                event.setData(data);
//                EventBus.getDefault().post(event);
//            }
//
//            @Override
//            public void onError(String message) {
//                event.setEventType(EventType.GET_ACTIVE_FAILURE);
//                event.setEventMsg(message);
//                EventBus.getDefault().post(event);
//            }
//        });
    }

}
