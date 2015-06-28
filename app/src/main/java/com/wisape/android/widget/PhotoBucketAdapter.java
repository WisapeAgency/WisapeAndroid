package com.wisape.android.widget;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wisape.android.R;
import com.wisape.android.bean.PhotoBucketInfo;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.view.TextView;

import java.util.List;

/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoBucketAdapter extends RecyclerView.Adapter<RecyclerHolder>{
    private static final String TAG = PhotoBucketAdapter.class.getSimpleName();
    private List<PhotoBucketInfo> buckets;

    public void update(List<PhotoBucketInfo> buckets){
        List<PhotoBucketInfo> oldBuckets = this.buckets;
        this.buckets = buckets;
        notifyDataSetChanged();
        if(null != oldBuckets){
            oldBuckets.clear();
        }
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_photo_bucket, parent, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        View parent = holder.itemView;
        TextView titleTxtv = (TextView) parent.findViewById(R.id.txtv_bucket_title);
        TextView messageTxtv = (TextView) parent.findViewById(R.id.txtv_bucket_message);
        final PhotoBucketInfo bucket = buckets.get(position);
        titleTxtv.setText(bucket.displayName);
        messageTxtv.setText(Integer.toString(bucket.childrenCount));

        final SimpleDraweeView icon = (SimpleDraweeView) parent.findViewById(R.id.imgv_bucket_thumb);
        Uri uri = PhotoProvider.getBucketThumbUri(bucket.id);
        Log.d(TAG, "#onBindViewHolder uri:" + uri.toString());
        icon.setImageURI(uri);
    }

    @Override
    public int getItemCount() {
        return null == buckets ? 0 : buckets.size();
    }
}
