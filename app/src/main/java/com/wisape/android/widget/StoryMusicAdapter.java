package com.wisape.android.widget;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wisape.android.R;
import com.wisape.android.database.StoryMusicEntity;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener{
    public static final int VIEW_TYPE_MUSIC_ENTITY = 0x01;
    public static final int VIEW_TYPE_MUSIC_TYPE = 0x02;

    private static final int STATUS_DOWNLOADING = 0x01;
    private static final int STATUS_NONE = 0;

    private StoryMusicDataInfo[] dataArray;
    private volatile boolean destroyed;
    private StoryMusicCallback callback;
    private long selectedMusic;
    private int selectedMusicPosition;

    public StoryMusicAdapter(StoryMusicCallback callback, long selectedMusicId){
        this.callback = callback;
        this.selectedMusic = selectedMusicId;
    }

    public void update(StoryMusicDataInfo[] dataArray){
        this.dataArray = dataArray;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return dataArray[position].getItemViewType();
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerHolder holder;
        switch (viewType){
            default :
                return null;

            case VIEW_TYPE_MUSIC_TYPE :
                view = View.inflate(parent.getContext(), R.layout.layout_story_music_title, null);
                break;

            case VIEW_TYPE_MUSIC_ENTITY :
                view = View.inflate(parent.getContext(), R.layout.layout_story_music_entity, null);
                view.setOnClickListener(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                view.findViewById(R.id.story_music_flag).setOnClickListener(this);
                break;
        }
        holder = new RecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        StoryMusicDataInfo info = dataArray[position];
        View view = holder.itemView;
        view.setTag(position);
        TextView titleTxtv = (TextView)view.findViewById(R.id.story_music_title_txtv);
        titleTxtv.setText(info.getDisplayName());
        if(VIEW_TYPE_MUSIC_ENTITY == holder.getItemViewType()){
            ImageView flagImgv = (ImageView) view.findViewById(R.id.story_music_flag);
            ProgressBar downloadProgress = (ProgressBar) view.findViewById(R.id.story_music_download_progress);
            if(STATUS_DOWNLOADING == info.getUiStatus()){
                if(View.VISIBLE != downloadProgress.getVisibility()){
                    downloadProgress.setVisibility(View.VISIBLE);
                }
                if(View.VISIBLE == flagImgv.getVisibility()){
                    flagImgv.setVisibility(View.GONE);
                }
                downloadProgress.setProgress(info.getProgress());
            }else{
                if(View.VISIBLE == downloadProgress.getVisibility()){
                    downloadProgress.setVisibility(View.GONE);
                }
                flagImgv.setTag(position);
                int flagImgVisibility = View.GONE;
                Uri musicLocal = info.getMusicLocal();
                if(null == musicLocal){
                    flagImgVisibility = View.VISIBLE;
                    flagImgv.setImageResource(R.drawable.icon_download);
                }else if(selectedMusic == info.getId() || selectedMusicPosition == position){
                    flagImgVisibility = View.VISIBLE;
                    flagImgv.setImageResource(R.drawable.icon_selected_flag);
                }
                flagImgv.setVisibility(flagImgVisibility);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            default :
                return;

            case R.id.story_music_flag :
                if (null != callback) {
                    int position = (Integer)view.getTag();
                    StoryMusicDataInfo info = dataArray[position];
                    callback.onStoryMusicDownload(position, (StoryMusicEntity) info);
                    info.setUiStatus(STATUS_DOWNLOADING);
                    notifyItemChanged(position);
                }
                break;

            case R.id.story_music_item :
                if (null != callback) {
                    int position = (Integer)view.getTag();
                    StoryMusicDataInfo info = dataArray[position];
                    Uri track = info.getMusicLocal();
                    if(null != track){
                        callback.onStoryMusicPlay((StoryMusicEntity) info);
                        selectedMusicPosition = position;
                        selectedMusic = info.getId();
                        notifyItemChanged(position);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        return null == dataArray ? 0 : dataArray.length;
    }

    public void notifyMusicDownloadCompleted(int position, Uri downloadUri){
        StoryMusicDataInfo data = dataArray[position];
        data.setUiStatus(STATUS_NONE);
        notifyItemChanged(position);
    }

    public void notifyMusicDownloadProgress(int position, int progress, RecyclerView.LayoutManager layoutManager){
        StoryMusicDataInfo data = dataArray[position];
        if(STATUS_DOWNLOADING == data.getUiStatus()){
            data.setProgress(progress);
            View view = layoutManager.getChildAt(position);
            if(null != view){
                ProgressBar downloadProgress = (ProgressBar) view.findViewById(R.id.story_music_download_progress);
                if(null != downloadProgress && View.VISIBLE == downloadProgress.getVisibility()){
                    downloadProgress.setProgress(progress);
                }
            }
        }
    }

    public void destroy(){
        destroyed = true;
        dataArray = null;
        callback = null;
    }

    public interface StoryMusicDataInfo extends RecyclerViewType{
        long getId();
        String getDisplayName();
        Uri getDownloadUrl();
        Uri getMusicLocal();
        void setProgress(int progress);
        int getProgress();
        void setUiStatus(int status);
        int getUiStatus();
    }

    public interface StoryMusicCallback{
        void onStoryMusicDownload(int position, StoryMusicEntity music);
        void onStoryMusicPlay(StoryMusicEntity music);
    }
}
