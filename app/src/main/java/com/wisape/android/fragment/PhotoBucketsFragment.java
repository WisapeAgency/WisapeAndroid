package com.wisape.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wisape.android.R;
import com.wisape.android.activity.PhotoSelectorActivity;
import com.wisape.android.bean.AppPhotoBucketInfo;
import com.wisape.android.widget.PhotoBucketsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoBucketsFragment extends BaseFragment{
    private static final String TAG = "PhotoSelector";

    private PhotoBucketsAdapter bucketAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null == savedInstanceState){
            Bundle args = getArguments();
            if(null != args){
                ArrayList<AppPhotoBucketInfo> buckets = args.getParcelableArrayList(PhotoSelectorActivity.EXTRA_BUCKET_LIST);
                int size = null == buckets ? 0 : buckets.size();
                if(0 < size){
                    bucketAdapter = new PhotoBucketsAdapter(buckets);
                }
            }
        }

        if(null == bucketAdapter){
            bucketAdapter = new PhotoBucketsAdapter();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = getActivity().getApplicationContext();
        RecyclerView bucketRecyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_photo_buckets,container, false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        bucketRecyclerView.setLayoutManager(layoutManager);
        bucketRecyclerView.setAdapter(bucketAdapter);
        return bucketRecyclerView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_backets, menu);
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
