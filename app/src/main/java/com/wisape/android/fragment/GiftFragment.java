package com.wisape.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.wisape.android.R;
import com.wisape.android.activity.AboutWebViewActivity;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.logic.ActiveLogic;
import com.wisape.android.model.ActiveInfo;
import com.wisape.android.util.Utils;
import com.wisape.android.view.GalleryView;
import com.wisape.android.widget.DividerItemDecoration;
import com.wisape.android.widget.NoticeDialog;

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
    RecyclerView giftGallery;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        giftGallery.setLayoutManager(linearLayoutManager);
        giftGallery.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.HORIZONTAL_LIST,false));

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
               handler.sendEmptyMessage(1);
            }
        }else{
            showToast((String)data.obj);
        }
    }

   private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            closeFragment();
            NoticeDialog.getInstance("No Active","There is current no active").show(getFragmentManager(), "close");

        }
    };


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
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        it.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        getActivity().startActivity(it);
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

            LinearLayout.LayoutParams params;

            int width = (int)(mDisplayMetrics.heightPixels * 0.48);
            params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);

            holder.linearGif.setLayoutParams(params);
            Picasso.with(mContext).load(activeInfo.getBg_img())
                    .placeholder(R.mipmap.icon_camera)
                    .error(R.mipmap.app_logo)
                    .into(holder.imgContent);
            holder.imgContent.setAlpha(1.0f);
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
        @InjectView(R.id.linear_gif)
        protected LinearLayout linearGif;

        public GHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }
}
