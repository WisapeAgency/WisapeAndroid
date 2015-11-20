package com.wisape.android.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.wisape.android.R;

import java.util.ArrayList;


/**
 * @author Duke
 */
public class GalleryView extends RecyclerView{

    public final static int LEVEL_PRIMARY = 1;
    public final static int LEVEL_SECONDARY = 2;


    DisplayMetrics mDisplayMetrics;
    int mSpace;
    ArrayList<CardGalleryEvent> mCardGalleryEventList;
    GalleryLayoutManager layoutManager;

    public GalleryView(Context context) {
        super(context);
        init();
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        mCardGalleryEventList = new ArrayList<>();

        setHasFixedSize(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        mDisplayMetrics = getResources().getDisplayMetrics();
        final int cardWidth = mDisplayMetrics.widthPixels - 150;
        this.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                int padding = (parent.getWidth() - cardWidth) / 2;

                if (parent.getChildAdapterPosition(view) == 0)
                    outRect.left = padding;
                else if (parent.getChildAdapterPosition(view) == (parent.getAdapter().getItemCount() - 1)) {
                    outRect.left = mSpace;
                    outRect.right = padding;
                } else {
                    outRect.left = mSpace;
                }

            }
        });


        layoutManager = new GalleryLayoutManager(getContext());
        layoutManager.setOrientation(GalleryLayoutManager.HORIZONTAL);
        this.setLayoutManager(layoutManager);

        addOnScrollListener(new GalleryScrollListener());
    }

    public void setVisiblePostion(int postion){
        layoutManager.scrollToPosition(postion);
    }

    public void setSpace(int space) {
        this.mSpace = space;
    }

    public class GalleryLayoutManager extends LinearLayoutManager{

        public GalleryLayoutManager(Context context) {
            super(context);
        }

        public GalleryLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public GalleryLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }
    }


    public class GalleryScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            Point pCenterPoint = new Point(getLeft() + getWidth() / 2, getTop() + getHeight() / 2);

            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = getChildAt(i);
                if(null == view)continue;
                if (view instanceof CardGalleryEvent) {
                    CardGalleryEvent eventView = (CardGalleryEvent)view;
                    Rect cRect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (cRect.contains(pCenterPoint.x, pCenterPoint.y)) {
                        eventView.onCardLevelChanged(LEVEL_PRIMARY,view.getId());
                        for(CardGalleryEvent event:mCardGalleryEventList)if(null != event)event.onCardLevelChanged(LEVEL_PRIMARY,view.getId());
                    } else {
                        eventView.onCardLevelChanged(LEVEL_SECONDARY,view.getId());
                        for(CardGalleryEvent event:mCardGalleryEventList)if(null != event)event.onCardLevelChanged(LEVEL_SECONDARY,view.getId());
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (SCROLL_STATE_IDLE == newState) {

                Point pCenterPoint = new Point(getLeft() + getWidth() / 2, getTop() + getHeight() / 2);

                boolean hasCenterView = false;

                int childCount = getChildCount();
                View primaryView = null;
                for (int i = 0; i < childCount; i++) {
                    View view = getChildAt(i);
                    if(null == view)continue;
                    Rect cRect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (cRect.contains(pCenterPoint.x, pCenterPoint.y)) {
                        int lx = pCenterPoint.x-view.getWidth()/2;
                        int dx = view.getLeft()-lx;
                        recyclerView.scrollBy(dx,0);
                        hasCenterView = true;
                        primaryView = view;
                    }
                }

                //not found center view
                if(!hasCenterView) {
                    float minDistance = Float.MAX_VALUE;
                    View minDistanceView = null;
                    for (int i = 0; i < childCount; i++) {
                        View view = getChildAt(i);
                        if (null == view) continue;

                        int vx = view.getLeft() + view.getWidth() / 2;
                        if(vx <= minDistance){
                            minDistance = vx;
                            minDistanceView = view;
                        }
                    }

                    if(null != minDistanceView){
                        int lx = pCenterPoint.x-minDistanceView.getWidth()/2;
                        int dx = minDistanceView.getLeft()-lx;
                        recyclerView.scrollBy(dx,0);
                        primaryView = minDistanceView;
                    }
                }

                for(CardGalleryEvent event:mCardGalleryEventList)if(null != event)event.onScrollStopped(primaryView);
            }
        }
    }


    public void addCardGalleryEvent(CardGalleryEvent event) {
        if (null == mCardGalleryEventList)
            mCardGalleryEventList = new ArrayList<>();

        mCardGalleryEventList.add(event);
    }

    public void removeCardGalleryEvent(CardGalleryEvent event) {
        if (null != mCardGalleryEventList)
            mCardGalleryEventList.remove(event);
    }

    public interface CardGalleryEvent{
        /**
         * {@link #LEVEL_PRIMARY},{@link #LEVEL_SECONDARY}
         * @param level
         */
        void onCardLevelChanged(int level,int position);

        void onScrollStopped(View primaryView);
    }

}
