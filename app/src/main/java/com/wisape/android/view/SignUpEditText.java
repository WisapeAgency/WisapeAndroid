package com.wisape.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;

import com.wisape.android.R;

/**
 * The view for sign up or sign in edit text
 *
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpEditText extends LinearLayout{

    public SignUpEditText(Context context) {
        this(context, null, 0);
    }

    public SignUpEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignUpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        View.inflate(context, R.layout.layout_signup_edittext, this);

        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.SignUpEditText);
        try{
            EditText editText = (EditText)findViewById(R.id.signup_edit);
            TextView warningTxtv = (TextView)findViewById(R.id.signup_warning);

            Drawable icon = typed.getDrawable(R.styleable.SignUpEditText_sign_up_icon);
            Drawable action = typed.getDrawable(R.styleable.SignUpEditText_action);
            int iconPaddingStart = typed.getDimensionPixelSize(R.styleable.SignUpEditText_icon_paddingStart, 0);
            int iconPaddingEnd = typed.getDimensionPixelSize(R.styleable.SignUpEditText_icon_paddingEnd, 0);
            int actionPaddingStart = typed.getDimensionPixelSize(R.styleable.SignUpEditText_action_paddingStart, 0);
            int actionPaddingEnd = typed.getDimensionPixelSize(R.styleable.SignUpEditText_action_paddingEnd, 0);

            editText.setCompoundDrawablesWithIntrinsicBounds(icon, null, action, null);
            editText.setCompoundDrawablePadding(iconPaddingEnd);

            Drawable background = typed.getDrawable(R.styleable.SignUpEditText_edit_background);
            if(null != background){
                editText.setBackgroundDrawable(background);
            }

            int textStyle = typed.getResourceId(R.styleable.SignUpEditText_edit_textStyle, -1);
            if(-1 != textStyle){
                editText.setTypeface(null, textStyle);
            }
            editText.setInputType(0);
            int inputType = typed.getInt(R.styleable.SignUpEditText_android_inputType, EditorInfo.TYPE_CLASS_TEXT);
            editText.setInputType(inputType);

            background = typed.getDrawable(R.styleable.SignUpEditText_warning_background);
            if(null != background){
                warningTxtv.setBackgroundDrawable(background);
            }
            textStyle = typed.getResourceId(R.styleable.SignUpEditText_warning_textStyle, -1);
            if(-1 != textStyle){
                warningTxtv.setTypeface(null, textStyle);
            }
        }finally {
            typed.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
