package com.wisape.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.model.UserInfo;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.wisape.android.activity.MainActivity.EXTRA_USER_INFO;

/**
 * Created by LeiGuoting on 6/7/15.
 */
public class UserProfileActivity extends BaseActivity implements View.OnTouchListener{
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    public static final int REQUEST_CODE_PROFILE = 0x102;

    public static void launch(Fragment fragment, UserInfo user, int requestCode){
        fragment.startActivityForResult(getIntent(fragment.getActivity().getApplicationContext(), user), requestCode);
    }

    public static Intent getIntent(Context context, UserInfo user){
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(EXTRA_USER_INFO, user);
        return intent;
    }

    private UserInfo user;

    @InjectView(R.id.user_profile_icon)
    protected SimpleDraweeView iconView;

    @InjectView(R.id.user_profile_name_edit)
    protected EditText nameEdit;

    @InjectView(R.id.user_profile_email_edit)
    protected EditText emailEdit;

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

        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);


        nameEdit.setEnabled(false);
        emailEdit.setEnabled(false);
        nameEdit.setOnTouchListener(this);
        emailEdit.setOnTouchListener(this);
        nameEdit.setText(user.nick_name);
        emailEdit.setText(user.user_email);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_USER_INFO, user);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.d(TAG, "#onTouch view:" + view.getId());
        return true;
    }

    @OnClick(R.id.user_profile_name_edit)
    @SuppressWarnings("unused")
    protected void doNameEditClicked(){
        Log.d(TAG, "#doNameEditClicked ___");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
        if(null != nameEdit){
            nameEdit.setOnTouchListener(null);
            emailEdit = null;
        }

        if(null != emailEdit){
            emailEdit.setOnTouchListener(null);
            emailEdit = null;
        }
        user = null;
    }
}
