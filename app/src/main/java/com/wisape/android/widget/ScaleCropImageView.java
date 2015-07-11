package com.wisape.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.soundcloud.android.crop.CropImageView;
import com.wisape.android.view.ScaleGestureDetector;

/**
 * Created by LeiGuoting on 10/7/15.
 */
public class ScaleCropImageView extends CropImageView implements ScaleGestureDetector.OnScaleGestureListener{
    private static final String TAG = ScaleCropImageView.class.getSimpleName();

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
        gestureDetector.setQuickScaleEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*
        int count = event.getPointerCount();
        Log.d(TAG, "#onTouchEvent count:" + count);
        boolean handed = false;
        if(2 <= count){
            handed = gestureDetector.onTouchEvent(event);
        }

        if(!handed){
            int action = event.getAction();
            if(MotionEvent.ACTION_DOWN == action){

            }else if(MotionEvent.ACTION_MOVE == action){

            }
        }
        */
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor();
        float centerX = detector.getFocusX();
        float centerY = detector.getFocusY();

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        suppMatrix.postScale(scale, scale, centerX, centerY);
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

    public float getScale() {
        return super.getScale();
    }

    @Override
    public void center(boolean horizontal, boolean vertical) {
        super.center(horizontal, vertical);
    }
}
