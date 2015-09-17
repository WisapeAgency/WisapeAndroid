package com.wisape.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 头像裁剪边框
 */
public class ClipImageBorderView extends View {
    /**
     * 水平方向的边距
     */
    private int mHorizontalPadding = 0;
    /**
     * 垂直方向与View的边距
     */
    private int mVerticalPadding = 0;
    /**
     * 边框的颜色，默认为白色
     */
    private int mBorderColor = Color.parseColor("#FFFFFF");
    /**
     * 边框的宽度 单位dp
     */
    private int mBorderWidth = 1;

    private Paint mPaint;

    public ClipImageBorderView(Context context) {
        this(context, null);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mBorderWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources()
                        .getDisplayMetrics());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 计算裁剪区域的宽度
        int mWidth = getWidth() - 2 * mHorizontalPadding;
        int mHeight = getHeight() - 2 * mVerticalPadding;

        mPaint.setColor(Color.parseColor("#aa000000"));

        mPaint.setStyle(Style.FILL);

        // 绘制左边矩形
        canvas.drawRect(0, 0, mHorizontalPadding, getHeight(), mPaint);

        // 绘制右边矩形
        canvas.drawRect(mHorizontalPadding + mWidth, 0, getWidth(),
                getHeight(), mPaint);

        // 绘制上边矩形
        canvas.drawRect(mHorizontalPadding, 0, mHorizontalPadding + mWidth,
                mVerticalPadding, mPaint);
        // 绘制下边矩形
        canvas.drawRect(mHorizontalPadding, mVerticalPadding + mHeight,
                mHorizontalPadding + mWidth, getHeight(), mPaint);

        mPaint.setColor(mBorderColor);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStyle(Style.STROKE);

        //绘制裁剪区域矩形
        canvas.drawRect(mHorizontalPadding, mVerticalPadding, getWidth()
                - mHorizontalPadding, getHeight() - mVerticalPadding, mPaint);

    }

    public void setHorizontalPadding(int horizontalPadding) {
        this.mHorizontalPadding = horizontalPadding;
    }

    public void setVerticalPadding(int verticalPadding) {
        mVerticalPadding = verticalPadding;
    }
}
