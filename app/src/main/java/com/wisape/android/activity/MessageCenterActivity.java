package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wisape.android.R;
import com.wisape.android.http.DefaultHttpRequestListener;
import com.wisape.android.http.HttpRequest;
import com.wisape.android.model.MessageInfo;
import com.wisape.android.network.WWWConfig;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 消息列表
 * Created by hm on 2015/8/15.
 */
public class MessageCenterActivity extends BaseActivity {

    private static final int SYSTEM_MESSAGE = 2;
    private static final int OPERATE_MESSAGE = 1;

    private static final String TAG = MessageCenterActivity.class.getSimpleName();

    @InjectView(R.id.message_list)
    RecyclerView messageListRecyclerView;

    @InjectView(R.id.imge_no_data)
    ImageView imgNoData;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), MessageCenterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center);
        ButterKnife.inject(this);
        initView();
        getData();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageListRecyclerView.setLayoutManager(linearLayoutManager);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5f5f5")));
    }

    private void getData() {
        String url = WWWConfig.acquireUri(getString(R.string.uri_message_list))+"?uid=" + wisapeApplication.getUserInfo().user_id;

        HttpRequest.addRequest(url, this, new DefaultHttpRequestListener() {
            @Override
            public void onReqeustSuccess(String data) {
                closeProgressDialog();
                List<MessageInfo> messageInfos = JSONObject.parseArray(data, MessageInfo.class);
                if (messageInfos == null || messageInfos.size() == 0) {
                    imgNoData.setVisibility(View.VISIBLE);
                    messageListRecyclerView.setVisibility(View.GONE);
                } else {
                    messageListRecyclerView.setAdapter(new MessageListAdapter(messageInfos));
                }
            }

            @Override
            public void onError(String message) {
                closeProgressDialog();
                showToast("加载数据出错");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        HttpRequest.cancleRequest(this);
    }

    public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListHolder> {

        private List<MessageInfo> messageInfoList;

        public MessageListAdapter(List<MessageInfo> messageInfos){
            messageInfoList = messageInfos;
        }

        @Override
        public int getItemCount() {
            return messageInfoList.size();
        }

        @Override
        public MessageListHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list,
                    parent, false);
            MessageListHolder holder = new MessageListHolder(view, new HoderClickListener() {
                @Override
                public void onItemClick(int positon) {
                    Log.e(TAG, "#onCreateHolder:" + viewType);
                    MessageCenterDetailActivity.launch(getApplicationContext(),messageInfoList.get(positon).getId());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(MessageListHolder holder, int position) {
            MessageInfo messageInfo = messageInfoList.get(position);
            if (SYSTEM_MESSAGE == messageInfo.getType()) {
                holder.messageType.setImageResource(R.mipmap.icon_operation);
            } else {
                holder.messageType.setImageResource(R.mipmap.icon_system);
            }
            holder.messageTitle.setText(messageInfo.getTitle());
            holder.messageTime.setText(messageInfo.getParsetime());
            String subject = messageInfo.getSubject();
            if (null == subject || "".equals(subject)) {
                holder.messageContent.setText("没有简介");
            }else{
                holder.messageContent.setText(messageInfo.getSubject());
            }
        }

        public class MessageListHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            @InjectView(R.id.img_message_list_type)
            ImageView messageType;
            @InjectView(R.id.tv_message_list_title)
            TextView messageTitle;
            @InjectView(R.id.tv_message_list_time)
            TextView messageTime;
            @InjectView(R.id.tv_message_list_content)
            TextView messageContent;

            HoderClickListener hoderClickListener;



            public MessageListHolder(View view,HoderClickListener hoderClickListener) {
                super(view);
                ButterKnife.inject(this, view);
                this.hoderClickListener = hoderClickListener;
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                hoderClickListener.onItemClick(getPosition());
            }
        }

    }

    interface HoderClickListener{
        void onItemClick(int postion);
    }
}
