package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.widget.PhotoWallsAdapter;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoWallsFragment extends BaseFragment{
    private static final String TAG = PhotoWallsFragment.class.getSimpleName();
    private PhotoWallsAdapter adapter;
    private WallsCallback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof WallsCallback){
            callback = (WallsCallback) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.fragment_photo_walls, container, false);
        final Context context = getActivity().getApplicationContext();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        view.setLayoutManager(layoutManager);
        adapter = new PhotoWallsAdapter();
        view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.photo_walls, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(R.id.switch_to_buckets == item.getItemId()){
            if(null != callback){
                callback.onSwitchToBuckets();
            }
            return true;
        }
        return false;
    }

    public void updateData(AppPhotoInfo[] photos){
        if (isDetached() || null == photos) {
            return;
        }
        adapter.update(photos);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
        callback = null;
    }

    public interface WallsCallback{
        void onSwitchToBuckets();
    }
}
