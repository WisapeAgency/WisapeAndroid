package com.wisape.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.wisape.android.R;

/**
 * @author Duke
 */
public class RoundImageView extends ImageView {


    public static final int ROUND_TOP_LEFT = 0x0001;
    public static final int ROUND_TOP_RIGHT = 0x0010;
    public static final int ROUND_BOTTOM_RIGHT = 0x0100;
    public static final int ROUND_BOTTOM_LEFT = 0x1000;

    protected BitmapShader mShader;
    protected Paint mBitmapPaint;
    private DisplayMetrics mDm;
    protected float mBitmapWidth, mBitmapHeight;
    private PaintFlagsDrawFilter mPaintFilter;
    private int mRoundType = 0x0000;
    private int radius;

    public RoundImageView(Context context) {
        super(context);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        ;
        mDm = getContext().getResources().getDisplayMetrics();

        mBitmapPaint = new Paint();
        mBitmapPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setAntiAlias(true);

        radius = getContext().getResources().getDimensionPixelSize(R.dimen.app_dialog_radius);

    }


    /**
     *
     * @param point {@link #ROUND_TOP_LEFT},{@link #ROUND_TOP_RIGHT},{@link #ROUND_BOTTOM_RIGHT},{@link #ROUND_BOTTOM_LEFT}
     */
    public void setOrthogonal(int point) {
        this.mRoundType = point;
    }

    public void setImageBitmap(Bitmap bitmap) {
        mShader = null;
        if (null == bitmap || bitmap.isRecycled()) {
            mBitmapPaint.setShader(null);
        } else {
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
            mShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mBitmapPaint.setShader(mShader);
        }

        this.invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        if (null != mShader) {
            Matrix matrix = new Matrix();
            mShader.getLocalMatrix(matrix);
            matrix.setScale(getWidth() / mBitmapWidth, getHeight() / mBitmapHeight);
            mShader.setLocalMatrix(matrix);
        }
        canvas.save();
        canvas.setDrawFilter(mPaintFilter);

        RectF rectF = new RectF(0, 0, this.getWidth(), this.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, mBitmapPaint);

        if(0x0000 != mRoundType) {
            if ((mRoundType & ROUND_TOP_LEFT) != 0) {
                canvas.drawRect(0, 0, radius, radius, mBitmapPaint);
            }
            if ((mRoundType & ROUND_TOP_RIGHT) != 0) {
                canvas.drawRect(rectF.right - radius, 0, rectF.right, radius, mBitmapPaint);
            }
            if ((mRoundType & ROUND_BOTTOM_LEFT) != 0) {
                canvas.drawRect(0, rectF.bottom - radius, radius, rectF.bottom, mBitmapPaint);
            }
            if ((mRoundType & ROUND_BOTTOM_RIGHT) != 0) {
                canvas.drawRect(rectF.right - radius, rectF.bottom - radius, rectF.right, rectF.bottom, mBitmapPaint);
            }
        }
        canvas.restore();
    }
}
