package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.wisape.android.R;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.widget.PhotoWallsAdapter;

import java.io.File;

import static com.wisape.android.bean.AppPhotoInfo.VIEW_TYPE_CAMERA;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoWallsFragment extends BaseFragment implements PhotoWallsAdapter.PhotoItemListener {
    private static final String TAG = PhotoWallsFragment.class.getSimpleName();
    private static final int REQUEST_CODE_CAMERA = 2;
    private PhotoWallsAdapter adapter;
    private WallsCallback callback;
    private Uri cameraImageUri;

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
        adapter.setPhotoItemListener(this);
        view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onItemSelected(int type, AppPhotoInfo photo) {
        if(VIEW_TYPE_CAMERA == type){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            cameraImageUri = Uri.fromFile(photoFile);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        }else{
            if(null != callback){
                callback.onPhotoSelected(Uri.parse(photo.data));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_CODE_CAMERA == requestCode){
            if(resultCode == Activity.RESULT_OK){
                if(null != callback){
                    Uri uri = cameraImageUri;
                    callback.onPhotoSelected(uri);
                    cameraImageUri = null;
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
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
        if(null != adapter){
            adapter.destroy();
            adapter = null;
        }
        callback = null;
    }

    public interface WallsCallback{
        void onSwitchToBuckets();
        void onPhotoSelected(Uri uri);
    }
}
