package com.wisape.android.widget;

import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wisape.android.R;
import com.wisape.android.model.AppPhotoBucketInfo;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.view.TextView;

import java.util.List;

/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoBucketsAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener{
    private static final String TAG = PhotoBucketsAdapter.class.getSimpleName();
    private List<AppPhotoBucketInfo> buckets;
    private BucketAdapterListener adapterListener;

    public PhotoBucketsAdapter(){}

    public PhotoBucketsAdapter(List<AppPhotoBucketInfo> buckets){
        this.buckets = buckets;
    }

    public void setBucketAdapterListener(BucketAdapterListener adapterListener){
        this.adapterListener = adapterListener;
    }

    public void update(List<AppPhotoBucketInfo> buckets){
        List<AppPhotoBucketInfo> oldBuckets = this.buckets;
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
        View itemView = holder.itemView;
        itemView.setOnClickListener(this);
        itemView.setTag(position);
        TextView titleTxtv = (TextView) itemView.findViewById(R.id.txtv_bucket_title);
        TextView messageTxtv = (TextView) itemView.findViewById(R.id.txtv_bucket_message);
        final AppPhotoBucketInfo bucket = buckets.get(position);
        titleTxtv.setText(bucket.displayName);
        messageTxtv.setText(Integer.toString(bucket.childrenCount));

        final SimpleDraweeView thumb = (SimpleDraweeView) itemView.findViewById(R.id.imgv_bucket_thumb);
        final ViewGroup.LayoutParams layoutParams = thumb.getLayoutParams();
        final int itemWidth = layoutParams.width;
        final int itemHeight = layoutParams.height;
        Uri uri = PhotoProvider.getBucketThumbUri(bucket.id);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(itemWidth, itemHeight))
                .setProgressiveRenderingEnabled(true)
                .setLocalThumbnailPreviewsEnabled(true)
                .setPostprocessor(new PngResizePostprocessor(itemWidth, itemHeight))
                .setAutoRotateEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(thumb.getController())
                .setImageRequest(request)
                .setAutoPlayAnimations(false)
                .build();
        thumb.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
        thumb.setController(controller);

        final SimpleDraweeView selectedView = (SimpleDraweeView) itemView.findViewById(R.id.imgv_bucket_selected);
        boolean selected = bucket.isSelected();
        if(selected){
            bucket.setSelected(false);
            selectedView.setVisibility(View.VISIBLE);
        }else if(View.VISIBLE == selectedView.getVisibility()){
            selectedView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return null == buckets ? 0 : buckets.size();
    }

    @Override
    public void onClick(View view) {
        if(null == adapterListener){
            return;
        }

        int position = (Integer) view.getTag();

        AppPhotoBucketInfo bucket = buckets.get(position);
        bucket.setSelected(true);
        notifyItemChanged(position);

        AppPhotoBucketInfo copyBucket;
        try {
            copyBucket = (AppPhotoBucketInfo)bucket.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "", e);
            copyBucket = bucket;
        }
        adapterListener.onBucketSelected(copyBucket);
    }

    public void destroy(){
        if(null != buckets){
            buckets.clear();
            buckets = null;
        }
        adapterListener = null;
    }

    public interface AppBucketItemData{
        boolean isSelected();
        void setSelected(boolean selected);
    }

    public interface BucketAdapterListener{
        void onBucketSelected(AppPhotoBucketInfo bucket);
    }
}
