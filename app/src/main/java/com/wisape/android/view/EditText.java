package com.wisape.android.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * @author Duke
 */
public class EditText extends AppCompatEditText {
    public EditText(Context context) {
        super(context);
        init(null, 0);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }


    protected void init(AttributeSet attrs, int defStyle) {

    }
}
