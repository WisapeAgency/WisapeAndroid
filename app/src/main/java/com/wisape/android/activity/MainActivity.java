package com.wisape.android.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.wisape.android.R;
import com.wisape.android.common.DynamicBroadcastReceiver;
import com.wisape.android.fragment.MainMenuFragment;
import com.wisape.android.model.UserInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Duke
 */
public class MainActivity extends BaseActivity implements DrawerLayout.DrawerListener, MainMenuFragment.UserCallback,
        DynamicBroadcastReceiver.OnDynamicBroadcastReceiverListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRA_USER_INFO = "_user_info";

    @InjectView(R.id.drawer)
    DrawerLayout drawer;

    public static void launch(Activity activity, UserInfo user, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.putExtra(EXTRA_USER_INFO, user);
        if(-1 == requestCode){
            activity.startActivity(intent);
            activity.finish();
        }else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private UserInfo user;
    private LocalBroadcastManager localBroadcastManager;
    private DynamicBroadcastReceiver localReceiver;

    @Override
    public void onReceiveBroadcast(Context context, Intent intent) {
        if(isDestroyed()){
            return;
        }

        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return;
        }

        if(UserProfileActivity.ACTION_PROFILE_UPDATED.equals(action)){
            UserInfo newUser = intent.getParcelableExtra(EXTRA_USER_INFO);
            user = newUser;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args;
        if(null == savedInstanceState){
            args = getIntent().getExtras();
        }else{
            args = savedInstanceState;
        }
        user = args.getParcelable(EXTRA_USER_INFO);
        if(null == user && 0 < user.user_id){
            SignUpActivity.launch(this, -1);
            return;
        }

        Log.d(TAG, "#onCreate this:" + hashCode() + ", user:" + user.toString());
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        initStyle();
        drawer.setDrawerListener(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localReceiver = new DynamicBroadcastReceiver(this);
        localBroadcastManager.registerReceiver(localReceiver, new IntentFilter(UserProfileActivity.ACTION_PROFILE_UPDATED));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER_INFO, user);
    }

    private void initStyle() {
        drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public UserInfo getUserInfo() {
        return user;
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

    public final void openOrCloseMainMenu(){
        if(null != drawer){
            if(drawer.isDrawerOpen(Gravity.LEFT)){
                drawer.closeDrawer(Gravity.LEFT);
            }else{
                drawer.openDrawer(Gravity.LEFT);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(Gravity.LEFT)){
            openOrCloseMainMenu();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(null != drawer){
            drawer.setOnDragListener(null);
            drawer = null;
        }

        if(null != localBroadcastManager){
            localBroadcastManager.unregisterReceiver(localReceiver);
            localReceiver.destroy();
            localReceiver = null;
            localBroadcastManager = null;
        }
        user = null;
    }
}
