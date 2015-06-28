package com.wisape.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wisape.android.R;
import com.wisape.android.bean.AppPhotoBucketInfo;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.common.PhotoSelector;
import com.wisape.android.widget.PhotoBucketsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoBucketsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Message>{
    private static final String TAG = "PhotoSelector";
    private static final int LOADER_ID = 1;
    private static final long ALL_IN_ONE_BUCKET_ID = 0;

    private PhotoBucketsAdapter bucketAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity().getApplicationContext();
        RecyclerView bucketRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_photo_buckets,container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        bucketRecyclerView.setLayoutManager(layoutManager);
        bucketAdapter = new PhotoBucketsAdapter();
        bucketRecyclerView.setAdapter(bucketAdapter);
        return bucketRecyclerView;
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID != id) {
            return null;
        }

        return new AsyncTaskLoader<Message>(getActivity().getApplicationContext()) {
            @Override
            public Message loadInBackground() {
                Message msg = Message.obtain();
                try {
                    final Context context = getContext();
                    List<AppPhotoBucketInfo> buckets = PhotoSelector.instance(AppPhotoInfo.class, AppPhotoBucketInfo.class).acquireBuckets(context);
                    int size = (null == buckets ? 0 : buckets.size());
                    if(0 != size){
                        AppPhotoBucketInfo allInBucket = new AppPhotoBucketInfo();
                        allInBucket.id = ALL_IN_ONE_BUCKET_ID;
                        allInBucket.displayName = context.getString(R.string.photo_bucket_all);
                        int total = 0;
                        for(AppPhotoBucketInfo bucket : buckets){
                            total += bucket.childrenCount;
                        }
                        allInBucket.childrenCount = total;
                        List<AppPhotoBucketInfo> newBuckets = new ArrayList(size + 1);
                        newBuckets.add(allInBucket);
                        newBuckets.addAll(buckets);
                        buckets.clear();
                        msg.obj = newBuckets;
                    }
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
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
            if (null != data.obj) {
                List<AppPhotoBucketInfo> buckets = (List<AppPhotoBucketInfo>) data.obj;
                bucketAdapter.update(buckets);
            }
        } finally {
            data.recycle();
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        //do nothing
    }

    public void updateData(List<AppPhotoBucketInfo> buckets){
        if(isDetached()){
            return;
        }

        bucketAdapter.update(buckets);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bucketAdapter = null;
    }
}
