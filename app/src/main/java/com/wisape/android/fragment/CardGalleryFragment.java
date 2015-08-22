package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.Message.OperateMessage;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.activity.TestActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.http.DefaultHttpRequestListener;
import com.wisape.android.http.HttpRequest;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.util.FrescoFactory;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.PopupWindowMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment {

    private static final String TAG = CardGalleryFragment.class.getSimpleName();

    private static final String DEFAULT_STORY_ID_KEY = "id";
    private static final String DEFAULT_STORY_NAME_KEY = "name";
    private static final String DEFAULT_STORY_URL_KEY = "zip_url";
    private static final String DEFAUTL_STORY_IMAGE_URL_KEY = "small_img";

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.gift_count)
    TextView mTextGifCount;
    private StoryInfo defaultStroy;

    private PopupWindowMenu popupWindow;
    private GalleryAdapter mGalleryAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_gallery, container, false);
        ButterKnife.inject(this, rootView);
        EventBus.getDefault().register(this);
        initView();
        return rootView;
    }


    private void initView() {
        popupWindow = new PopupWindowMenu(getActivity());
        mCardGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        mCardGallery.setAdapter(mGalleryAdapter = new GalleryAdapter(getActivity()));
        defaultStroy = new StoryInfo();
        getDefaultStoryInfo();
    }

    /**
     * 获取默认模版信息
     */
    private void getDefaultStoryInfo() {
        ((BaseActivity) getActivity()).showProgressDialog(R.string.loading_user_story);
        HttpRequest.addRequest(WWWConfig.acquireUri(getResources().getString(R.string.uri_story_default)).toString()
                ,this, new DefaultHttpRequestListener() {
            @Override
            public void onReqeustSuccess(String data) {
                ((BaseActivity) getActivity()).closeProgressDialog();
                Log.e(TAG, "#getDefaultStory:" + data);
                JSONObject jsonObject = JSONObject.parseObject(data);
                defaultStroy.id = jsonObject.getInteger(DEFAULT_STORY_ID_KEY);
                defaultStroy.story_name = jsonObject.getString(DEFAULT_STORY_NAME_KEY);
                defaultStroy.story_url = jsonObject.getString(DEFAULT_STORY_URL_KEY);
                defaultStroy.small_img =jsonObject.getString(DEFAUTL_STORY_IMAGE_URL_KEY);
                downloadStroy();
                showDataInView();
            }

            @Override
            public void onError(String message) {
                ((BaseActivity) getActivity()).closeProgressDialog();
                ((BaseActivity) getActivity()).showToast("加载默认Story出错");
            }
        });
    }

    /**
     * 下载默认Story
     */
    private void downloadStroy() {
        final File file = new File(StoryManager.getStoryDirectory(), defaultStroy.story_name + ".zip");
        Log.e(TAG, "默认模版是否存在：" + file.exists());
        if (!file.exists()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "开始下载默认stotry");
                    Downloader.download(Uri.parse(defaultStroy.story_url),
                            Uri.fromFile(file), new Downloader.DownloaderCallback() {
                                @Override
                                public void onDownloading(double progress) {
                                }

                                @Override
                                public void onCompleted(Uri uri) {
                                    Log.e(TAG, "#downLoadStory:" + uri.toString());
                                }

                                @Override
                                public void onError(Uri uri) {
                                }
                            });
                }
            }).start();
        }
    }

    /**
     * 显示数据
     */
    private void showDataInView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<StoryEntity> storyEntities = StoryLogic.instance().getIndexStoryList(getActivity(),
                        WisapeApplication.getInstance().getUserInfo().user_id);
                getStroyFromServer(storyEntities);
            }
        }).start();
    }

    /**
     * 从服务端获取数据
     *
     * @param storyEntityList 数据库查处得数据
     */
    private void getStroyFromServer(final List<StoryEntity> storyEntityList) {
        Map<String, String> params = new HashMap<>();
        String url = WWWConfig.acquireUri(getResources().getString(R.string.uri_story_list)).toString()+"?access_token="+ WisapeApplication.getInstance().getUserInfo().access_token;
        HttpRequest.addRequest(url,
                this, new DefaultHttpRequestListener() {
                    @Override
                    public void onReqeustSuccess(String responseJson) {
                        Log.e(TAG, "#showDataInView:" + responseJson);
                        List<StoryInfo> storyEntities = JSONObject.parseArray(responseJson, StoryInfo.class);
                        getAllStory(storyEntityList, storyEntities);
                    }

                    @Override
                    public void onError(String message) {
                        getAllStory(storyEntityList,null);
                    }
                });
    }

    /**
     * 数据整合
     *
     * @param storyEntityList 本地数据库数据
     * @param storyInfoList   服务器端数据
     */
    private void getAllStory(List<StoryEntity> storyEntityList, List<StoryInfo> storyInfoList) {
        if(storyInfoList == null){
            storyInfoList = new ArrayList<>();
        }
        if (null != storyEntityList) {
            int size = storyEntityList.size();
            for (int i = 0; i < size; i++) {
                StoryEntity storyEntity = storyEntityList.get(i);

                StoryInfo storyInfo = new StoryInfo();
                storyInfo.story_name = storyEntity.storyName;
                storyInfo.view_num = storyEntity.viewNum;
                storyInfo.like_num = storyEntity.likeNum;
                storyInfo.share_num = storyEntity.shareNum;
                storyInfo.id = storyEntity.id;//可能会有问题
                storyInfo.small_img = storyEntity.storyThumbUri;
                storyInfo.uid = storyEntity.userId;
                storyInfoList.add(storyInfo);
            }
        }
        ((BaseActivity) getActivity()).closeProgressDialog();
        storyInfoList.add(0, defaultStroy);
        mGalleryAdapter.setData(storyInfoList);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
        EventBus.getDefault().unregister(this);
        popupWindow.dismiss();
    }

    @OnClick(R.id.add_story)
    @SuppressWarnings("unused")
    protected void doAddStory() {
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

        public GalleryAdapter(Context c) {
            this.mContext = c;
        }

        @Override
        public GHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_view_card_item, parent, false);
            return new GHolder(view, new MyClickeListener() {
                @Override
                public void onItemClick(int positonId) {
                    Log.e(TAG, "#oncreateViewHoder:" + positonId);
                }

                @Override
                public void onShareClicked(int position) {
                    Log.e(TAG, "#oncreateViewHoder:" + position);
                    if (!popupWindow.isShowing()) {
                        PopupWindowMenu.setStoryId(position);
                        popupWindow.showAtLocation(parent, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                }
            });
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {
            StoryInfo storyEntity = storyEntityList.get(position);

            holder.mTextEyecount.setText(storyEntity.view_num+"");
            holder.mTextStoryState.setText(storyEntity.status+"");
            holder.mTextZanCount.setText(storyEntity.like_num+"");
            holder.mTextShareCount.setText(storyEntity.share_num + "");

            Log.e(TAG, "image_url:" + storyEntity.small_img);
            FrescoFactory.bindImageFromUri(holder.mStoryBg, storyEntity.story_url);
            holder.mTextStoryName.setText(storyEntity.story_name);
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

    public class GHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.main_story_bg)
        SimpleDraweeView mStoryBg;

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
        private MyClickeListener clickeListener;


        public GHolder(View itemView, MyClickeListener myClickeListener) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            clickeListener = myClickeListener;
            mStoryBg.setOnClickListener(this);
            imageShare.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (R.id.main_story_bg == v.getId()) {
                clickeListener.onItemClick(getPosition());
            }
            if (R.id.main_story_more == v.getId()) {
                clickeListener.onShareClicked(getPosition());
            }

        }
    }

    interface MyClickeListener {
        void onItemClick(int postionId);

        void onShareClicked(int postionId);
    }


    /**
     * eventbus消息处理
     */
    public void onEventMainThread(com.wisape.android.Message.Message message) {
        if (message instanceof OperateMessage) {
            if (mTextGifCount.getVisibility() == View.GONE) {
                mTextGifCount.setVisibility(View.VISIBLE);
            }
            mTextGifCount.setText(Integer.parseInt(mTextGifCount.getText().toString()) + 1 + "");
        }
    }

    private void clearMsgCount() {
        mTextGifCount.setText("0");
        mTextGifCount.setVisibility(View.GONE);
    }
}
