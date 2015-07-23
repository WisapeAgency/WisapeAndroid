package com.wisape.android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;

import com.wisape.android.R;

import org.cubieline.lplayer.PlayerProxy;
import org.cubieline.lplayer.content.LPlayerReceiver;

import java.util.ArrayList;
import java.util.List;

import static org.cubieline.lplayer.media.IClientNotificationManager.ACTION_PLAYER_STATUS_CHANGED;
import static org.cubieline.lplayer.media.IKey._CLIENT_STATUS;
import static org.cubieline.lplayer.media.ILPlayerClient.CLIENT_STATUS_BUFFERING;
import static org.cubieline.lplayer.media.ILPlayerClient.CLIENT_STATUS_PLAYING;

/**
 * Created by LeiGuoting on 14/11/21.
 */
public class PlayerStatusView extends View{
    private static final int REFRESH_TIME_IN_MILLI = 60;
    private static final int WHAT_REFRESH_START = 0x01;
    private static final int WHAT_REFRESH_STOP = 0x02;
    private static final int ITEM_COUNT = 4;
    private static final int ITEM_STEP_COUNT = 14;
    private static final int ITEM_DIRECTION_MASK = 1 << 29;
    private static final int STEP_MOVE_BIT = 25;
    private static final int STEP_MASK = ((1 << 29) - 1) ^ ((1 << STEP_MOVE_BIT) - 1);
    private static final String TAG = PlayerStatusView.class.getSimpleName();
    private static HandlerThread handlerThread = new HandlerThread("player-status-view");
    static {
        handlerThread.start();
    }
    private static final BackgroundHandler backgroundHandler = new BackgroundHandler(handlerThread.getLooper());


    private float [] itemMatrixes;
    private float [] itemStaticMatrixes;

    private int gapWidth;
    private int itemWidth;
    private int itemColor;
    private float stepLength;
    private Paint itemPaint;

    public PlayerStatusView(Context context) {
        this(context, null);
    }

    public PlayerStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(null != attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlayerStatusView);
            itemWidth = typedArray.getDimensionPixelSize(R.styleable.PlayerStatusView_itemWidth, 0);
            gapWidth = typedArray.getDimensionPixelSize(R.styleable.PlayerStatusView_gapWidth, 0);
            itemColor = typedArray.getColor(R.styleable.PlayerStatusView_itemColor, 0xffffff);
            typedArray.recycle();
        }

        itemPaint = new Paint();
        itemPaint.setColor(itemColor);
        itemPaint.setAntiAlias(true);
        itemPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        backgroundHandler.registerView(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int height = getHeight();
        final int paddingBottom = getPaddingBottom();
        final int paddingTop = getPaddingTop();
        final int itemMaxHeight = height - (paddingBottom + paddingTop);
        stepLength = (float)itemMaxHeight / (float)ITEM_STEP_COUNT;
        if(0 >= stepLength){
            stepLength = 1;
        }
        itemStaticMatrixes = getActivityItemsHeight(backgroundHandler.getStaticStepAndDirection());
    }

    /**
     * The method is invoked in background thread
     */
    /*package*/ void postRefresh(float[] itemMatrixes){
        synchronized (this){
            this.itemMatrixes = itemMatrixes;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int paddingBottom = getPaddingBottom();

        float itemLeft;
        float itemRight;
        float itemTop;
        float itemHeight;
        final int itemBottom = height - paddingBottom;
        final int itemAndGapWidth = itemWidth + gapWidth;
        final int leftForItem = (width - (ITEM_COUNT * itemWidth + (ITEM_COUNT - 1) * gapWidth)) / 2;

        float[] matrixes;
        synchronized (this){
            matrixes = (null == itemMatrixes ? itemStaticMatrixes : itemMatrixes);
            itemMatrixes = null;
        }

        RectF itemF;
        for (int i = 0; i < ITEM_COUNT; i++) {
            itemHeight = matrixes[i];
            itemLeft = i * itemAndGapWidth + leftForItem;
            itemRight = itemLeft + itemWidth;
            itemTop = itemBottom - itemHeight;

            itemF = new RectF(itemLeft, itemTop, itemRight, itemBottom);
            canvas.drawRect(itemF, itemPaint);
        }
        canvas.restore();
    }

    /*package*/ float[] getActivityItemsHeight(int[] stepAndDirectionArray){
        int length = stepAndDirectionArray.length;
        float [] matrixes = new float[length];
        for(int i = 0; i < length; i ++){
            matrixes[i] = getActivityItemHeight(stepAndDirectionArray[i]);
        }
        return matrixes;
    }

    private float getActivityItemHeight(int stepAndDirection){
        int step = Utils.getStep(stepAndDirection);
        return step * stepLength;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        backgroundHandler.unregisterView(this);
    }

    private static final class BackgroundHandler extends Handler implements LPlayerReceiver.OnLPlayerBroadcastReceiverListener{
        private LPlayerReceiver lPlayerReceiver;
        private LocalBroadcastManager localBroadcastManager;
        private List<PlayerStatusView> viewList = new ArrayList(5);
        private final int[] stepAndDirectionArray;
        private final int[] staticStepAndDirectionArray;
        private boolean isShowing;



        /*package*/ BackgroundHandler(Looper looper){
            super(looper);
            staticStepAndDirectionArray = new int[ITEM_COUNT];
            stepAndDirectionArray = new int[ITEM_COUNT];
            int direction = ITEM_DIRECTION_MASK;
            int itemStep = Utils.putStepToBit(ITEM_STEP_COUNT / 2);
            stepAndDirectionArray[0] =  staticStepAndDirectionArray[0] = itemStep | direction;
            itemStep = Utils.putStepToBit(ITEM_STEP_COUNT);
            stepAndDirectionArray[1] =  staticStepAndDirectionArray[1] =  itemStep | direction;
            itemStep = Utils.putStepToBit(ITEM_STEP_COUNT / 4 * 3);
            stepAndDirectionArray[2] =  staticStepAndDirectionArray[2] = itemStep | direction;
            itemStep = Utils.putStepToBit(ITEM_STEP_COUNT / 4);
            stepAndDirectionArray[3] =  staticStepAndDirectionArray[3] = itemStep | direction;
        }

        private void copyStatic(){
            int length = stepAndDirectionArray.length;
            for(int i = 0; i < length; i ++){
                stepAndDirectionArray[i] = staticStepAndDirectionArray[i];
            }
        }

        /*package*/ int[] getStaticStepAndDirection(){
            return staticStepAndDirectionArray;
        }

        /**
         * Working in background thread
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
                    return;

                case WHAT_REFRESH_START:
                    if (isShowing) {
                        //Refresh UI
                        final int size = viewList.size();
                        if(0 == size){
                            return;
                        }

                        PlayerStatusView[] views = viewList.toArray(new PlayerStatusView[size]);
                        float [] itemHeightArray;
                        for(PlayerStatusView view : views){
                            if(null == view){
                                continue;
                            }

                            itemHeightArray = view.getActivityItemsHeight(stepAndDirectionArray);
                            view.postRefresh(itemHeightArray);
                        }

                        int step;
                        int direction;
                        int stepAndDirection;
                        int length = stepAndDirectionArray.length;
                        for(int i = 0; i < length; i ++){
                            stepAndDirection = stepAndDirectionArray[i];
                            step = Utils.getStep(stepAndDirection);
                            direction = ITEM_DIRECTION_MASK & stepAndDirection;

                            //UP
                            if(ITEM_DIRECTION_MASK == direction){
                                ++step;
                                if(ITEM_STEP_COUNT < step){
                                    step = ITEM_STEP_COUNT;
                                    //Changing direction to down
                                    direction = 0;
                                }
                                int stepBit = Utils.putStepToBit(step);
                                stepAndDirection = stepBit | direction;
                            }

                            //Down
                            else{
                                --step;
                                if(0 >= step){
                                    step = 1;
                                    //Changing direction to up
                                    direction = ITEM_DIRECTION_MASK;
                                }
                                step = Utils.putStepToBit(step);
                                stepAndDirection = step | direction;
                            }
                            stepAndDirectionArray[i] = stepAndDirection;
                        }
                        sendMessageDelayed(obtainMessage(WHAT_REFRESH_START), REFRESH_TIME_IN_MILLI);
                    }
                    return;

                case WHAT_REFRESH_STOP:
                    float [] itemHeightArray;
                    final int size = viewList.size();
                    if(0 < size){
                        PlayerStatusView[] views = viewList.toArray(new PlayerStatusView[size]);
                        for (PlayerStatusView view : views) {
                            itemHeightArray = view.getActivityItemsHeight(staticStepAndDirectionArray);
                            view.postRefresh(itemHeightArray);
                        }
                    }
                    copyStatic();
                    removeMessages(WHAT_REFRESH_START);
                    return;
            }
        }

        /**
         * Working in UI thread
         *
         * @param context
         * @param intent
         */
        @Override
        public void onLPlayerReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }

            int status = intent.getIntExtra(_CLIENT_STATUS, 0);
            if (CLIENT_STATUS_PLAYING == status || CLIENT_STATUS_BUFFERING == status) {
                if(!isShowing){
                    isShowing = true;
                    obtainMessage(WHAT_REFRESH_START).sendToTarget();
                }
            } else {
                isShowing = false;
                obtainMessage(WHAT_REFRESH_STOP).sendToTarget();
            }
        }

        public void registerView(PlayerStatusView statusView){
            final int size = viewList.size();
            if(0 == size){
                lPlayerReceiver = new LPlayerReceiver(this);
                localBroadcastManager = LocalBroadcastManager.getInstance(statusView.getContext().getApplicationContext());
                localBroadcastManager.registerReceiver(lPlayerReceiver, new IntentFilter(ACTION_PLAYER_STATUS_CHANGED));

                new AsyncTask<Object, Object, Integer>() {
                    @Override
                    protected Integer doInBackground(Object... params) {
                        int status;
                        try{
                            status = PlayerProxy.getLPlayerClient().getClientStatus();
                        }catch(IllegalStateException e){
                            status = -1;
                        }
                        return status;
                    }

                    @Override
                    protected void onPostExecute(Integer statusFromPlayer) {
                        int status = statusFromPlayer;
                        if (CLIENT_STATUS_PLAYING == status || CLIENT_STATUS_BUFFERING == status) {
                            if(!isShowing){
                                isShowing = true;
                                obtainMessage(WHAT_REFRESH_START).sendToTarget();
                            }
                        }
                    }
                }.execute();
            }

            if(!viewList.contains(statusView)){
                viewList.add(statusView);
            }
        }

        public void unregisterView(PlayerStatusView statusView){
            viewList.remove(statusView);
            int size = viewList.size();
            if(0 == size){
                localBroadcastManager.unregisterReceiver(lPlayerReceiver);
                localBroadcastManager = null;
                lPlayerReceiver.onDestroy();
                lPlayerReceiver = null;
                isShowing = false;
            }
        }
    }

    /*package*/ static final class Utils{
        public static int getStep(int stepAndDirection){
            return (STEP_MASK & stepAndDirection) >> STEP_MOVE_BIT;
        }

        public static int putStepToBit(int step){
            return step << STEP_MOVE_BIT;
        }
    }
}