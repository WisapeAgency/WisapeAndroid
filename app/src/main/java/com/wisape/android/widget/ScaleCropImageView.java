package com.wisape.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.soundcloud.android.crop.CropImageView;

/**
 * Created by LeiGuoting on 10/7/15.
 */
public class ScaleCropImageView extends CropImageView implements ScaleGestureDetector.OnScaleGestureListener{
    private ScaleGestureDetector gestureDetector;

    public ScaleCropImageView(Context context) {
        this(context, null);
    }

    public ScaleCropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleCropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        gestureDetector = new ScaleGestureDetector(context, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        setImageMatrix(getImageViewMatrix());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        //do nothing
    }
}
