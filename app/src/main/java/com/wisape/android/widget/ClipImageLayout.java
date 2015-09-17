package com.wisape.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 头像裁剪布局
 */
public class ClipImageLayout extends RelativeLayout {

    /**
     * 可以缩放的图片
     */
    private ClipZoomImageView mZoomImageView;
    /**
     * 裁剪的边框
     */
    private ClipImageBorderView mClipImageBoder;


    public ClipImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mZoomImageView = new ClipZoomImageView(context);
        mClipImageBoder = new ClipImageBorderView(context);

        android.view.ViewGroup.LayoutParams layoutParams = new LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        this.addView(mZoomImageView, layoutParams);
        this.addView(mClipImageBoder, layoutParams);
    }

    public void setImageDrawable(Drawable drawable) {
        mZoomImageView.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bitmap) {
        mZoomImageView.setImageBitmap(bitmap);
    }

    /**
     * 对外公布设置左右的边距
     *
     * @param horizontalPadding 左右的边距
     */
    public void setHorizontalPadding(int horizontalPadding) {
        mClipImageBoder.setHorizontalPadding(horizontalPadding);
        mZoomImageView.setHorizontalPadding(horizontalPadding);
    }

    public void setVertrialPadding(int vertrialPadding){
        mClipImageBoder.setVerticalPadding(vertrialPadding);
        mZoomImageView.setVerticalPadding(vertrialPadding);
    }


    /**
     * 裁切图片
     *
     * @return 返回裁剪后的bitmap
     */
    public Bitmap clip() {
        return mZoomImageView.clip();
    }
}
