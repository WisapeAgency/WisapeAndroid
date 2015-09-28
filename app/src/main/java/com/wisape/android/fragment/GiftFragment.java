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
import com.wisape.android.activity.AboutWebViewActivity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.ActiveLogic;
import com.wisape.android.model.ActiveInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.view.GalleryView;

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

    @InjectView(R.id.gift_gallery)
    GalleryView giftGallery;

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
        giftGallery.setSpace((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        Bundle args = new Bundle();
        args.putString(EXTRAS_COUNTRY_CODE, Utils.getCountry(getActivity()));
        startLoadWithProgress(LOADER_USER_ACTIVE, args);
    }

    @Override
    public Message loadingInbackground(int what, Bundle args) {
        Message msg = ActiveLogic.getInstance().activeList(args.getString(EXTRAS_COUNTRY_CODE));
        msg.what = what;
        return msg;
    }

    @Override
    protected void onLoadComplete(Message data) {
        super.onLoadComplete(data);
        if(HttpUrlConstancts.STATUS_SUCCESS == data.arg1){
            List<ActiveInfo> activeInfoList = (List<ActiveInfo>)data.obj;
            if(null != activeInfoList && activeInfoList.size() > 0){
                giftGallery.setAdapter(new GalleryAdapter(getActivity(),activeInfoList));
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

    public void gotoActive(String url,String title){
        AboutWebViewActivity.launch(getActivity(),url,title);
    }

    public class GalleryAdapter extends RecyclerView.Adapter<GHolder> {

        private Context mContext;
        private List<ActiveInfo> activeInfoList;

        public GalleryAdapter(Context c,List<ActiveInfo> activeInfos) {
            this.mContext = c;
            activeInfoList = activeInfos;
        }


        @Override
        public GHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GHolder(LayoutInflater.from(mContext).inflate(R.layout.item_gift,parent,false));
        }

        @Override
        public void onBindViewHolder(final GHolder holder, int position) {
            final ActiveInfo activeInfo = activeInfoList.get(position);
            Picasso.with(mContext).load(activeInfo.getBg_img()).centerCrop().resize(600,800).into(holder.imgContent);
            holder.imgContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null != activeInfo){
                        gotoActive(activeInfo.getUrl(),activeInfo.getTitle());
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return activeInfoList.size();
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
