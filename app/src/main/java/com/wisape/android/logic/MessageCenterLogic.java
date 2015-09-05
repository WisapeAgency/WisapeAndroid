package com.wisape.android.logic;

import android.os.Message;

import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.http.OkhttpUtil;
import com.wisape.android.model.MessageInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息中心逻辑
 * Created by huangmeng on 15/8/24.
 */
public class MessageCenterLogic {

    private static final String ATTR_USER_ID = "uid";
    private static final String ATTR_MESSAGE_ID = "mid";

    private static MessageCenterLogic messageCenterLogic = new MessageCenterLogic();
    private Map<String, String> params = new HashMap<>();

    public static MessageCenterLogic getInstance() {
        return messageCenterLogic;
    }

    private MessageCenterLogic() {
    }

    /**
     * 消息列表
     *
     * @param userId 用户id
     */
    public Message messageList(long userId) {

        params.clear();
        params.put(ATTR_USER_ID, userId + "");

        Message message = Message.obtain();
        try {
            List<MessageInfo> messageInfoList = OkhttpUtil.execute(params, HttpUrlConstancts.MESSAGE_LIST,
                    MessageInfo.class);
            message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = messageInfoList;
        } catch (Exception e) {
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }

        return message;
    }

    /**
     * 根据消息ID获取消息
     *
     * @param msgId 消息ID
     */
    public Message getMessageById(long userId, int msgId) {

        params.clear();
        params.put(ATTR_USER_ID, userId + "");
        params.put(ATTR_MESSAGE_ID,msgId + "");

        Message message = Message.obtain();
        try {
            MessageInfo messageInfo = OkhttpUtil.execute(HttpUrlConstancts.MESSAGE_READ,params,
                    MessageInfo.class);
            message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = messageInfo;
        } catch (Exception e) {
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
            message.obj = e.getMessage();
        }

        return message;
    }
}
