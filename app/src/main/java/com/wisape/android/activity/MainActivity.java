package com.wisape.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wisape.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Duke
 */
public class MainActivity extends BaseCompatActivity {

    @InjectView(R.id.main_drawer)
    DrawerLayout mMainDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initStyle();
        initEvents();
    }

    private void initStyle() {
        mMainDrawer.setScrimColor(Color.TRANSPARENT);
        mMainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void initEvents() {
        mMainDrawer.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mMainDrawer.getChildAt(0);

                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;
                DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) drawerView.getLayoutParams();
                if (Gravity.LEFT == lp.gravity) {
                    float leftScale = 1 - 0.3f * scale;
                    drawerView.setScaleX(leftScale);
                    drawerView.setScaleY(leftScale);
                    drawerView.setAlpha(0.6f + 0.4f * (1 - scale));
                    mContent.setTranslationX(drawerView.getMeasuredWidth() * (1 - scale));
                    mContent.setPivotX(0);
                    mContent.setPivotY(mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    mContent.setScaleX(rightScale);
                    mContent.setScaleY(rightScale);
                } else {
                    mContent.setTranslationX(-drawerView.getMeasuredWidth() * slideOffset);
                    mContent.setPivotX(mContent.getMeasuredWidth());
                    mContent.setPivotY(mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    mContent.setScaleX(rightScale);
                    mContent.setScaleY(rightScale);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Photo");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        PhotoSelectorActivity.launch(this, 10);
        return true;
    }


    public final void openOrCloseMainMenu(){
        if(null != mMainDrawer){
            if(mMainDrawer.isDrawerOpen(Gravity.LEFT)){
                mMainDrawer.closeDrawer(Gravity.LEFT);
            }else{
                mMainDrawer.openDrawer(Gravity.LEFT);
            }
        }
    }
}
