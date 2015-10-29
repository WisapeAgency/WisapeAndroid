package com.wisape.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wisape.android.R;
import com.wisape.android.activity.PhotoSelectorActivity;
import com.wisape.android.model.AppPhotoBucketInfo;
import com.wisape.android.widget.DividerItemDecoration;
import com.wisape.android.widget.PhotoBucketsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoBucketsFragment extends AbsFragment implements PhotoBucketsAdapter.BucketAdapterListener,
        RecyclerView.RecyclerListener {
    private static final String TAG = "PhotoSelector";

    private RecyclerView recyclerView;
    private PhotoBucketsAdapter bucketAdapter;
    private BucketsCallback callback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BucketsCallback) {
            callback = (BucketsCallback) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            Bundle args = getArguments();
            if (null != args) {
                ArrayList<AppPhotoBucketInfo> buckets = args.getParcelableArrayList(PhotoSelectorActivity.EXTRA_BUCKET_LIST);
                int size = null == buckets ? 0 : buckets.size();
                if (0 < size) {
                    bucketAdapter = new PhotoBucketsAdapter(buckets);
                }
            }
        }

        if (null == bucketAdapter) {
            bucketAdapter = new PhotoBucketsAdapter();
        }
        bucketAdapter.setBucketAdapterListener(this);
        setTitle(R.string.photo_bucket_list);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity().getApplicationContext();
        RecyclerView bucketRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_photo_buckets, container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        bucketRecyclerView.setLayoutManager(layoutManager);
        bucketRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST,true));
        bucketRecyclerView.setAdapter(bucketAdapter);
        bucketRecyclerView.setRecyclerListener(this);
        recyclerView = bucketRecyclerView;
        return bucketRecyclerView;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        holder.itemView.setOnClickListener(null);
        Log.d(TAG, "#onViewRecycled __" + holder.itemView.hashCode() + "__");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.photo_backets, menu);
    }

    public void updateData(List<AppPhotoBucketInfo> buckets) {
        if (isDetached()) {
            return;
        }
        bucketAdapter.update(buckets);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (null != bucketAdapter) {
            bucketAdapter.destroy();
            bucketAdapter = null;
        }
        callback = null;
        recyclerView.setRecyclerListener(null);
        recyclerView = null;
    }

    @Override
    public void onBucketSelected(AppPhotoBucketInfo bucket) {
        if (null != callback) {
            callback.onNewBucketSelected(bucket);
        }
    }

    public interface BucketsCallback {
        void onNewBucketSelected(AppPhotoBucketInfo bucket);
    }
}
