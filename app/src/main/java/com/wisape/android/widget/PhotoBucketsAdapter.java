package com.wisape.android.widget;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wisape.android.R;
import com.wisape.android.model.AppPhotoBucketInfo;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.view.TextView;

import java.util.List;

/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoBucketsAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener {
    private static final String TAG = PhotoBucketsAdapter.class.getSimpleName();
    private List<AppPhotoBucketInfo> buckets;
    private BucketAdapterListener adapterListener;
    private Context context;

    public PhotoBucketsAdapter() {
    }

    public PhotoBucketsAdapter(List<AppPhotoBucketInfo> buckets) {
        this.buckets = buckets;
    }

    public void setBucketAdapterListener(BucketAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public void update(List<AppPhotoBucketInfo> buckets) {
        List<AppPhotoBucketInfo> oldBuckets = this.buckets;
        this.buckets = buckets;
        notifyDataSetChanged();
        if (null != oldBuckets) {
            oldBuckets.clear();
        }
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_photo_bucket, parent, false);
        return new RecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        View itemView = holder.itemView;
        itemView.setOnClickListener(this);
        itemView.setTag(position);
        TextView titleTxtv = (TextView) itemView.findViewById(R.id.txtv_bucket_title);
        TextView messageTxtv = (TextView) itemView.findViewById(R.id.txtv_bucket_message);
        final AppPhotoBucketInfo bucket = buckets.get(position);
        titleTxtv.setText(bucket.displayName);
        messageTxtv.setText(Integer.toString(bucket.childrenCount));
        final ImageView thumb = (ImageView) itemView.findViewById(R.id.imgv_bucket_thumb);
        final ViewGroup.LayoutParams layoutParams = thumb.getLayoutParams();
        final int itemWidth = layoutParams.width;
        final int itemHeight = layoutParams.height;
        Uri uri = PhotoProvider.getBucketThumbUri(bucket.id);
        Glide.with(context).load(uri)
                .error(R.mipmap.no_pic)
                .centerCrop()
                .override(itemWidth,itemHeight)
                .into(thumb);
    }

    @Override
    public int getItemCount() {
        return null == buckets ? 0 : buckets.size();
    }

    @Override
    public void onClick(View view) {
        if (null == adapterListener) {
            return;
        }

        int position = (Integer) view.getTag();

        AppPhotoBucketInfo bucket = buckets.get(position);
        bucket.setSelected(true);
        notifyItemChanged(position);

        AppPhotoBucketInfo copyBucket;
        try {
            copyBucket = (AppPhotoBucketInfo) bucket.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "", e);
            copyBucket = bucket;
        }
        adapterListener.onBucketSelected(copyBucket);
    }

    public void destroy() {
        if (null != buckets) {
            buckets.clear();
            buckets = null;
        }
        adapterListener = null;
    }

    public interface AppBucketItemData {
        boolean isSelected();

        void setSelected(boolean selected);
    }

    public interface BucketAdapterListener {
        void onBucketSelected(AppPhotoBucketInfo bucket);
    }
}
