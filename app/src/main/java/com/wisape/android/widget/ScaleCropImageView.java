//package com.wisape.android.widget;
//
//import android.content.Context;
//import android.support.v4.view.MotionEventCompat;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//
//import com.soundcloud.android.crop.CropImageView;
//import com.wisape.android.view.ScaleGestureDetector;
//
///**
// * Created by LeiGuoting on 10/7/15.
// */
//public class ScaleCropImageView extends CropImageView implements ScaleGestureDetector.OnScaleGestureListener{
//    private static final String TAG = ScaleCropImageView.class.getSimpleName();
//    private static final int INVALID_POINTER_ID = 0;
//
//    //private float posX;
//    //private float posY;
//    private float lastTouchX;
//    private float lastTouchY;
//    private int activePointerId;
//
//    private ScaleGestureDetector scaleDetector;
//
//    public ScaleCropImageView(Context context) {
//        this(context, null);
//    }
//
//    public ScaleCropImageView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public ScaleCropImageView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        scaleDetector = new ScaleGestureDetector(context, this);
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        scaleDetector.onTouchEvent(event);
//
//        final int action = MotionEventCompat.getActionMasked(event);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN: {
//                final int pointerIndex = MotionEventCompat.getActionIndex(event);
//                final float x = MotionEventCompat.getX(event, pointerIndex);
//                final float y = MotionEventCompat.getY(event, pointerIndex);
//
//                // Remember where we started (for dragging)
//                lastTouchX = x;
//                lastTouchY = y;
//                // Save the ID of this pointer (for dragging)
//                activePointerId = MotionEventCompat.getPointerId(event, 0);
//                break;
//            }
//
//            case MotionEvent.ACTION_MOVE: {
//                // Find the index of the active pointer and fetch its position
//                final int pointerIndex = MotionEventCompat.findPointerIndex(event, activePointerId);
//
//                final float x = MotionEventCompat.getX(event, pointerIndex);
//                final float y = MotionEventCompat.getY(event, pointerIndex);
//
//                // Calculate the distance moved
//                final float dx = x - lastTouchX;
//                final float dy = y - lastTouchY;
//                suppMatrix.postTranslate(dx, dy);
//                setImageMatrix(getImageViewMatrix());
//
//                //posX += dx;
//                //posY += dy;
//                // Remember this touch position for the next move event
//                lastTouchX = x;
//                lastTouchY = y;
//                break;
//            }
//
//            case MotionEvent.ACTION_UP: {
//                activePointerId = INVALID_POINTER_ID;
//                break;
//            }
//
//            case MotionEvent.ACTION_CANCEL: {
//                activePointerId = INVALID_POINTER_ID;
//                break;
//            }
//
//            case MotionEvent.ACTION_POINTER_UP: {
//
//                final int pointerIndex = MotionEventCompat.getActionIndex(event);
//                final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
//
//                if (pointerId == activePointerId) {
//                    // This was our active pointer going up. Choose a new
//                    // active pointer and adjust accordingly.
//                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//                    lastTouchX = MotionEventCompat.getX(event, newPointerIndex);
//                    lastTouchY = MotionEventCompat.getY(event, newPointerIndex);
//                    activePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
//                }
//                break;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onScale(ScaleGestureDetector detector) {
//        float scale = detector.getScaleFactor();
//        float centerX = detector.getFocusX();
//        float centerY = detector.getFocusY();
//
//        centerX = getWidth() / 2;
//        centerY = getHeight() / 2;
//        suppMatrix.postScale(scale, scale, centerX, centerY);
//        setImageMatrix(getImageViewMatrix());
//        return true;
//    }
//
//    @Override
//    public boolean onScaleBegin(ScaleGestureDetector detector) {
//        return true;
//    }
//
//    @Override
//    public void onScaleEnd(ScaleGestureDetector detector) {
//        //do nothing
//    }
//
//    public float getScale() {
//        return super.getScale();
//    }
//
//    @Override
//    public void center(boolean horizontal, boolean vertical) {
//        super.center(horizontal, vertical);
//    }
//}
