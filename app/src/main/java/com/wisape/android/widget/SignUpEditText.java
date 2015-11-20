package com.wisape.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisape.android.R;

/**
 * The view for sign up or sign in edit text
 *
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpEditText extends LinearLayout implements View.OnFocusChangeListener{

    private static final String TAG = SignUpEditText.class.getSimpleName();

    private InnerEditText editText;
    private TextView warningTxtv;
    private boolean showError;

    public SignUpEditText(Context context) {
        this(context, null, 0);
    }

    public SignUpEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public SignUpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        View.inflate(context, R.layout.layout_signup_edittext, this);

        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.SignUpEditText);
        try{
            showError = typed.getBoolean(R.styleable.SignUpEditText_show_error, false);

            editText = (InnerEditText)findViewById(R.id.edit_text);
            warningTxtv = (TextView)findViewById(R.id.warning_text);
            warningTxtv.setVisibility(showError ? INVISIBLE : GONE);

            Drawable icon = typed.getDrawable(R.styleable.SignUpEditText_sign_up_icon);
            Drawable action = typed.getDrawable(R.styleable.SignUpEditText_action);
            int iconPaddingStart = typed.getDimensionPixelSize(R.styleable.SignUpEditText_icon_paddingStart, 0);
            int iconPaddingEnd = typed.getDimensionPixelSize(R.styleable.SignUpEditText_icon_paddingEnd, 0);
            int actionPaddingStart = typed.getDimensionPixelSize(R.styleable.SignUpEditText_action_paddingStart, 0);
            int actionPaddingEnd = typed.getDimensionPixelSize(R.styleable.SignUpEditText_action_paddingEnd, 0);

            editText.setOnFocusChangeListener(this);
            editText.setIconAndAction(icon, action);
            editText.setCompoundDrawablePadding(iconPaddingEnd > actionPaddingStart ? iconPaddingEnd : actionPaddingStart);
            editText.setPadding(iconPaddingStart, 0, actionPaddingEnd, 0);

            Drawable background = typed.getDrawable(R.styleable.SignUpEditText_edit_background);
            if(null != background){
                editText.setBackground(background);
            }

            int textStyle = typed.getResourceId(R.styleable.SignUpEditText_edit_textStyle, -1);
            if(-1 != textStyle){
                editText.setTextAppearance(context, textStyle);
            }
            editText.setInputType(0);
            int inputType = typed.getInt(R.styleable.SignUpEditText_android_inputType, EditorInfo.TYPE_CLASS_TEXT);
            editText.setInputType(inputType);

            int height = typed.getDimensionPixelSize(R.styleable.SignUpEditText_edit_height, 0);
            if(0 < height){
                editText.setHeight(height);
            }

            if(showError){
                background = typed.getDrawable(R.styleable.SignUpEditText_warning_background);
                if(null != background){
                    warningTxtv.setBackgroundDrawable(background);
                }
                textStyle = typed.getResourceId(R.styleable.SignUpEditText_warning_textStyle, -1);
                if(-1 != textStyle){
                    warningTxtv.setTextAppearance(context, textStyle);
                }

                icon = typed.getDrawable(R.styleable.SignUpEditText_warning_icon);
                if(null != icon){
                    if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT){
                        warningTxtv.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    }else{
                        warningTxtv.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                    }
                }

                iconPaddingEnd = typed.getDimensionPixelSize(R.styleable.SignUpEditText_warning_icon_paddingEnd, 0);
                if(0 < iconPaddingEnd){
                    warningTxtv.setCompoundDrawablePadding(iconPaddingEnd);
                }
                height = typed.getDimensionPixelSize(R.styleable.SignUpEditText_warning_height, 0);
                if(0 < height){
                    warningTxtv.setHeight(height);
                }
            }
        }finally {
            typed.recycle();
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(R.id.edit_text == view.getId() && !hasFocus){
            cleanWarning();

        }
    }

    public void setText(CharSequence text){
        editText.setText(text);
    }

    public String getText(){
        return editText.getText().toString();
    }

    public void setError(CharSequence error){
        setError(error, null);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setError(CharSequence error, Drawable icon){
        if(!showError){
            return;
        }

        warningTxtv.setText(error);
        if(null != icon){
            if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT){
                warningTxtv.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            }else{
                warningTxtv.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }
        warningTxtv.setVisibility(VISIBLE);
        editText.setSelected(true);
    }

    public void setOnActionListener(OnActionListener onActionListener){
        InnerEditText editText = this.editText;
        if(null != editText){
            if(null != onActionListener){
                editText.setOnActionListener(onActionListener, this);
            }else{
                editText.setOnActionListener(null, null);
            }
        }
    }

    public void setInputType(int inputType){
        editText.setInputType(inputType);
    }

    public int getInputType(){
        return editText.getInputType();
    }

    public boolean isPasswordText(){
        int inputType = editText.getInputType();
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD)
                || variation
                == (EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    public boolean isVisiblePasswordInputType() {
        int inputType = editText.getInputType();
        final int variation =
                inputType & (EditorInfo.TYPE_MASK_CLASS | EditorInfo.TYPE_MASK_VARIATION);
        return variation
                == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private void cleanWarning(){
        if(showError && VISIBLE == warningTxtv.getVisibility()){
            warningTxtv.setText(null);
            warningTxtv.setVisibility(INVISIBLE);
            editText.setSelected(false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(null != editText){
            editText.setOnFocusChangeListener(null);
            editText = null;
        }
        warningTxtv = null;
    }

    public static class InnerEditText extends EditText{
        private static final int DRAWABLE_START = 0;
        private static final int DRAWABLE_END = 2;
        private OnActionListener onActionListener;
        private SignUpEditText parent;

        public InnerEditText(Context context) {
            this(context, null);
        }

        public InnerEditText(Context context, AttributeSet attrs) {
            this(context, attrs, android.R.attr.editTextStyle);
        }

        public InnerEditText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void setOnActionListener(OnActionListener onActionListener, SignUpEditText parent){
            this.onActionListener = onActionListener;
            if(null != onActionListener && null == parent){
                throw new IllegalArgumentException("The parent SignUpEditText can not is null when the OnActionListener is not null.");
            }
            this.parent = parent;
        }

        @Override
        @Deprecated
        public final void setCompoundDrawablesWithIntrinsicBounds(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
            super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }

        @Override
        @Deprecated
        public final void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
            super.setCompoundDrawablesRelative(start, top, end, bottom);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void setIconAndAction(Drawable icon, Drawable action){
            if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT){
                super.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, action, null);
            }else{
                super.setCompoundDrawablesWithIntrinsicBounds(icon, null, action, null);
            }
        }

        public void setBackground(Drawable background) {
            setBackgroundDrawable(background);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public boolean onTouchEvent(MotionEvent event) {
            if(MotionEvent.ACTION_DOWN == event.getAction()){
                Drawable action = getAction();
                if(null != action){
                    Rect bounds = action.getBounds();
                    float x = event.getRawX();
                    float y = event.getRawY();
                    //Log.d(TAG, "#onTouchEvent x:" + x + ", y:" + y + ", Action's bounds:" + bounds.toString());
                    if(isActionBounds(bounds, x, y)){
                        if(null != onActionListener){
                            parent.cleanWarning();
                            onActionListener.onActionClicked(parent);
                        }
                    }
                }
            }
            return super.onTouchEvent(event);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public Drawable getAction(){
            Drawable action;
            if(Build.VERSION_CODES.JELLY_BEAN_MR1 <= Build.VERSION.SDK_INT){
                action = getCompoundDrawablesRelative()[DRAWABLE_END];
            }else{
                action = getCompoundDrawables()[DRAWABLE_END];
            }
            return action;
        }

        private boolean isActionBounds(Rect actionBounds, float x, float y){
            return x >= getRight() - actionBounds.width();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            parent = null;
        }
    }

    public interface OnActionListener{
        void onActionClicked(View view);
    }
}
