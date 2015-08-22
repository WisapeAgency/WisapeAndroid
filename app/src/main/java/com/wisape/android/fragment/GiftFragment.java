package com.wisape.android.fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONObject;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.Message.SystemMessage;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.BaseActivity;
import com.wisape.android.http.DefaultHttpRequestListener;
import com.wisape.android.http.HttpRequest;
import com.wisape.android.model.ActiveInfo;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.util.FrescoFactory;
//import com.wisape.android.view.GalleryRelativeLayoutWrapper;
import com.wisape.android.view.GalleryView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author limit
 */
public class GiftFragment extends AbsFragment {

    private static final String REQEUST_PARAM_KEY = "country_code";

    private static final String TAG = GiftFragment.class.getSimpleName();

    @InjectView(R.id.gift_gallery)
    GalleryView giftGallery;
    @InjectView(R.id.img_no_active)
    ImageView imageNoActive;
    GalleryAdapter mGalleryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gift, null, false);
        rootView.setLayoutParams(
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        handleEventCross(rootView);
        ButterKnife.inject(this, rootView);
        init();
        return rootView;
    }

    private void init() {
        giftGallery.setAdapter(mGalleryAdapter = new GalleryAdapter(getActivity()));
        giftGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));

        ((BaseActivity) getActivity()).showProgressDialog(R.string.loading_user_story);
        String url = WWWConfig.acquireUri(getString(R.string.uri_active_list)).toString() + "?"+REQEUST_PARAM_KEY+"="+getResources().getConfiguration().locale.getCountry()+
        "&now=" + System.currentTimeMillis()+"";
        HttpRequest.addRequest(url, this,
                new DefaultHttpRequestListener() {
                    @Override
                    public void onError(String message) {
                        ((BaseActivity) getActivity()).closeProgressDialog();
                        ((BaseActivity) getActivity()).showToast(null == message?"加载数据出错":message);
                    }

                    @Override
                    public void onReqeustSuccess(String data) {
                        Log.e(TAG, "onReqeustSuccess:" + data);
                        ((BaseActivity) getActivity()).closeProgressDialog();
                        List<ActiveInfo> activeInfos = JSONObject.parseArray(data, ActiveInfo.class);
                        if(null == activeInfos || activeInfos.size() == 0){
                            giftGallery.setVisibility(View.GONE);
                            imageNoActive.setVisibility(View.VISIBLE);
                        }else{
                            mGalleryAdapter.setData(activeInfos);
                        }
                    }
                });
    }

    @OnClick(R.id.btn_close)
    public void onCloseClick(View view) {
        FragmentTransaction trans = getWisapeFragmentManager().beginTransaction();
        trans.remove(this);
        trans.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


    public static class GalleryAdapter extends RecyclerView.Adapter<GHolder> {

        private Context mContext;
        private List<ActiveInfo> activeInfoList = new ArrayList<>();

        public GalleryAdapter(Context c) {
            this.mContext = c;
        }

        @Override
        public GHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SimpleDraweeView view = new SimpleDraweeView(mContext);

            GenericDraweeHierarchyBuilder builder = new GenericDraweeHierarchyBuilder(parent.getContext().getResources());
            GenericDraweeHierarchy hierarchy = builder
                    .setFadeDuration(100)
                    .setRoundingParams(new RoundingParams().setCornersRadius(parent.getResources().getDimensionPixelSize(R.dimen.app_dialog_radius)))
                    .build();
            view.setHierarchy(hierarchy);

            view.setLayoutParams(new GalleryView.LayoutParams(
                    mContext.getResources().getDimensionPixelOffset(R.dimen.card_gallery_item_size_w),
                    GalleryView.LayoutParams.MATCH_PARENT));

            GHolder holder = new GHolder(view, new GHolder.OnViewClickListener() {
                @Override
                public void onItemClicked(int postion) {
                    Log.e(TAG,"onItemClicked:" + postion);
                    //TODO 进入查看活动界面
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {
            holder.wrapperView.setId(position);
            FrescoFactory.bindImageFromUri(holder.wrapperView, activeInfoList.get(position).getUrl());
        }

        @Override
        public int getItemCount() {
            return activeInfoList.size();
        }

        public void setData(List<ActiveInfo> activeInfos) {
            activeInfoList = activeInfos;
            notifyDataSetChanged();
        }
    }

    public static class GHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public SimpleDraweeView wrapperView;
        private OnViewClickListener onViewClickListener;

        public GHolder(SimpleDraweeView itemView,OnViewClickListener clickListener) {
            super(itemView);
            wrapperView = itemView;
            onViewClickListener = clickListener;
        }

        @Override
        public void onClick(View v) {
            onViewClickListener.onItemClicked(getPosition());
        }

        interface OnViewClickListener{
            void onItemClicked(int postion);
        }
    }
}
