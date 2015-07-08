package com.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Editable TextView
 *
 * <p>
 *     <b>XML attributes</b>
 * </p>
 * See {@link R.styleable#EditableTextView EditableTextView Attributes}
 *
 * @attr ref {@link R.styleable#EditableTextView_editable_background}
 * @attr ref {@link R.styleable#EditableTextView_readonly_background}
 * @attr ref {@link R.styleable#EditableTextView_editable_textStyle}
 * @attr ref {@link R.styleable#EditableTextView_readonly_textStyle}
 *
 * Created by LeiGuoting on 8/7/15.
 */
public class EditableTextView extends AppCompatEditText implements GestureDetector.OnGestureListener{
    private static final String TAG = EditableTextView.class.getSimpleName();

    private boolean editable;
    private OnEditableModeChangedListener modeChangedListener;
    private Drawable editableBackground;
    private Drawable readonlyBackground;
    private int editableTextStyle;
    private int readonlyTextStyle;

    private GestureDetector gestureDetector;

    private KeyListener keyListener;

    public EditableTextView(Context context) {
        this(context, null);
    }

    public EditableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public EditableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditableTextView);
        try {
            editable = typedArray.getBoolean(R.styleable.EditableTextView_etv_editable, true);
            editableBackground = typedArray.getDrawable(R.styleable.EditableTextView_editable_background);
            readonlyBackground = typedArray.getDrawable(R.styleable.EditableTextView_readonly_background);
            editableTextStyle = typedArray.getResourceId(R.styleable.EditableTextView_editable_textStyle, 0);
            readonlyTextStyle = typedArray.getResourceId(R.styleable.EditableTextView_readonly_textStyle, 0);
        }finally {
            typedArray.recycle();
        }

        gestureDetector = new GestureDetector(context, this);

        keyListener = getKeyListener();
        if(null == keyListener){
            Log.d(TAG, "#init keyListener == null");
        }
        if(null == editableBackground){
            editableBackground = getBackground();
        }
        setEditable(editable);
    }

    /*
    @Override
    protected boolean getDefaultEditable() {
        Log.d(TAG, "#getDefaultEditable editable:" + editable);
        return editable;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }
    */

    public void setEditable(boolean editable){
        this.editable = editable;
        setKeyListener(editable ? keyListener : null);
        setCursorVisible(editable);
        if(editable){
            requestFocus();
            setBackgroundDrawable(editableBackground);
        }else{
            clearFocus();
            setBackgroundDrawable(readonlyBackground);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, editable ? BufferType.EDITABLE : BufferType.NORMAL);
    }

    public void setOnEditableModeChangedListener(OnEditableModeChangedListener listener){
        modeChangedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        //do nothing
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        //do nothing
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        //do nothing
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //do nothing
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        final boolean editable = this.editable;
        if(!editable){
            setEditable(!editable);
            if(null != modeChangedListener){
                modeChangedListener.onEditableModeChanged(this, editable);
            }
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //do nothing
        return false;
    }

    public interface OnEditableModeChangedListener{
        void onEditableModeChanged(EditableTextView view, boolean editable);
    }
}
