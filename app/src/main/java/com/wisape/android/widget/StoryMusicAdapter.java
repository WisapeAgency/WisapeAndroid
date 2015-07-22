package com.wisape.android.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wisape.android.R;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicAdapter extends RecyclerView.Adapter<RecyclerHolder>{
    public static final int VIEW_TYPE_MUSIC_ENTITY = 0x01;
    public static final int VIEW_TYPE_MUSIC_TYPE = 0x02;
    private StoryMusicDataInfo[] dataArray;
    private volatile boolean destroyed;

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
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(params);
                break;
        }
        holder = new RecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        StoryMusicDataInfo info = dataArray[position];
        View view = holder.itemView;
        TextView titleTxtv = (TextView)view.findViewById(R.id.story_music_title_txtv);
        titleTxtv.setText(info.getDisplayName());
        if(VIEW_TYPE_MUSIC_ENTITY == holder.getItemViewType()){
            //TODO
        }
    }

    @Override
    public int getItemCount() {
        return null == dataArray ? 0 : dataArray.length;
    }

    public void destroy(){
        destroyed = true;
        dataArray = null;
    }

    public interface StoryMusicDataInfo extends RecyclerViewType{
        long getId();
        String getDisplayName();
        String getDownloadUrl();
    }
}
