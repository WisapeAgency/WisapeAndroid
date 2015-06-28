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
import com.wisape.android.bean.PhotoBucketInfo;
import com.wisape.android.bean.PhotoInfo;
import com.wisape.android.common.PhotoSelector;
import com.wisape.android.widget.PhotoBucketAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoBucketsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Message>{
    private static final String TAG = "PhotoSelector";
    private static final int LOADER_ID = 1;
    private static final long ALL_IN_ONE_BUCKET_ID = 0;

    private PhotoBucketAdapter bucketAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity().getApplicationContext();
        RecyclerView bucketRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_photo_buckets,container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        bucketRecyclerView.setLayoutManager(layoutManager);
        bucketAdapter = new PhotoBucketAdapter();
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
                    List<PhotoBucketInfo> buckets = PhotoSelector.instance(PhotoInfo.class, PhotoBucketInfo.class).acquireBuckets(context);
                    int size = (null == buckets ? 0 : buckets.size());
                    if(0 != size){
                        PhotoBucketInfo allInOneBucket = new PhotoBucketInfo();
                        allInOneBucket.id = ALL_IN_ONE_BUCKET_ID;
                        allInOneBucket.displayName = context.getString(R.string.photo_bucket_all);
                        int total = 0;
                        for(PhotoBucketInfo bucket : buckets){
                            total += bucket.childrenCount;
                        }
                        allInOneBucket.childrenCount = total;
                        List<PhotoBucketInfo> newBuckets = new ArrayList(size + 1);
                        newBuckets.add(allInOneBucket);
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
                List<PhotoBucketInfo> buckets = (List<PhotoBucketInfo>) data.obj;
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

    @Override
    public void onDetach() {
        super.onDetach();
        bucketAdapter = null;
    }
}
