package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.model.StoryGestureInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * 手势选择activity
 * Created by huangmeng on 15/9/2.
 */
public class GestorChoiceActivity extends BaseActivity {

    public static final int REQUEST_CODE_SLIDE_MOTION = 0x02;
    public static final String EXTRO_SELECT_SLIDE = "extro_select_slide";

    public static final int GESTOR_LEFT_ID = 1;
    public static final int GETSORY_TOP_ID = 2;

    private TextView selectTextView;

    @InjectView(R.id.img_slide)
    protected ImageView imgSlide;

    @InjectView(R.id.slide_top)
    protected TextView slideTop;
    @InjectView(R.id.slide_left)
    protected TextView slideLeft;

    protected StoryGestureInfo storyGestureInfo;


    public static void launch(Activity activity, StoryGestureInfo select, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), GestorChoiceActivity.class);
        if(null != select){
            intent.putExtra(EXTRO_SELECT_SLIDE, select);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestor_choice);
        ButterKnife.inject(this);
        selectTextView = slideTop;
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            storyGestureInfo = extras.getParcelable(EXTRO_SELECT_SLIDE);
            if(GESTOR_LEFT_ID == storyGestureInfo.id){
                cancleSelected();
                selectTextView = slideLeft;
                setSelected();
                imgSlide.setBackgroundResource(R.mipmap.icon_sile_left);
            }
        }else{
            storyGestureInfo = new StoryGestureInfo();
            storyGestureInfo.id = GETSORY_TOP_ID;
            storyGestureInfo.name = selectTextView.getText().toString();
        }
    }


    @OnClick(R.id.slide_top)
    @SuppressWarnings("unused")
    protected void onSlideTopClick() {
        if (selectTextView != slideTop) {
            cancleSelected();
            selectTextView = slideTop;
            setSelected();
            imgSlide.setBackgroundResource(R.mipmap.icon_slide_top);
            storyGestureInfo.id = GETSORY_TOP_ID;
        }
    }

    @OnClick(R.id.slide_left)
    @SuppressWarnings("unused")
    protected void onSlideLeftClick() {
        if (selectTextView != slideLeft) {
            cancleSelected();
            selectTextView = slideLeft;
            setSelected();
            imgSlide.setBackgroundResource(R.mipmap.icon_sile_left);
            storyGestureInfo.id = GESTOR_LEFT_ID;
        }
    }

    private void cancleSelected() {
        selectTextView.setTextColor(getResources().getColor(R.color.app_sixth));
        selectTextView.setCompoundDrawables(null, null, null, null);
    }

    private void setSelected() {
        selectTextView.setTextColor(getResources().getColor(R.color.app_btn_green_normal));
        Drawable drawable = getResources().getDrawable(R.drawable.icon_selected_flag);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        selectTextView.setCompoundDrawables(null, null, drawable, null);
        storyGestureInfo.name = selectTextView.getText().toString();
    }

    @Override
    protected boolean onBackNavigation() {
        Intent intent = new Intent();
        intent.putExtra(EXTRO_SELECT_SLIDE, storyGestureInfo);
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }
}
