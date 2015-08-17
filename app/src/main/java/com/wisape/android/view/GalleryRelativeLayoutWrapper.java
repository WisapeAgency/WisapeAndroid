//package com.wisape.android.view;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//
//import com.facebook.drawee.view.SimpleDraweeView;
//import com.wisape.android.R;
//import com.wisape.android.util.FrescoFactory;
//
//import butterknife.ButterKnife;
//import butterknife.InjectView;
//
///**
// * @author Duke
// */
//public class GalleryRelativeLayoutWrapper extends RelativeLayout implements GalleryView.CardGalleryEvent {
//
//    @InjectView(R.id.shadow_view)
//    RelativeLayout mShadowView;
//    @InjectView(R.id.card_album)
//    SimpleDraweeView mCardAlbum;
//    @InjectView(R.id.story_name)
//    TextView mStoryName;
//
//    public GalleryRelativeLayoutWrapper(Context context) {
//        super(context);
//        init();
//    }
//
//    public GalleryRelativeLayoutWrapper(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    private void init() {
//        LayoutInflater.from(getContext()).inflate(R.layout.layout_main_view_card_item, this, true);
//
//        ButterKnife.inject(this, this);
//
//
//        mCardAlbum.setScaleType(ImageView.ScaleType.FIT_CENTER);
//    }
//
//
//    public void setShadowViewVisable(boolean flag) {
//        if (null != mShadowView) {
//            if (flag) {
//                if (mShadowView.getVisibility() != View.VISIBLE)
//                    mShadowView.setVisibility(View.VISIBLE);
//            } else {
//                if (View.GONE != mShadowView.getVisibility())
//                    mShadowView.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//    public void bindData(GalleryWrapperData data) {
//
//        FrescoFactory.bindImage(mCardAlbum, data.album);
//        mStoryName.setText(data.name);
//    }
//
//    @Override
//    public void onCardLevelChanged(int level, int position) {
//        if (GalleryView.LEVEL_PRIMARY == level)
//            setShadowViewVisable(false);
//        else
//            setShadowViewVisable(true);
//    }
//
//    @Override
//    public void onScrollStopped(View primaryView) {
//
//    }
//
//
//    public final static class GalleryWrapperData {
//        public int album;
//        public String name;
//
//        public GalleryWrapperData(int album, String name) {
//            this.album = album;
//            this.name = name;
//        }
//    }
//}
