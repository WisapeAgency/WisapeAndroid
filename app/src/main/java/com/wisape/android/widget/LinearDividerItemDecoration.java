package com.wisape.android.widget;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * http://www.snip2code.com/Snippet/209554/DIviderItemDecoration-updated-without-de
 *
 * Created by LeiGuoting on 14/12/2.
 */
public class LinearDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private boolean isIgnoreHeadItem;
    private int paddingLeftPixels;
    private int paddingRightPixels;
    private int orientation = LinearLayoutManager.VERTICAL;

    public LinearDividerItemDecoration(Drawable drawable){
        this(drawable, false);
    }

    public LinearDividerItemDecoration(Resources resources, int dividerResId){
        this(resources, dividerResId, false);
    }

    public LinearDividerItemDecoration(Resources resources, int dividerResId, boolean isIgnoreHeadItem){
        this(resources.getDrawable(dividerResId), isIgnoreHeadItem);
    }

    public LinearDividerItemDecoration(Drawable drawable, boolean isIgnoreHeadItem){
        divider = drawable;
        this.isIgnoreHeadItem = isIgnoreHeadItem;
    }

    /**
     *
     * @param orientation LinearLayoutManager.HORIZONTAL or LinearLayoutManager.VERTICAL
     */
    public void setOrientation(int orientation){
        this.orientation = orientation;
    }

    public void setPaddingLeftAndRight(Resources resources, int paddingLeftResId, int paddingRightResId){
        setPaddingLeftAndRight(0 < paddingLeftResId ? resources.getDimensionPixelSize(paddingLeftResId) : 0, 0 < paddingRightResId ? resources.getDimensionPixelSize(paddingRightResId) : 0);
    }

    public void setPaddingLeftAndRight(int paddingLeftPixels, int paddingRightPixels){
        this.paddingLeftPixels = paddingLeftPixels;
        this.paddingRightPixels = paddingRightPixels;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        //super.onDrawOver(canvas, parent, state);
        if(LinearLayoutManager.VERTICAL == orientation){
            doDrawVertical(canvas, parent, state);
        }else if(LinearLayoutManager.HORIZONTAL == orientation){
            doDrawHorizontal(canvas, parent, state);
        }
    }

    private void doDrawHorizontal(Canvas canvas, RecyclerView parent, RecyclerView.State state){
        final int parentPaddingTop = parent.getPaddingTop();
        final int parentPaddingBottom = parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        RecyclerView.LayoutParams params;
        View child;

        int start;
        int end;
        int top = parentPaddingTop;
        int bottom = parentPaddingBottom;
        int size;

        for (int i = 1; i < childCount; i++) {
            child = parent.getChildAt(i);
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            size = divider.getIntrinsicWidth();
            start = child.getLeft() - params.leftMargin - size;
            end = start + size;
            divider.setBounds(start, top, end, bottom);
            divider.draw(canvas);
        }
    }

    private void doDrawVertical(Canvas canvas, RecyclerView parent, RecyclerView.State state){
        final int parentPaddingLeft = parent.getPaddingLeft();
        final int parentPaddingRight = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        RecyclerView.LayoutParams params;
        View child;
        int bottom;
        int size;
        int top;
        int position;
        int left = parentPaddingLeft;
        if(0 < paddingLeftPixels){
            left = parentPaddingLeft + paddingLeftPixels;
            if(left > parent.getWidth()){
                left = parent.getWidth();
            }
        }

        int right = parentPaddingRight;
        if(0 < paddingRightPixels){
            right = parentPaddingRight - paddingRightPixels;
            if(right <= left){
                right = left;
            }
        }

        for (int i = 1; i < childCount; i++) {
            child = parent.getChildAt(i);
            if(isIgnoreHeadItem){
                position = parent.getChildPosition(child);
                if(1 == position){
                    continue;
                }
            }
            params = (RecyclerView.LayoutParams) child.getLayoutParams();
            size = divider.getIntrinsicHeight();
            top = child.getTop() - params.topMargin - size;
            bottom = top + size;
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }

    @Override
    @TargetApi(17)
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildPosition(view);
        if (1 > position || (isIgnoreHeadItem && 1 == position)){
            return ;
        }

        if(LinearLayoutManager.VERTICAL == orientation){
            outRect.top = divider.getIntrinsicHeight();
        }else if(LinearLayoutManager.HORIZONTAL == orientation){
            final boolean isLayoutRtl = 17 <= Build.VERSION.SDK_INT && View.LAYOUT_DIRECTION_RTL == parent.getLayoutDirection();

            if(isLayoutRtl){
                outRect.right = divider.getIntrinsicWidth();
            }else{
                outRect.left = divider.getIntrinsicWidth();
            }
        }
    }
}