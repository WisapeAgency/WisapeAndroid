package com.wisape.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.common.OnActivityClickListener;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.ActiveLogic;
import com.wisape.android.model.ActiveInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.view.GalleryView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author limit
 */
public class GiftFragment extends AbsFragment {

    private static final String TAG = GiftFragment.class.getSimpleName();

    private static final int LOADER_USER_ACTIVE = 1;

    private static final String EXTRAS_COUNTRY_CODE = "country_code";
    private static final String EXTRAS_NOW = "now";

    @InjectView(R.id.gift_gallery)
    GalleryView giftGallery;
    GalleryAdapter mGalleryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gift, container, false);
        rootView.setLayoutParams(
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        handleEventCross(rootView);
        ButterKnife.inject(this, rootView);
        init();
        return rootView;
    }

    private void init() {
        mGalleryAdapter = new GalleryAdapter(getActivity());
        giftGallery.setAdapter(mGalleryAdapter);
        mGalleryAdapter.setOnRecycleViewClickListener(new OnActivityClickListener() {
            @Override
            public void onItemClickListener(String url) {
                //TODO 跳转至相对应的界面
            }
        });
        giftGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));

        Bundle args = new Bundle();
        args.putString(EXTRAS_COUNTRY_CODE, Utils.getCountry(getActivity()));
        args.putLong(EXTRAS_NOW, Utils.acquireUTCTimestamp());
        startLoadWithProgress(LOADER_USER_ACTIVE, args);
    }

    @Override
    public Message loadingInbackground(int what, Bundle args) {
        Message msg = ActiveLogic.getInstance().activeList(args.getString(EXTRAS_COUNTRY_CODE), args.getInt(EXTRAS_NOW));
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadComplete(Message data) {
        super.onLoadComplete(data);
        if(HttpUrlConstancts.STATUS_SUCCESS == data.arg1){
            List<ActiveInfo> activeInfoList = (List<ActiveInfo>)data.obj;
            if(null != activeInfoList && activeInfoList.size() > 0){
                mGalleryAdapter.setData(activeInfoList);
            }else{
                showToast("No Active");
            }
        }else{
            showToast((String)data.obj);
        }
    }


    @OnClick(R.id.btn_close)
    @SuppressWarnings("unused")
    public void onCloseClick(View view) {
        closeFragment();
    }

    private void closeFragment(){
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
        private OnActivityClickListener onRecycleViewClickListener;

        public GalleryAdapter(Context c) {
            this.mContext = c;
        }

        public void setOnRecycleViewClickListener(OnActivityClickListener onRecycleViewClickListener) {
            this.onRecycleViewClickListener = onRecycleViewClickListener;
        }

        @Override
        public GHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GHolder(LayoutInflater.from(mContext).inflate(R.layout.item_gift,parent,false));
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {
            final ActiveInfo activeInfo = activeInfoList.get(position);
            holder.imgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG,activeInfo.getId()+"");
                }
            });
            Picasso.with(mContext).load(activeInfo.getUrl()).centerCrop().resize(80,80).into(holder.imgContent);
            holder.imgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRecycleViewClickListener.onItemClickListener(activeInfo.getUrl());
                }
            });
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

    public static class GHolder extends RecyclerView.ViewHolder{

        @InjectView(R.id.img_content)
        protected ImageView imgContent;

        public GHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }
}
