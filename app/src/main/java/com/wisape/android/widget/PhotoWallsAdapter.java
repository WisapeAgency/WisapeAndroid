package com.wisape.android.widget;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.wisape.android.R;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.util.FrescoUriUtils;

import static com.wisape.android.bean.AppPhotoInfo.VIEW_TYPE_CAMERA;
import static com.wisape.android.bean.AppPhotoInfo.VIEW_TYPE_PHOTO;
/**
 * Created by LeiGuoting on 10/6/15.
 */
public class PhotoWallsAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener{
    private static final String TAG = PhotoWallsAdapter.class.getSimpleName();
    private AppPhotoInfo[] datas;
    private PhotoItemListener itemListener;

    public void update(AppPhotoInfo[] datas){
        this.datas = datas;
        notifyDataSetChanged();
    }

    public void setPhotoItemListener(PhotoItemListener itemListener){
        this.itemListener = itemListener;
    }

    @Override
    public int getItemViewType(int position) {
        return datas[position].getItemViewType();
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View itemView;
        switch (viewType){
            default :
            case VIEW_TYPE_PHOTO :
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_photo_item, parent, false);
                break;

            case VIEW_TYPE_CAMERA :
                itemView = LayoutInflater.from(context).inflate(R.layout.layout_photo_camera, parent, false);
                break;
        }
        itemView.setOnClickListener(this);
        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        final AppPhotoItemData data = datas[position];
        View itemView = holder.itemView;
        itemView.setTag(position);
        SimpleDraweeView thumbView = (SimpleDraweeView)itemView.findViewById(R.id.photo_thumb);
        switch (holder.getItemViewType()){
            case VIEW_TYPE_PHOTO :
                AppPhotoInfo photo = (AppPhotoInfo) data;
                String path = photo.data;
                Uri uri = FrescoUriUtils.fromFilePath(path);

                final ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                final int itemWidth = layoutParams.width;
                final int itemHeight = layoutParams.height;

                Postprocessor postProcessor = null;
                int index = path.lastIndexOf('.');
                int length = path.length();
                if(0 < index && index < (length - 1)){
                    String suffix = path.substring(index + 1, path.length());
                    if("PNG".equals(suffix.toUpperCase())){
                        postProcessor = new PngResizePostprocessor(itemWidth, itemHeight);
                    }
                }

                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .setResizeOptions(new ResizeOptions(itemWidth, itemHeight))
                        .setProgressiveRenderingEnabled(true)
                        .setLocalThumbnailPreviewsEnabled(true)
                        .setPostprocessor(postProcessor)
                        .setAutoRotateEnabled(true)
                        .build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(thumbView.getController())
                        .setImageRequest(request)
                        .setAutoPlayAnimations(false)
                        .build();

                thumbView.getHierarchy().setActualImageFocusPoint(new PointF(0.5f, 0.5f));
                thumbView.setController(controller);
                SimpleDraweeView selectedFlag = (SimpleDraweeView) itemView.findViewById(R.id.photo_selected_flag);
                if(photo.isSelected()){
                    selectedFlag.setVisibility(View.VISIBLE);
                    selectedFlag.setImageURI(FrescoUriUtils.fromResId(R.mipmap.icon_selected_flag));
                }else{
                    selectedFlag.setVisibility(View.GONE);
                }
                break;
            default :
                return;
        }
    }

    @Override
    public void onClick(View view) {
        if(null != itemListener){
            int position = (Integer)view.getTag();
            AppPhotoInfo photoInfo = datas[position];
            itemListener.onItemSelected(photoInfo.getItemViewType(), photoInfo);
        }
    }

    @Override
    public int getItemCount() {
        return null == datas ? 0 : datas.length;
    }

    public void destroy(){
        datas = null;
        itemListener = null;
    }

    public interface AppPhotoItemData extends RecyclerViewType{
        boolean isSelected();
        void setSelected(boolean selected);
    }

    public interface PhotoItemListener{
        /**
         * @param type one of {@link com.wisape.android.bean.AppPhotoInfo#VIEW_TYPE_CAMERA}
         *             {@link com.wisape.android.bean.AppPhotoInfo#VIEW_TYPE_PHOTO}
         */
        void onItemSelected(int type, AppPhotoInfo photo);
    }
}
