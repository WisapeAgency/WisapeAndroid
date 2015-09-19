package com.wisape.android.fragment;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
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
import com.wisape.android.activity.StoryReleaseActivity;
import com.wisape.android.activity.StorySettingsActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.activity.TestActivity;
import com.wisape.android.content.ActiveBroadcastReciver;
import com.wisape.android.content.BroadCastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.PopupWindowMenu;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * my story界面
 *
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment implements BroadCastReciverListener,
        PopupWindowMenu.OnPuupWindowItemClickListener {

    private static final int LOADER_STORY = 1;
    private static final int LOADER_DELETE_STORY = 2;
    private static final int LOADER_CREATE_STORY = 3;
    private static final int LOADER_PUBLISH_STORY = 4;

    private static final String EXTRAS_ACCESS_TOKEN = "access_token";
    private static final String EXTRAS_STORY_ENTITY = "story_entity";
    private static final String EXRAS_IS_SERVER = "is_server";

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.gift_count)
    TextView mTextGifCount;

    private PopupWindowMenu popupWindow;
    private ActiveBroadcastReciver activeBroadcastReciver;
    private StoryEntity clickStoryEntity;
    private int clickPosition;
    private GalleryAdapter mGalleryAdapter;
    private List<StoryEntity> storyEntityList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_gallery, container, false);
        ButterKnife.inject(this, rootView);
        initView();
        setReciver();
        return rootView;
    }

    private void setReciver() {
        activeBroadcastReciver = new ActiveBroadcastReciver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.wisape.android.content.ActiveBroadcastReciver");
        getActivity().registerReceiver(activeBroadcastReciver, intentFilter);
    }

    private void initView() {
        popupWindow = new PopupWindowMenu((BaseActivity) getActivity(), this);
        mCardGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_ACCESS_TOKEN, wisapeApplication.getUserInfo().access_token);
        startLoadWithProgress(LOADER_STORY, bundle);
    }

    @Override
    public Message loadingInbackground(int what, Bundle args) {
        Message message = Message.obtain();
        switch (what) {
            case LOADER_STORY:
                message = StoryLogic.instance().getUserStory(args.getString(EXTRAS_ACCESS_TOKEN));
                break;
            case LOADER_DELETE_STORY:
                StoryEntity storyEntity = args.getParcelable(EXTRAS_STORY_ENTITY);
                message = StoryLogic.instance().deleteStory(getActivity(), storyEntity
                        , args.getString(EXTRAS_ACCESS_TOKEN), args.getBoolean(EXRAS_IS_SERVER));
                break;
            case LOADER_CREATE_STORY:
                message.obj = StoryLogic.instance().createStory(getActivity());
                break;
            case LOADER_PUBLISH_STORY:
                break;
        }
        message.what = what;
        return message;
    }

    @Override
    protected void onLoadComplete(Message data) {
        super.onLoadComplete(data);
        switch (data.what) {
            case LOADER_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    storyEntityList = (List<StoryEntity>) data.obj;
                    if (0 == storyEntityList.size()) {
                        showToast("no story");
                    } else {
                        mGalleryAdapter = new GalleryAdapter();
                        mCardGallery.setAdapter(mGalleryAdapter);
                    }
                } else {
                    showToast("get story error");
                }
                break;
            case LOADER_DELETE_STORY:
                if (data.arg1 == HttpUrlConstancts.STATUS_SUCCESS) {
                    deleteData();
                } else {
                    showToast("删除story失败");
                }
                break;
            case LOADER_CREATE_STORY:
                StoryEntity storyEntity = (StoryEntity) data.obj;
                if (null == storyEntity) {
                    showToast("本地数据库创建失败,请重新创建");
                } else {
                    wisapeApplication.setStoryEntity(storyEntity);
                    addStoryData(storyEntity);
//                    StorySettingsActivity.launch(getActivity(), StorySettingsActivity.REQUEST_SETTING);
                }
                break;
            case LOADER_PUBLISH_STORY:
                StoryEntity entity = StoryEntity.transform((StoryInfo)data.obj);


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

        StoryTemplateActivity.launch(this, 0);


//        startLoad(LOADER_CREATE_STORY, null);
//        TestActivity.launch(getActivity(), 0);
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

    @Override
    public void updateMsgCount() {
        if (mTextGifCount.getVisibility() == View.GONE) {
            mTextGifCount.setVisibility(View.VISIBLE);
        }
        mTextGifCount.setText(Integer.parseInt(mTextGifCount.getText().toString()) + 1 + "");
    }

    @Override
    public void onEditClick() {
        //TODO 跳转至再次编辑界面
    }

    @Override
    public void onPrevidewClick() {
        //TODO 跳转至预览界面
    }

    @Override
    public void onPublishClick() {
        if ("0".equals(clickStoryEntity.status)) {
            startLoad(LOADER_PUBLISH_STORY, null);
        } else {
            StoryReleaseActivity.launch(getActivity(), StoryEntity.convert(clickStoryEntity));
        }
    }

    @Override
    public void onDeleteClick() {
        boolean isSever = true;
        /*如果是草稿story只进行本地删除*/
        if ("0".equals(clickStoryEntity.status)) {
            isSever = false;
        }

        Bundle args = new Bundle();
        args.putParcelable(EXTRAS_STORY_ENTITY, clickStoryEntity);
        args.putString(EXTRAS_ACCESS_TOKEN, wisapeApplication.getUserInfo().access_token);
        args.putBoolean(EXRAS_IS_SERVER, isSever);
        startLoad(LOADER_DELETE_STORY, args);
    }

    /*删除story*/
    private void deleteData() {
        storyEntityList.remove(clickPosition);
        mGalleryAdapter.notifyItemRemoved(clickPosition);
        mGalleryAdapter.notifyDataSetChanged();
    }

    /*修改story信息*/
    private void updateStoryData(StoryEntity storyEntity) {
        storyEntityList.add(clickPosition, storyEntity);
        mGalleryAdapter.notifyItemChanged(clickPosition);
        mGalleryAdapter.notifyDataSetChanged();
    }

    /*新增story*/
    private void addStoryData(StoryEntity storyEntity) {
        int size = storyEntityList.size();
        storyEntityList.add(size, storyEntity);
        mGalleryAdapter.notifyItemInserted(size);
        mGalleryAdapter.notifyDataSetChanged();
    }

    /**
     * story列表适配器
     */
    public class GalleryAdapter extends RecyclerView.Adapter<GHolder> {

        @Override
        public GHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_view_card_item,
                    parent, false);
            return new GHolder(view);
        }

        @Override
        public void onBindViewHolder(GHolder holder, final int position) {
            final StoryEntity storyEntity = storyEntityList.get(position);

            holder.mTextEyecount.setText(storyEntity.viewNum + "");
            holder.mTextZanCount.setText(storyEntity.likeNum + "");
            holder.mTextShareCount.setText(storyEntity.shareNum + "");
            holder.mTextStoryName.setText(storyEntity.storyName);

            if (0 == position) {
                holder.mTextStoryState.setText("默认");
            } else {
                if ("0".equals(storyEntity.status)) {
                    holder.mTextStoryState.setText("草稿");
                } else {
                    holder.mTextStoryState.setText("已经发布");
                }
            }
            Picasso.with(getActivity())
                    .load(storyEntity.storyThumbUri)
                    .into(holder.mStoryBg);

            holder.imageShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickPosition = position;
                    clickStoryEntity = storyEntity;
                    if (!popupWindow.isShowing()) {
                        popupWindow.showAtLocation(getView(),
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return storyEntityList.size();
        }
    }

    public class GHolder extends RecyclerView.ViewHolder {
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
}
