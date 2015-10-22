package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.StoryPreviewActivity;
import com.wisape.android.activity.StoryReleaseActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.ActiveBroadcastReciver;
import com.wisape.android.content.BroadCastReciverListener;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.content.StoryBroadcastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.util.FileUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.PopupWindowMenu;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
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
        PopupWindowMenu.OnPuupWindowItemClickListener, StoryBroadcastReciverListener {

    private static final String PREVIEW_HEADER = "www/views/header.html";
    private static final String PREVIEW_FOOTER = "www/views/footer.html";

    private static final int LOADER_STORY = 1;
    private static final int LOADER_DELETE_STORY = 2;
    private static final int LOADER_PREVIEW_STORY = 3;
    private static final int LOADER_PUBLISH_STORY = 4;
    private static final int LOADER_EDIT_STORY = 5;

    private static final String EXTRAS_ACCESS_TOKEN = "access_token";
    private static final String EXTRAS_STORY_ENTITY = "story_entity";
    private static final String EXRAS_IS_SERVER = "is_server";

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.gift_count)
    TextView mTextGifCount;

    private PopupWindowMenu popupWindow;
    private ActiveBroadcastReciver activeBroadcastReciver;
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
        activeBroadcastReciver = new ActiveBroadcastReciver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActiveBroadcastReciver.ACTIVE_ACTION);
        getActivity().registerReceiver(activeBroadcastReciver, intentFilter);

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
                LogUtil.d("首页预览story路径故事:" + file.getAbsolutePath());
                File previewFile = new File(StoryManager.getStoryDirectory(), StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/preview.html");
                LogUtil.d("首页预览story路径:" + previewFile.getAbsolutePath());
                if (previewFile.exists()) {
                    previewFile.delete();
                }
                if (saveStoryPreview(previewFile, FileUtils.readFileToString(file), StoryLogic.instance().getStoryEntityFromShare())) {
                    message.obj = previewFile.getAbsolutePath();
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                }
                break;
            case LOADER_EDIT_STORY:
                File editFile = new File(StoryManager.getStoryDirectory(), StoryLogic.instance().getStoryEntityFromShare().storyLocal + "/story.html");
                String data = FileUtils.readFileToString(editFile);
                LogUtil.d("首页编辑story:" + editFile.getAbsolutePath() + ":" + data);
                if (Utils.isEmpty(data)) {
                    message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
                    message.obj = "Get StoryInfo Failure";
                } else {
                    message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
                    message.obj = data;
                }
                break;
            case LOADER_PUBLISH_STORY:
                StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();

                ApiStory.AttrStoryInfo storyAttr = new ApiStory.AttrStoryInfo();
                storyAttr.attrStoryThumb = Uri.parse((new File(StoryManager.getStoryDirectory(), story.storyLocal + "/thumb.jpg")).getAbsolutePath());
                storyAttr.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
                storyAttr.story = Uri.fromFile(new File(StoryManager.getStoryDirectory(), story.storyLocal));
                storyAttr.storyName = story.storyName;
                storyAttr.bgMusic = story.storyMusicName;
                storyAttr.storyDescription = story.storyDesc;
                storyAttr.imgPrefix = StoryManager.getStoryDirectory().getAbsolutePath() + "/" + story.storyLocal;
                storyAttr.userId = UserLogic.instance().getUserInfoFromLocal().user_id;
                if ("-1".equals(story.status)) {
                    storyAttr.sid = -1;
                } else {
                    storyAttr.sid = story.storyServerId;
                }

                StoryLogic.instance().update(getActivity().getApplicationContext(),
                        storyAttr, "release");

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
                } else {
                    showToast("Delete Story Failure");
                }
                break;
            case LOADER_PREVIEW_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    StoryPreviewActivity.launch(getActivity(), (String) data.obj);

                } else {
                    showToast("No StoryInfo");
                }
                break;
            case LOADER_PUBLISH_STORY:
                Intent intent = new Intent();
                intent.setAction(StoryBroadcastReciver.STORY_ACTION);
                intent.putExtra(StoryBroadcastReciver.EXTRAS_TYPE, StoryBroadcastReciverListener.TYPE_ADD_STORY);
                getActivity().sendBroadcast(intent);
                StoryReleaseActivity.launch(getActivity());
                break;

            case LOADER_EDIT_STORY:
                if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1) {
                    StoryTemplateActivity.launch(getActivity(), (String) data.obj, 0);
                } else {
                    showToast("No StoryInfo");
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        popupWindow.dismiss();
        activeBroadcastReciver.destroy();
        storyBroadcastReciver.destory();
        getActivity().unregisterReceiver(activeBroadcastReciver);
        getActivity().unregisterReceiver(storyBroadcastReciver);
    }

    @OnClick(R.id.add_story)
    @SuppressWarnings("unused")
    protected void doAddStory() {
        StoryLogic.instance().clear();
        StoryTemplateActivity.launch(this, 0);
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
    public void storyStateChange(int type) {
        if (StoryBroadcastReciverListener.TYPE_ADD_STORY == type) {
            addStoryData();
        }
        if (StoryBroadcastReciverListener.UPDATE_STORY_SETTING == type) {
            updateStorySeeting();
        }
    }

    /**
     * 更新storys设置
     */
    private void updateStorySeeting() {
        StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
        int postion = 0;
        int size = storyEntityList.size();
        if (ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY.equals(storyEntity.status)) {
            for (int i = 0; i < size; i++) {
                StoryEntity entity = storyEntityList.get(i);
                if (entity.id == storyEntity.id) {
                    postion = i;
                    break;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                StoryEntity entity = storyEntityList.get(i);
                if (entity.storyServerId == storyEntity.storyServerId) {
                    postion = i;
                    break;
                }
            }
        }
        storyEntityList.remove(postion);
        storyEntityList.add(postion, storyEntity);
        mGalleryAdapter.notifyItemChanged(postion);
        mGalleryAdapter.notifyDataSetChanged();
    }


    @Override
    public void onEditClick() {
        startLoadWithProgress(LOADER_EDIT_STORY, null);
    }

    @Override
    public void onPrevidewClick() {
        startLoadWithProgress(LOADER_PREVIEW_STORY, null);
    }

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(getActivity().getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = bufReader.readLine()) != null)
                result.append(line);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void onPublishClick() {
        if (ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE.equals(StoryLogic.instance().getStoryEntityFromShare().status)) {
            StoryReleaseActivity.launch(getActivity());
        } else {
            Bundle args = new Bundle();
            args.putParcelable(EXTRAS_STORY_ENTITY, StoryLogic.instance().getStoryEntityFromShare());
            startLoadWithProgress(LOADER_PUBLISH_STORY, args);
        }
    }

    @Override
    public void onDeleteClick() {
        boolean isSever = false;
        /*如果是草稿story只进行本地删除*/
        if (!ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY.equals(StoryLogic.instance().getStoryEntityFromShare().status)) {
            isSever = true;
        }
        Bundle args = new Bundle();
        args.putParcelable(EXTRAS_STORY_ENTITY, StoryLogic.instance().getStoryEntityFromShare());
        args.putString(EXTRAS_ACCESS_TOKEN, UserLogic.instance().getUserInfoFromLocal().access_token);
        args.putBoolean(EXRAS_IS_SERVER, isSever);
        startLoad(LOADER_DELETE_STORY, args);
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

    /*新增story包含更新*/
    private void addStoryData() {
        StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
        if (null == storyEntityList) {
            storyEntityList = new ArrayList<>();
        }
        boolean haveStory = false;
        int postion = 0;
        int size = storyEntityList.size();
        for (int i = 0; i < size; i++) {
            StoryEntity entity = storyEntityList.get(i);
            if (entity.id == storyEntity.id) {
                haveStory = true;
                postion = i;
                break;
            }
        }
        LogUtil.d("更新story封面信息:" + haveStory + postion);
        if (haveStory) {
            storyEntityList.remove(postion);
            storyEntityList.add(postion, storyEntity);
            mGalleryAdapter.notifyItemChanged(postion);
        } else {
            storyEntityList.add(size, StoryLogic.instance().getStoryEntityFromShare());
            mGalleryAdapter.notifyItemInserted(size);
        }
        mGalleryAdapter.notifyDataSetChanged();
    }

    /**
     * 保存预览文件
     *
     * @param previewFile
     * @param html
     * @param story
     * @return
     */
    private boolean saveStoryPreview(File previewFile, String html, StoryEntity story) {
        String header = getFromAssets(PREVIEW_HEADER);
        String footer = getFromAssets(PREVIEW_FOOTER);
        LogUtil.d("首页点击预览story:" + html);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(previewFile, "utf-8");
            writer.println(header);
            writer.println(html);
            if (!Utils.isEmpty(story.storyMusicLocal)) {
                writer.println("<div id=\"audio-btn\" class=\"on\" onclick=\"lanren.changeClass(this,'media')\">");
                writer.println(String.format("    <audio loop=\"loop\" src=\"%s\" id=\"media\" preload=\"preload\"></audio>",
                        story.storyMusicLocal));
                writer.println("</div>");
            }
            writer.println(footer);
            writer.close();
        } catch (IOException e) {
            LogUtil.e("saveStoryPreview", e);
            return false;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return true;
    }

    @Override
    public void updateMsgCount() {
        if (mTextGifCount.getVisibility() == View.GONE) {
            mTextGifCount.setVisibility(View.VISIBLE);
        }
        mTextGifCount.setText(Integer.parseInt(mTextGifCount.getText().toString()) + 1 + "");
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
                int height = (int) ((mDisplayMetrics.heightPixels - 54) * 0.74);
                params = new LinearLayout.LayoutParams(width, height);

            } else {
                int width = (int) (mDisplayMetrics.widthPixels * 0.78);
                int height = (int) ((mDisplayMetrics.heightPixels - 54) * 0.85);
                params = new LinearLayout.LayoutParams(width, height);

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
            File storyDirectory = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal);
            File coverFile = new File(storyDirectory, "thumb.jpg");
            if (coverFile.exists()) {
                Picasso.with(getActivity()).invalidate(coverFile);
                Picasso.with(getActivity()).load(coverFile)
                        .fit()
                        .skipMemoryCache()
                        .centerCrop()
                        .placeholder(R.mipmap.icon_camera)
                        .error(R.mipmap.icon_login_email)
                        .into(holder.mStoryBg);
            } else {
                String imgPath = storyEntity.storyThumbUri;
                if (imgPath.contains("http")) {
                    Picasso.with(getActivity()).load(imgPath)
                            .placeholder(R.mipmap.icon_camera)
                            .error(R.mipmap.icon_login_email)
                            .fit()
                            .centerCrop()
                            .into(holder.mStoryBg);
                } else {
                    Picasso.with(getActivity()).load(new File(imgPath))
                            .fit()
                            .centerCrop()
                            .placeholder(R.mipmap.icon_camera)
                            .error(R.mipmap.icon_login_email)
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
