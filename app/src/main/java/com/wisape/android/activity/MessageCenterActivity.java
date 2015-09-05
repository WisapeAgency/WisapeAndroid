package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.common.OnRecycleViewClickListener;
import com.wisape.android.logic.MessageCenterLogic;
import com.wisape.android.model.MessageInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 消息列表
 * Created by hm on 2015/8/15.
 */
public class MessageCenterActivity extends BaseActivity {

    private static final int SYSTEM_MESSAGE = 2;

    private static final int LOADER_MESSAGE_LIST = 1;

    private static final String EXTRAS_USER_ID = "user_id";

    @InjectView(R.id.message_list)
    RecyclerView messageListRecyclerView;

    @InjectView(R.id.imge_no_data)
    ImageView imgNoData;

    private MessageListAdapter messageListAdapter;

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
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        messageListRecyclerView.setLayoutManager(linearLayoutManager);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5f5f5")));
        }


        Bundle args = new Bundle();
        args.putLong(EXTRAS_USER_ID, wisapeApplication.getUserInfo().user_id);
        startLoad(LOADER_MESSAGE_LIST, args);

    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = MessageCenterLogic.getInstance().messageList(args.getLong(EXTRAS_USER_ID));
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if(STATUS_SUCCESS == data.arg1){
            List<MessageInfo> messageInfoList = (List<MessageInfo>)data.obj;
            if(null == messageInfoList || messageInfoList.size() == 0){
                imgNoData.setVisibility(View.VISIBLE);
                messageListRecyclerView.setVisibility(View.GONE);
            }else {
                messageListAdapter = new MessageListAdapter(messageInfoList);
                messageListAdapter.setOnRecycleViewClickListener(new OnRecycleViewClickListener() {
                    @Override
                    public void onItemClick(long storyId) {
                        MessageCenterDetailActivity.launch(getApplicationContext(),(int)storyId);
                    }

                    @Override
                    public void onItemSubViewClick(long storyId) {

                    }
                });
                messageListRecyclerView.setAdapter(messageListAdapter);
            }
        }else{
            showToast((String)data.obj);
            imgNoData.setVisibility(View.VISIBLE);
            messageListRecyclerView.setVisibility(View.GONE);
        }
    }

    public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageListHolder> {

        private List<MessageInfo> messageInfoList = new ArrayList<>();
        private OnRecycleViewClickListener onRecycleViewClickListener;

        public MessageListAdapter(List<MessageInfo> messageInfos){
            messageInfoList = messageInfos;
        }

        public void setOnRecycleViewClickListener(OnRecycleViewClickListener onRecycleViewClickListener) {
            this.onRecycleViewClickListener = onRecycleViewClickListener;
        }

        @Override
        public int getItemCount() {
            return messageInfoList.size();
        }

        @Override
        public MessageListHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_list,
                    parent, false);
            return new MessageListHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageListHolder holder, int position) {
            final MessageInfo messageInfo = messageInfoList.get(position);
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecycleViewClickListener.onItemClick(messageInfo.getId());
                }
            });
        }

        public class MessageListHolder extends RecyclerView.ViewHolder{

            @InjectView(R.id.img_message_list_type)
            ImageView messageType;
            @InjectView(R.id.tv_message_list_title)
            TextView messageTitle;
            @InjectView(R.id.tv_message_list_time)
            TextView messageTime;
            @InjectView(R.id.tv_message_list_content)
            TextView messageContent;

            View itemView;

            public MessageListHolder(View view) {
                super(view);
                ButterKnife.inject(this, view);
                itemView = view;
            }
        }
    }
}
