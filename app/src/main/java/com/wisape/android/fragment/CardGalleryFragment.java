package com.wisape.android.fragment;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.StoryPreviewActivity;
import com.wisape.android.activity.StoryReleaseActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.ComfirmDialog;
import com.wisape.android.widget.PopupWindowMenu;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * my story界面
 *
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment implements PopupWindowMenu.OnPuupWindowItemClickListener
        , StoryBroadcastReciver.StoryBroadcastReciverListener {

    private static final String WISAPE_SD_CARD_LOCATION = "/WISAPE_SD_CARD_LOCATION/";

    private static final int LOADER_STORY = 1;
    private static final int LOADER_DELETE_STORY = 2;
    private static final int LOADER_PREVIEW_STORY = 3;
    private static final int LOADER_EDIT_STORY = 5;
    private static final int LOADER_GET_STORY_LOCAL = 6;

    private static final String EXTRAS_ACCESS_TOKEN = "access_token";
    private static final String EXTRAS_STORY_ENTITY = "story_entity";
    private static final String EXRAS_IS_SERVER = "is_server";

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;

    private PopupWindowMenu popupWindow;
    private StoryBroadcastReciver storyBroadcastReciver;
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
        storyBroadcastReciver = new StoryBroadcastReciver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(StoryBroadcastReciver.STORY_ACTION);
        getActivity().registerReceiver(storyBroadcastReciver, filter);
    }

    private void initView() {
        popupWindow = new PopupWindowMenu((BaseActivity) getActivity(), this);
        mCardGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26, mDisplayMetrics));
        Bundle bundle = new Bundle();
        bundle.putString(EXTRAS_ACCESS_TOKEN, UserLogic.instance().getUserInfoFromLocal().access_token);
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
            case LOADER_PREVIEW_STORY:
                File file = new File(StoryManager.getStoryDirectory(), StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/story.html");
                File previewFile = new File(StoryManager.getStoryDirectory(), StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/preview.html");
                if (previewFile.exists()) {
                    previewFile.delete();
                }
                if (FileUtils.saveStoryPreviewFile(previewFile, FileUtils.readFileToString(file))) {
                    message.obj = previewFile.getAbsolutePath();
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                }
                break;
            case LOADER_EDIT_STORY:
                File editFile = new File(StoryManager.getStoryDirectory(), StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/story.html");
                String html = FileUtils.readFileToString(editFile);
                File dataFile = EnvironmentUtils.getAppDataDirectory();
                html = html.replace(WISAPE_SD_CARD_LOCATION, dataFile.getAbsolutePath());
                if (Utils.isEmpty(html)) {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                    message.obj = "Get StoryInfo Failure";
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                    message.obj = html;
                }
                break;
            case LOADER_GET_STORY_LOCAL:
                List<StoryEntity> storyEntities = StoryLogic.instance().getUserStoryFromLocal(getActivity());
                if (null != storyEntities) {
                    message.obj = storyEntities;
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                }
                break;
        }
        message.what = what;
        return message;
    }

    @Override
    protected void onLoadComplete(Message data) {
        closeProgressDialog();
        super.onLoadComplete(data);
        switch (data.what) {
            case LOADER_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    storyEntityList = (List<StoryEntity>) data.obj;
                    if (0 == storyEntityList.size()) {
                        showToast("No Story");
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
                    showToast("detele story success");
                } else {
                    showToast("Delete Story Failure");
                }
                break;
            case LOADER_PREVIEW_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    StoryPreviewActivity.launch(getActivity(), (String) data.obj);

                } else {
                    showToast("Get StoryInfo Failure");
                }
                break;

            case LOADER_EDIT_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    StoryTemplateActivity.launch(getActivity(), (String) data.obj, 0);
                } else {
                    showToast("story downing");
                }
                break;
            case LOADER_GET_STORY_LOCAL:

                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    storyEntityList = (List<StoryEntity>) data.obj;
                    if (0 == storyEntityList.size()) {
                        showToast("No Story");
                    } else {
                        mGalleryAdapter = new GalleryAdapter();
                        mCardGallery.setAdapter(mGalleryAdapter);
                        mCardGallery.scrollToPosition(mGalleryAdapter.getItemPostion(StoryLogic.instance().getStoryEntityFromShare()));
                    }
                } else {
                    showToast("get story error");
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        popupWindow.dismiss();
        storyBroadcastReciver.destory();
        getActivity().unregisterReceiver(storyBroadcastReciver);
    }

    @OnClick(R.id.add_story)
    @SuppressWarnings("unused")
    protected void doAddStory() {
        StoryLogic.instance().clear();
        StoryTemplateActivity.launch(this, 0);
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
    public void storyStateChange(int type) {
        LogUtil.d("重新加载首页数据");
        startLoad(LOADER_GET_STORY_LOCAL, null);
    }

    @Override
    public void onEditClick() {
        startLoadWithProgress(LOADER_EDIT_STORY, null);
    }

    @Override
    public void onPrevidewClick() {
        startLoadWithProgress(LOADER_PREVIEW_STORY, null);
    }

    @Override
    public void onPublishClick() {
        StoryReleaseActivity.launch(getActivity());
    }

    @Override
    public void onDeleteClick() {

        final ComfirmDialog comfirmDialog = ComfirmDialog.getInstance(getString(R.string.delete_story_title)
                , getString(R.string.delete_story_content));
        comfirmDialog.show(getFragmentManager(), "clear");
        comfirmDialog.setOnConfirmClickListener(new ComfirmDialog.OnComfirmClickListener() {
            @Override
            public void onConfirmClicked() {
                comfirmDialog.dismiss();
                boolean isSever = false;
        /*如果是草稿story只进行本地删除*/
                if (ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE.equals(StoryLogic.instance().getStoryEntityFromShare().status)) {
                    isSever = true;
                }
                Bundle args = new Bundle();
                args.putParcelable(EXTRAS_STORY_ENTITY, StoryLogic.instance().getStoryEntityFromShare());
                args.putString(EXTRAS_ACCESS_TOKEN, UserLogic.instance().getUserInfoFromLocal().access_token);
                args.putBoolean(EXRAS_IS_SERVER, isSever);
                startLoad(LOADER_DELETE_STORY, args);
            }
        });
    }

    /*删除列表中的story*/
    private void deleteData() {
        StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
        int postion = 0;
        int size = storyEntityList.size();
        for (int i = 0; i < size; i++) {
            StoryEntity entity = storyEntityList.get(i);
            if (entity.id == storyEntity.id) {
                postion = i;
                break;
            }
        }
        storyEntityList.remove(postion);
        mGalleryAdapter.notifyItemRemoved(postion);
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
            LinearLayout.LayoutParams params;
            float screenWidth = mDisplayMetrics.widthPixels;
            float screenHeight = mDisplayMetrics.heightPixels;

            float bili = screenWidth / screenHeight;
            if (bili * 10 <= 6) {
                int width = (int) (mDisplayMetrics.widthPixels * 0.7);
                params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);

            } else {
                int width = (int) (mDisplayMetrics.widthPixels * 0.78);
                params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
            }

            holder.linearLayout.setLayoutParams(params);
            holder.mTextEyecount.setText(storyEntity.viewNum + "");
            holder.mTextZanCount.setText(storyEntity.likeNum + "");
            holder.mTextStoryName.setText(storyEntity.storyName);

            if (ApiStory.AttrStoryInfo.STORY_DEFAULT.equals(storyEntity.status)) {
                holder.mTextStoryState.setText("default");
            }
            if (ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY.equals(storyEntity.status)) {
                holder.mTextStoryState.setText("draft");
            }
            if (ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE.equals(storyEntity.status)) {
                holder.mTextStoryState.setText("publish");
            }

            if (storyEntity.localCover == 0) {
                File storyDirectory = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal);
                File coverFile = new File(storyDirectory, "thumb.jpg");

                StoryEntity cureent = StoryLogic.instance().getStoryEntityFromShare();
                if (cureent != null && cureent.id == storyEntity.id) {
                    Glide.with(getActivity()).load(coverFile)
                            .placeholder(R.mipmap.loading)
                            .error(R.mipmap.loading)
                            .centerCrop()
                            .signature(new StringSignature(Utils.acquireUTCTimestamp()))
                            .crossFade()
                            .into(holder.mStoryBg);
                } else {
                    Glide.with(getActivity()).load(coverFile)
                            .placeholder(R.mipmap.loading)
                            .error(R.mipmap.loading)
                            .centerCrop()
                            .crossFade()
                            .into(holder.mStoryBg);
                }


            } else {
                File storyDirectory = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal);
                File coverFile = new File(storyDirectory, "thumb.jpg");

                StoryEntity cureent = StoryLogic.instance().getStoryEntityFromShare();
                if (cureent != null && cureent.id == storyEntity.id) {
                    Glide.with(getActivity()).load(coverFile)
                            .placeholder(R.mipmap.loading)
                            .error(R.mipmap.loading)
                            .centerCrop()
                            .signature(new StringSignature(Utils.acquireUTCTimestamp()))
                            .crossFade()
                            .into(holder.mStoryBg);
                } else {
                    Glide.with(getActivity()).load(coverFile)
                            .placeholder(R.mipmap.loading)
                            .error(R.mipmap.loading)
                            .centerCrop()
                            .crossFade()
                            .into(holder.mStoryBg);
                }
            }

            holder.imageShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StoryLogic.instance().saveStoryEntityToShare(storyEntity);
                    if (!popupWindow.isShowing()) {
                        popupWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                }
            });
            holder.mStoryBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StoryLogic.instance().saveStoryEntityToShare(storyEntity);
                    startLoadWithProgress(LOADER_PREVIEW_STORY, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return storyEntityList.size();
        }

        public int getItemPostion(StoryEntity storyEntity) {
            if (storyEntityList != null && storyEntityList.size() > 0) {
                int size = storyEntityList.size();
                for (int i = 0; i < size; i++) {
                    StoryEntity story = storyEntityList.get(i);
                    if (story != null && storyEntity != null) {
                        if (story.id == storyEntity.id) {
                            return i;
                        }
                    }
                }
            }
            return 0;
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
        @InjectView(R.id.story_name)
        TextView mTextStoryName;
        @InjectView(R.id.main_story_more)
        ImageView imageShare;
        @InjectView(R.id.liner_item)
        LinearLayout linearLayout;

        public GHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}