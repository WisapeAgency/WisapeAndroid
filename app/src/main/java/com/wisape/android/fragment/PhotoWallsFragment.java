package com.wisape.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wisape.android.R;
import com.wisape.android.common.PhotoSelector;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.bean.PhotoBucketInfo;
import com.wisape.android.widget.PhotoWallAdapter;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoWallsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Message>{
    private static final String TAG = PhotoWallsFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;

    public static final String EXTRA_BUCKET_ID = "extra_bucket_id";

    private PhotoWallAdapter adapter;
    private long bucketId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras;
        if(null != savedInstanceState){
            extras = savedInstanceState;
        }else{
            extras = getArguments();
        }
        bucketId = extras.getLong(EXTRA_BUCKET_ID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_BUCKET_ID, bucketId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView view = (RecyclerView)inflater.inflate(R.layout.fragment_photo_walls, container, false);
        final Context context = getActivity().getApplicationContext();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        view.setLayoutManager(layoutManager);
        adapter = new PhotoWallAdapter();
        view.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        if(LOADER_ID != id){
            return null;
        }

        return new AsyncTaskLoader<Message>(getActivity().getApplicationContext()) {

            @Override
            public Message loadInBackground() {
                final Context context = getContext();
                PhotoSelector<AppPhotoInfo, PhotoBucketInfo> selector = PhotoSelector.instance(AppPhotoInfo.class, PhotoBucketInfo.class);
                AppPhotoInfo[] photos;
                Message msg = Message.obtain();
                try {
                    if(0 == bucketId){
                        photos = selector.acquireAllPhotos(context);
                    }else{
                        photos = selector.acquirePhotos(context, bucketId);
                    }

                    int size = (null == photos ? 0 : photos.length);

                    AppPhotoInfo[] newDatas = new AppPhotoInfo[size + 1];
                    newDatas[0] = new AppPhotoInfo(AppPhotoInfo.VIEW_TYPE_CAMERA);
                    if(0 < size){
                        System.arraycopy(photos, 0, newDatas, 1, size);
                    }
                    photos = newDatas;
                    msg.what = 1;
                    msg.obj = photos;
                } catch (java.lang.InstantiationException e) {
                    Log.e(TAG, "", e);
                    msg.what = -1;
                    msg.obj = e;
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "", e);
                    msg.what = -1;
                    msg.obj = e;
                }
                return msg;
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        if(isDetached() || null == data){
            return;
        }

        try {
            if(1 == data.what){
                AppPhotoInfo[] photos = (AppPhotoInfo[]) data.obj;
                Log.d(TAG, "# onLoadFinished, photos:" + photos.length);
                adapter.update(photos);
            }
        }finally {
            data.recycle();
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        //do nothing
    }

    @Override
    public void onDetach() {
        super.onDetach();
        adapter = null;
    }
}
