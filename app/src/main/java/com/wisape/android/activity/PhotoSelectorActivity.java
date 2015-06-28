package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.wisape.android.fragment.PhotoWallsFragment;

/**
 * The class support UI where you can select some photo that from Android System or open camera to take a photo.
 * if you want to receive some photo that be selected by USER from this UI, you can do so as following:
 * 1)implementing onActivityResult method in Activity or Fragment.
 * 2)fetching photos from Intent.
 * <p/>
 * Created by LeiGuoting on 10/6/15.
 */
public class PhotoSelectorActivity extends BaseCompatActivity{
    private static final String TAG = "PhotoSelector";

    public static void launch(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), PhotoSelectorActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), PhotoSelectorActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null == savedInstanceState){
            Fragment fragment = new PhotoWallsFragment();
            Bundle args = new Bundle();
            args.putLong(PhotoWallsFragment.EXTRA_BUCKET_ID, 0);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commitAllowingStateLoss();
        }
    }
}
