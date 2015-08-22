package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.wisape.android.R;
import com.wisape.android.http.DefaultHttpRequestListener;
import com.wisape.android.http.HttpRequest;
import com.wisape.android.model.MessageInfo;
import com.wisape.android.network.WWWConfig;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 单条消息详细信息
 * Created by hm on 2015/8/15.
 */
public class MessageCenterDetailActivity extends BaseActivity {

    public static final String MESSAGE_ID = "message_id";

    private static final String TAG = MessageCenterDetailActivity.class.getSimpleName();

    @InjectView(R.id.message_center_message_detail_title)
    TextView messageTietle;
    @InjectView(R.id.message_center_message_detail_time)
    TextView messageTime;
    @InjectView(R.id.message_center_message_detail_content)
    TextView messageContent;

    public static void launch(Context context,int messageId){
        Intent intent = new Intent(new Intent(context, MessageCenterDetailActivity.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MESSAGE_ID,messageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center_detail);
        ButterKnife.inject(this);

        getData();
    }

    private void getData(){
        final int messageId = getIntent().getIntExtra(MESSAGE_ID,0);
        Map<String,String> params = new HashMap<>();
        params.put(MESSAGE_ID, messageId + "");
        String url = WWWConfig.acquireUri(getString(R.string.uri_message_read))+"?uid="+
                wisapeApplication.getUserInfo().user_id+"&mid="+getIntent().getIntExtra(MESSAGE_ID,-1);
        HttpRequest.addRequest(url,this,new DefaultHttpRequestListener(){
            @Override
            public void onReqeustSuccess(String data) {
                Log.e(TAG,"#getData:" + data);
                if(null == data || "".equals(data)){
                    messageTietle.setText("获取消息失败!");
                }
                MessageInfo messageInfo = JSONObject.parseObject(data,MessageInfo.class);
                messageTietle.setText(messageInfo.getTitle());
                messageTime.setText(messageInfo.getParsetime());
                String massage = messageInfo.getUser_message();
                if(null == massage || "".equals(massage)){
                    messageContent.setText("没有消息内容");
                }else{
                    messageContent.setText(messageInfo.getUser_message());
                }
            }

            @Override
            public void onError(String message) {
                super.onError(message);
                closeProgressDialog();
                showToast("数据加载出错!");
            }
        });
    }
}
