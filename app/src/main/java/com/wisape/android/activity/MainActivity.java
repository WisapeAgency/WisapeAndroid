package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;

import com.freshdesk.mobihelp.Mobihelp;
import com.freshdesk.mobihelp.MobihelpConfig;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Duke
 */
public class MainActivity extends BaseActivity implements DrawerLayout.DrawerListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_USER_INFO = "_user_info";

    @InjectView(R.id.drawer)
    DrawerLayout drawer;

    public static void launch(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mobihelp.init(this, new MobihelpConfig("https://wisapeagency.freshdesk.com", "wisape-1-793b05d4f430fb3889880016a735ed46", "b27a8d9aa40cb8c9e925ae964f6025c4c56c7eb6"));
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initStyle();
        drawer.setDrawerListener(this);
    }


//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelable(EXTRA_USER_INFO, WisapeApplication.getInstance().getUserInfo());
//    }

    private void initStyle() {
        drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /*
     * DrawerLayout.DrawerListener
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        View contentView = drawer.findViewById(R.id.main_view);
        float scale = 1 - slideOffset;
        float rightScale = 0.8f + scale * 0.2f;
        DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) drawerView.getLayoutParams();
        if (Gravity.LEFT == lp.gravity) {
            float leftScale = 1 - 0.3f * scale;
            drawerView.setScaleX(leftScale);
            drawerView.setScaleY(leftScale);
            drawerView.setAlpha(0.6f + 0.4f * (1 - scale));

            contentView.setTranslationX(drawerView.getMeasuredWidth() * (1 - scale));
            contentView.setPivotX(0);
            contentView.setPivotY(contentView.getMeasuredHeight() / 2);
            contentView.invalidate();
            contentView.setScaleX(rightScale);
            contentView.setScaleY(rightScale);
        } else {
            contentView.setTranslationX(-drawerView.getMeasuredWidth() * slideOffset);
            contentView.setPivotX(contentView.getMeasuredWidth());
            contentView.setPivotY(contentView.getMeasuredHeight() / 2);
            contentView.invalidate();
            contentView.setScaleX(rightScale);
            contentView.setScaleY(rightScale);
        }
    }

    /*
    * DrawerLayout.DrawerListener
    */
    @Override
    public void onDrawerOpened(View drawerView) {
    }

    /*
    * DrawerLayout.DrawerListener
    */
    @Override
    public void onDrawerClosed(View drawerView) {
    }

    /*
    * DrawerLayout.DrawerListener
    */
    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public final void openOrCloseMainMenu() {
        if (null != drawer) {
            if (drawer.isDrawerOpen(Gravity.LEFT)) {
                drawer.closeDrawer(Gravity.LEFT);
            } else {
                drawer.openDrawer(Gravity.LEFT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            openOrCloseMainMenu();
        } else {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setAction(Intent.ACTION_MAIN);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if (null != drawer) {
            drawer.setOnDragListener(null);
            drawer = null;
        }
    }
}
