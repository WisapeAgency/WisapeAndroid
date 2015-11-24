package com.wisape.android.widget;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wisape.android.R;
import com.wisape.android.model.AppPhotoInfo;

import static com.wisape.android.model.AppPhotoInfo.VIEW_TYPE_CAMERA;
import static com.wisape.android.model.AppPhotoInfo.VIEW_TYPE_PHOTO;
/**
 * Created by LeiGuoting on 10/6/15.
 */
public class PhotoWallsAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener{
    private static final String TAG = PhotoWallsAdapter.class.getSimpleName();

    private AppPhotoInfo[] datas;
    private PhotoItemListener itemListener;
    private Context context;

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
        context = parent.getContext();
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
        ImageView thumbView = (ImageView)itemView.findViewById(R.id.photo_thumb);
        if(VIEW_TYPE_PHOTO == holder.getItemViewType()){
            AppPhotoInfo photo = (AppPhotoInfo) data;
            String path = photo.data;
            Uri uri = Uri.parse(path);
            final ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            final int itemWidth = layoutParams.width;
            final int itemHeight = layoutParams.height;
            Glide.with(context).load(uri).override(itemWidth,itemHeight).centerCrop().into(thumbView);
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
         * @param type one of {@link com.wisape.android.model.AppPhotoInfo#VIEW_TYPE_CAMERA}
         *             {@link com.wisape.android.model.AppPhotoInfo#VIEW_TYPE_PHOTO}
         */
        void onItemSelected(int type, AppPhotoInfo photo);
    }
}
