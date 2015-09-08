package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.TestActivity;
import com.wisape.android.content.ActiveBroadcastReciver;
import com.wisape.android.content.BroadCastReciverListener;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.OnRecycleViewClickListener;
import com.wisape.android.widget.PopupWindowMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment implements BroadCastReciverListener{

    private static final String TAG = CardGalleryFragment.class.getSimpleName();

    private static final int LOADER_STORY = 1;

    private static final String EXTRAS_ACCESS_TOKEN = "access_token";

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.gift_count)
    TextView mTextGifCount;

    private PopupWindowMenu popupWindow;
    private GalleryAdapter mGalleryAdapter;
    private ActiveBroadcastReciver activeBroadcastReciver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_gallery, container, false);
        ButterKnife.inject(this, rootView);
        initView();
        setReciver();
        return rootView;
    }

    private void setReciver(){
        activeBroadcastReciver = new ActiveBroadcastReciver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wisape.android.content.ActiveBroadcastReciver");
        getActivity().registerReceiver(activeBroadcastReciver, intentFilter);
    }

    private void initView() {
        popupWindow = new PopupWindowMenu((BaseActivity)getActivity());
        mCardGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        mGalleryAdapter = new GalleryAdapter(getActivity());
        mGalleryAdapter.setOnRecycleViewClickListener(new OnRecycleViewClickListener() {
            @Override
            public void onItemClick(long storyId) {
                // TODO: 15/8/26 调用接口
                Log.e(TAG, "onItemClick:" + storyId);
            }

            @Override
            public void onItemSubViewClick(long storyId) {
                Log.e(TAG, "onItemSubViewClick:" + storyId);
                showPopupWindow(storyId);

            }
        });
        mCardGallery.setAdapter(mGalleryAdapter);

        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_ACCESS_TOKEN,wisapeApplication.getUserInfo().access_token);
        startLoadWithProgress(LOADER_STORY, bundle);
    }

    @Override
    public Message loadingInbackground(int what, Bundle args) {
        Message msg = StoryLogic.instance().getUserStory(args.getString(EXTRAS_ACCESS_TOKEN));
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadComplete(Message data) {
        super.onLoadComplete(data);
        if(HttpUrlConstancts.STATUS_SUCCESS == data.arg1){
            mGalleryAdapter.setData((List<StoryInfo>)data.obj);
        }else {
            showToast((String) data.obj);
        }
    }

    private void showPopupWindow(long storyid){
        if(!popupWindow.isShowing()){
            PopupWindowMenu.setStoryId(storyid);
            popupWindow.showAtLocation(getView(), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL,0,0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        popupWindow.dismiss();
        activeBroadcastReciver.destroy();
        getActivity().unregisterReceiver(activeBroadcastReciver);
    }

    @OnClick(R.id.add_story)
    @SuppressWarnings("unused")
    protected void doAddStory() {
        TestActivity.launch(getActivity(), 0);
    }

    @OnClick(R.id.gift)
    @SuppressWarnings("unused")
    public void showGift() {
        clearMsgCount();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        trans.add(R.id.drawer_main, new GiftFragment());
        trans.commit();
    }

    /**
     * 清除活动数字
     */
    private void clearMsgCount() {
        mTextGifCount.setText("0");
        mTextGifCount.setVisibility(View.GONE);
    }

    @OnClick(R.id.menu_switch)
    @SuppressWarnings("unused")
    public void onClickMenuSwitch(View view) {
        Activity activity = getActivity();
        if (null == activity) return;
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).openOrCloseMainMenu();
        }
    }

    /**
     * story列表适配器
     */
    public class GalleryAdapter extends RecyclerView.Adapter<GHolder> {

        private Context mContext;
        private List<StoryInfo> storyEntityList = new ArrayList<>();
        private OnRecycleViewClickListener onRecycleViewClickListener;

        public GalleryAdapter(Context c) {
            this.mContext = c;
        }

        public void setOnRecycleViewClickListener(OnRecycleViewClickListener onRecycleViewClickListener) {
            this.onRecycleViewClickListener = onRecycleViewClickListener;
        }

        @Override
        public GHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_view_card_item,
                    parent, false);
            return new GHolder(view);
        }

        @Override
        public void onBindViewHolder(GHolder holder, final int position) {
            final StoryInfo storyEntity = storyEntityList.get(position);

            holder.mTextEyecount.setText(storyEntity.view_num + "");
            holder.mTextStoryState.setText(storyEntity.status + "");
            holder.mTextZanCount.setText(storyEntity.like_num + "");
            holder.mTextShareCount.setText(storyEntity.share_num + "");
            holder.mTextStoryName.setText(storyEntity.story_name);
            Picasso.with(getActivity()).load(storyEntity.small_img)
                    .into(holder.mStoryBg);

            //设置事件监听
            holder.mStoryBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecycleViewClickListener.onItemClick(storyEntity.id);
                }
            });

            holder.imageShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecycleViewClickListener.onItemSubViewClick(storyEntity.id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return storyEntityList.size();
        }

        public void setData(List<StoryInfo> storyEntities) {
            storyEntityList = storyEntities;
            notifyDataSetChanged();
        }
    }

    public class GHolder extends RecyclerView.ViewHolder{
        @InjectView(R.id.main_story_bg)
        ImageView mStoryBg;
        @InjectView(R.id.text_story_state)
        TextView mTextStoryState;
        @InjectView(R.id.text_eye_count)
        TextView mTextEyecount;
        @InjectView(R.id.text_zan_count)
        TextView mTextZanCount;
        @InjectView(R.id.text_share_count)
        TextView mTextShareCount;
        @InjectView(R.id.story_name)
        TextView mTextStoryName;
        @InjectView(R.id.main_story_more)
        ImageView imageShare;

        public GHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }


    @Override
    public void updateMsgCount() {
        if (mTextGifCount.getVisibility() == View.GONE) {
            mTextGifCount.setVisibility(View.VISIBLE);
        }
        mTextGifCount.setText(Integer.parseInt(mTextGifCount.getText().toString()) + 1 + "");
    }
}
