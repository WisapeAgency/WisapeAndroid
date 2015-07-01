package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.wisape.android.R;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.util.KKBitmapFactory;
import com.wisape.android.util.image.StackBlur;
import com.wisape.android.view.GalleryRelativeLayoutWrapper;
import com.wisape.android.view.GalleryView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * @author Duke
 */
public class CardGalleryFragment extends AbsFragment {

    @InjectView(R.id.card_gallery)
    GalleryView mCardGallery;
    @InjectView(R.id.add_story)
    AddFloatingActionButton mAddStory;
    @InjectView(R.id.blur_background_image)
    ImageView mBlurBackgroundImage;


    GalleryAdapter mGalleryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_gallery, null, false);
        ButterKnife.inject(this, rootView);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        StackBlur.init();


        //
        Bitmap oriBitmap = KKBitmapFactory.getBitmap(CardGalleryFragment.this.getActivity(), mGalleryAdapter.getItem(0), 0, 0, Bitmap.Config.ARGB_8888);
        StackBlur.blur(oriBitmap, new StackBlur.BlurListener() {
            @Override
            public void onSuccessed(final Bitmap bitmap) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (null != bitmap && !bitmap.isRecycled())
                            mBlurBackgroundImage.setBackgroundDrawable(new BitmapDrawable(null, bitmap));
                    }
                });
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        StackBlur.free();
    }

    private int mPrimaryPosition;
    private void init() {
        mCardGallery.setSpace ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mDisplayMetrics));
        mCardGallery.setAdapter(mGalleryAdapter = new GalleryAdapter(getActivity()));
        mCardGallery.addCardGalleryEvent(new GalleryView.CardGalleryEvent() {
            @Override
            public void onCardLevelChanged(final int level,final int position) {}

            @Override
            public void onScrollStopped(View primaryView) {
                    int resId = -1;
                    if(null != mGalleryAdapter)resId = mGalleryAdapter.getItem(primaryView.getId());
                    if(mPrimaryPosition != primaryView.getId()) {
                        mPrimaryPosition = primaryView.getId();
                        Bitmap oriBitmap = KKBitmapFactory.getBitmap(CardGalleryFragment.this.getActivity(), resId, 0, 0, Bitmap.Config.ARGB_8888);
                        StackBlur.blur(oriBitmap, new StackBlur.BlurListener() {
                            @Override
                            public void onSuccessed(final Bitmap bitmap) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (null != bitmap && !bitmap.isRecycled())
                                            mBlurBackgroundImage.setBackgroundDrawable(new BitmapDrawable(null, bitmap));
                                    }
                                });
                            }
                        });
                    }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }





    public static class GalleryAdapter extends RecyclerView.Adapter<GHolder>{

        private Context mContext;

        public Integer[] imgs = {R.mipmap.navbg, R.mipmap.navbg, R.mipmap.navbg, R.mipmap.ic_launcher, R.mipmap.navbg, R.mipmap.navbg, R.mipmap.ic_launcher};
        DisplayMetrics dm;

        public GalleryAdapter(Context c) {
            dm = c.getResources().getDisplayMetrics();
            this.mContext = c;
        }

        @Override
        public GHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            GalleryRelativeLayoutWrapper view = new GalleryRelativeLayoutWrapper(mContext);

            view.setLayoutParams(new GalleryView.LayoutParams(
                    mContext.getResources().getDimensionPixelOffset(R.dimen.card_gallery_item_size_w),
                    GalleryView.LayoutParams.WRAP_CONTENT));

            GHolder holder = new GHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(GHolder holder, int position) {
            holder.wrapperView.setId(position);
            holder.wrapperView.bindData(new GalleryRelativeLayoutWrapper.GalleryWrapperData(imgs[position],"Name---"+position));
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
        public GalleryRelativeLayoutWrapper wrapperView;
        public GHolder(GalleryRelativeLayoutWrapper itemView) {
            super(itemView);
            wrapperView = itemView;
        }
    }

    @OnClick(R.id.menu_switch)
    public void onClickMenuSwitch(View view){
        Activity activity = getActivity();
        if(null == activity)return;
        if(activity instanceof MainActivity){
            ((MainActivity)activity).openOrCloseMainMenu();
        }
    }
}
