package com.wisape.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.BasePostprocessor;
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
public class PhotoWallAdapter extends RecyclerView.Adapter<RecyclerHolder>{
    private static final String TAG = PhotoWallAdapter.class.getSimpleName();
    private AppPhotoItemData[] datas;

    public void update(AppPhotoItemData[] datas){
        this.datas = datas;
        notifyDataSetChanged();
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
        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        final AppPhotoItemData data = datas[position];
        View itemView = holder.itemView;
        SimpleDraweeView thumbView = (SimpleDraweeView)itemView.findViewById(R.id.photo_thumb);
        switch (holder.getItemViewType()){
            default :
            case VIEW_TYPE_PHOTO :
                AppPhotoInfo photo = (AppPhotoInfo) data;
                Uri uri = FrescoUriUtils.fromFilePath(photo.data);

                final ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                final int itemWidth = layoutParams.width;
                final int itemHeight = layoutParams.height;

                Postprocessor postProcessor = null;
                String path = photo.data;
                int index = path.lastIndexOf('.');
                int length = path.length();
                if(0 < index && index < (length - 1)){
                    String suffix = path.substring(index + 1, path.length());
                    if("PNG".equals(suffix.toUpperCase())){
                        postProcessor = new PngPostprocessor(itemWidth, itemHeight);
                    }
                }

                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                        .setResizeOptions(new ResizeOptions(itemWidth, itemHeight))
                        .setProgressiveRenderingEnabled(true)
                        .setLocalThumbnailPreviewsEnabled(true)
                        .setPostprocessor(postProcessor)
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

            case VIEW_TYPE_CAMERA :
                thumbView.setImageURI(FrescoUriUtils.fromResId(R.mipmap.icon_camera));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return null == datas ? 0 : datas.length;
    }

    private static class PngPostprocessor extends BasePostprocessor{
        private final int viewWidth;
        private final int viewHeight;

        PngPostprocessor(int viewWidth, int viewHeight){
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;
        }

        @Override
        public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
            int bmpWidth = sourceBitmap.getWidth();
            int bmpHeight = sourceBitmap.getHeight();
            Log.d(TAG, "#process bitmap oldWidth:" + bmpWidth + ", oldHeight:" + bmpHeight);
            if(bmpWidth <= viewWidth){
                return super.process(sourceBitmap, bitmapFactory);
            }else{
                int newWidth = viewWidth;
                float scaleWidth = (float) newWidth / (float)bmpWidth;
                int newHeight = (int)(((float)bmpHeight) * scaleWidth);
                float scaleHeight = (float) newHeight / bmpHeight;

                CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(newWidth, newHeight);
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);

                Bitmap newBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
                CloseableReference var4;
                try {
                    Bitmaps.copyBitmap(bitmapRef.get(), newBitmap);
                    var4 = CloseableReference.cloneOrNull(bitmapRef);
                } finally {
                    CloseableReference.closeSafely(bitmapRef);
                }

                return var4;
            }
        }

        @Override
        public String getName() {
            return "PNG-Postprocessor";
        }
    }

    public interface AppPhotoItemData extends RecyclerViewType{
        boolean isSelected();
        void setSelected(boolean selected);
    }
}
