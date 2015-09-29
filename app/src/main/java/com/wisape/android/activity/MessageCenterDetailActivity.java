package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.logic.MessageCenterLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.MessageInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 单条消息详细信息
 * Created by hm on 2015/8/15.
 */
public class MessageCenterDetailActivity extends BaseActivity {

    public static final String MESSAGE_ID = "message_id";

    private static final int LOADER_MESSAGE_READ = 1;

    private static final String EXTRAS_MESAGE_ID = "mid";
    private static final String EXTRAS_USER_ID = "user_id";

    @InjectView(R.id.message_center_message_detail_title)
    TextView messageTietle;
    @InjectView(R.id.message_center_message_detail_time)
    TextView messageTime;
    @InjectView(R.id.message_center_message_detail_content)
    TextView messageContent;

    public static void launch(Context context, int messageId) {
        Intent intent = new Intent(new Intent(context, MessageCenterDetailActivity.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MESSAGE_ID, messageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_center_detail);
        ButterKnife.inject(this);

        Bundle args = new Bundle();
        args.putInt(EXTRAS_MESAGE_ID,getIntent().getExtras().getInt(MESSAGE_ID));
        args.putLong(EXTRAS_USER_ID, UserLogic.instance().getUserInfoFromLocal().user_id);
        startLoadWithProgress(LOADER_MESSAGE_READ, args);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = MessageCenterLogic.getInstance().getMessageById(args.getLong(EXTRAS_USER_ID),
                args.getInt(EXTRAS_MESAGE_ID));
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        super.onLoadCompleted(data);
        if(STATUS_SUCCESS == data.arg1){
            MessageInfo messageInfo = (MessageInfo)data.obj;
            if (null != messageInfo){
                messageTietle.setText(messageInfo.getTitle());
                messageTime.setText(messageInfo.getParsetime());
                messageContent.setText(messageInfo.getUser_message());
            }else{
                messageTietle.setText("获取消息失败!");
            }
        }else{
            showToast((String)data.obj);
        }
    }
}
