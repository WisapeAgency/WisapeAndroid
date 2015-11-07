package com.wisape.android.widget;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wisape.android.R;
import com.wisape.android.activity.StoryMusicActivity;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.util.LogUtil;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicAdapter extends RecyclerView.Adapter<RecyclerHolder> implements View.OnClickListener{
    private static final String TAG = StoryMusicAdapter.class.getSimpleName();
    public static final int VIEW_TYPE_MUSIC_ENTITY = 0x01;
    public static final int VIEW_TYPE_MUSIC_TYPE = 0x02;

    private StoryMusicDataInfo[] dataArray;
    private volatile boolean destroyed;
    private StoryMusicCallback callback;
    private long selectedId;
    private int selectedPosition = -1;
    private boolean downloading;

    public StoryMusicAdapter(StoryMusicCallback callback, long selectedMusicId){
        this.callback = callback;
        this.selectedId = selectedMusicId;
    }

    public void update(StoryMusicDataInfo[] dataArray){
        this.dataArray = dataArray;
        downloading = StoryManager.containsAction(StoryMusicActivity.ACTION_DOWNLOAD_MUSIC);
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
        LogUtil.d("#onBindViewHolder [" + info.toString() + "], position:" + position);
        View view = holder.itemView;
        view.setTag(position);
        TextView titleTxtv = (TextView)view.findViewById(R.id.story_music_title_txtv);
        titleTxtv.setText(info.getDisplayName());
        if(VIEW_TYPE_MUSIC_ENTITY == holder.getItemViewType()){
            ImageView flagImgv = (ImageView) view.findViewById(R.id.story_music_flag);
            ProgressBar downloadProgress = (ProgressBar) view.findViewById(R.id.story_music_download_progress);
            if(StoryMusicEntity.STATUS_DOWNLOADING == info.getStatus()){
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
                    flagImgv.setEnabled(true);
                    flagImgv.setImageResource(R.drawable.icon_download);
                }else if(selectedId == info.getId() || selectedPosition == position){
                    flagImgVisibility = View.VISIBLE;
                    flagImgv.setEnabled(false);
                    flagImgv.setImageResource(R.drawable.icon_selected_flag);
                    LogUtil.d("#onBindViewHolder FlagImageView VISIBLE, position:" + position);
                }else if(View.VISIBLE == flagImgv.getVisibility()){
                    LogUtil.d("#onBindViewHolder FlagImageView GONE, position:" + position);
                    flagImgv.setEnabled(true);
                    flagImgVisibility = View.GONE;
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
                    if(downloading){
                        Toast.makeText(view.getContext(), R.string.story_music_download_toast, Toast.LENGTH_SHORT).show();
                    }else{
                        downloading = true;
                        int position = (Integer)view.getTag();
                        StoryMusicDataInfo info = dataArray[position];
                        callback.onStoryMusicDownload(position, (StoryMusicEntity) info);
                        info.setStatus(StoryMusicEntity.STATUS_DOWNLOADING);
                        notifyItemChanged(position);
                    }
                }
                break;

            case R.id.story_music_item :
                if (null != callback) {
                    int preSelectedPosition = selectedPosition;
                    long preSelectedId = selectedId;
                    int position = (Integer)view.getTag();
                    if(preSelectedPosition == position){
                        return;
                    }

                    StoryMusicDataInfo info = dataArray[position];
                    Uri track = info.getMusicLocal();
                    if(null != track){
                        if(preSelectedId == info.getId()){
                            return;
                        }
                        callback.onStoryMusicPlay((StoryMusicEntity) info);
                        selectedPosition = position;
                        selectedId = info.getId();
                        if(0 > preSelectedPosition){
                            preSelectedPosition = findData(preSelectedId, 0);
                        }
                        LogUtil.d("#onClick preSelectedPosition:" + preSelectedPosition + ", position:" + position);
                        notifyItemChanged(preSelectedPosition);
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

    private int findData(long musicId, int position){
        int count = getItemCount();
        if(0 == count){
            return -1;
        }

        StoryMusicDataInfo data = dataArray[position];
        if(musicId != data.getId()){
            position = -1;
            int index;
            for(index = 0; index < count; index++){
                data = dataArray[index];
                if(musicId == data.getId()){
                    position = index;
                    break;
                }
            }

            if(index == (count - 1) && -1 == position){
                return -1;
            }
        }
        return position;
    }

    public void notifyMusicDownloadCompleted(Context context, long musicId, int position, Uri downloadUri){
        position = findData(musicId, position);
        if(0 > position){
            return;
        }

        downloading = false;
        StoryMusicDataInfo data = dataArray[position];
        data.setStatus(StoryMusicEntity.STATUS_NONE);
        notifyItemChanged(position);
        //story_music_download_error
        if(null == downloadUri){
            Toast.makeText(context, R.string.story_music_download_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void notifyMusicDownloadProgress(int position, long musicId, int progress, RecyclerView.LayoutManager layoutManager){
        position = findData(musicId, position);
        if(0 > position){
            return;
        }
        StoryMusicDataInfo data = dataArray[position];
//        if(StoryMusicEntity.STATUS_DOWNLOADING == data.getStatus()){
            data.setProgress(progress);
            View view = layoutManager.findViewByPosition(position);
            if(null != view){
                ProgressBar downloadProgress = (ProgressBar) view.findViewById(R.id.story_music_download_progress);
                if(null != downloadProgress && View.VISIBLE == downloadProgress.getVisibility()){
                    downloadProgress.setProgress(progress);
                }
            } else {
                notifyDataSetChanged();
            }
//        }
    }

    public StoryMusicEntity getSelectedStoryMusic(){
        if(0 <= selectedPosition){
            return (StoryMusicEntity) dataArray[selectedPosition];
        }else{
            return null;
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
        void setStatus(int status);
        int getStatus();
    }

    public interface StoryMusicCallback{
        void onStoryMusicDownload(int position, StoryMusicEntity music);
        void onStoryMusicPlay(StoryMusicEntity music);
    }
}
