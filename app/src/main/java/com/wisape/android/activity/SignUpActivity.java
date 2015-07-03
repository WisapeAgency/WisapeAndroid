package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wisape.android.R;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class SignUpActivity extends VolleyActivity{

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), SignUpActivity.class);
        if(-1 == requestCode){
            activity.startActivity(intent);
            activity.finish();
        }else{
            activity.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }
}
