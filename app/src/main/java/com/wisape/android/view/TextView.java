package com.wisape.android.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * @author Duke
 */
public  class TextView extends AppCompatTextView {
    public TextView(Context context) {
        super(context);
        init(null,0);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    protected  void init(AttributeSet attrs, int defStyle){

    }
}
