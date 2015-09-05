package com.wisape.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

import com.wisape.android.R;

/**
 * zhy
 *
 * @author zhy
 */
public class ClipImageLayout extends RelativeLayout {

    private ClipZoomImageView mZoomImageView;
    private ClipImageBorderView mClipImageView;

    /**
     * 这里测试，直接写死了大小，真正使用过程中，可以提取为自定义属性
     */
    private int mHorizontalPadding = 20;
    private Uri imgIconUri;

    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mZoomImageView = new ClipZoomImageView(context);
        mClipImageView = new ClipImageBorderView(context);

        android.view.ViewGroup.LayoutParams lp = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        this.addView(mZoomImageView, lp);
        this.addView(mClipImageView, lp);


        // 计算padding的px
        mHorizontalPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mHorizontalPadding, getResources()
                        .getDisplayMetrics());
        mZoomImageView.setHorizontalPadding(mHorizontalPadding);
//        mClipImageView.setHorizontalPadding(mHorizontalPadding);
    }

    /**
     * 对外公布设置边距的方法,单位为dp
     *
     * @param mHorizontalPadding
     */
    public void setHorizontalPadding(int mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
    }

    /**
     * 对外公布的设置需要裁剪的图片
     * @param imgIconUri
     */
    public void setImgIconUri(Uri imgIconUri){
        this.imgIconUri = imgIconUri;
        mZoomImageView.setImageURI(imgIconUri);
    }

    /**
     * 裁切图片
     *
     * @return
     */
    public Bitmap clip() {
        if(imgIconUri == null){
            return null;
        }else{
            return mZoomImageView.clip();

        }
    }

}
