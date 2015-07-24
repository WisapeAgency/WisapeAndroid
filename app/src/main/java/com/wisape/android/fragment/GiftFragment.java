package com.wisape.android.fragment;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.util.FrescoFactory;
import com.wisape.android.view.GalleryRelativeLayoutWrapper;
import com.wisape.android.view.GalleryView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author limit
 */
public class GiftFragment extends AbsFragment {

    @InjectView(R.id.gift_gallery)
    GalleryView giftGallery;

    GalleryAdapter mGalleryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gift, null, false);
        rootView.setLayoutParams(
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT));
        handleEventCross(rootView);
        ButterKnife.inject(this, rootView);
        init();
        return rootView;
    }

    private void init() {
        giftGallery.setAdapter(mGalleryAdapter = new GalleryAdapter(getActivity()));
        giftGallery.setSpace ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
    }

    @OnClick(R.id.btn_close)
    public void onCloseClick(View view){
        FragmentTransaction trans = getWisapeFragmentManager().beginTransaction();
        trans.remove(this);
        trans.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }










    public static class GalleryAdapter extends RecyclerView.Adapter<GHolder>{

        private Context mContext;

        public Integer[] imgs = {R.drawable.navbg, R.drawable.navbg, R.drawable.navbg, R.mipmap.ic_launcher, R.drawable.navbg, R.drawable.navbg, R.mipmap.ic_launcher};
        DisplayMetrics dm;

        public GalleryAdapter(Context c) {
            dm = c.getResources().getDisplayMetrics();
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

            GHolder holder = new GHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {
            holder.wrapperView.setId(position);

            FrescoFactory.bindImage(holder.wrapperView, imgs[position]);
        }

        @Override
        public int getItemCount() {
            return imgs.length;
        }

        public int getItem(int position){
            return imgs[position];
        }
    }

    public static class GHolder extends RecyclerView.ViewHolder{
        public SimpleDraweeView wrapperView;
        public GHolder(SimpleDraweeView itemView) {
            super(itemView);
            wrapperView = itemView;
        }
    }
}
