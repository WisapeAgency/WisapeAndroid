package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wisape.android.R;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.widget.StoryMusicAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicActivity extends BaseActivity{
    public static final int REQUEST_CODE_STORY_MUSIC = 11;

    private static final String EXTRA_SELECTED_MUSIC = "_selected_music";
    private static final int WHAT_LOAD_MUSIC_LOCAL = 0x01;
    private static final int WHAT_LOAD_MUSIC = 0x02;

    public static void launch(Activity activity, StoryMusicEntity music, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), StoryMusicActivity.class);
        if(null != music){
            intent.putExtra(EXTRA_SELECTED_MUSIC, music);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    protected RecyclerView recyclerView;
    private StoryMusicAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_music);
        recyclerView = (RecyclerView)findViewById(R.id.story_music_content);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new StoryMusicAdapter();
        recyclerView.setAdapter(adapter);
        startLoad(WHAT_LOAD_MUSIC_LOCAL, null);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg;
        switch (what){
            default :
                msg = null;
                break;

            case WHAT_LOAD_MUSIC_LOCAL :
                msg = Message.obtain();
                StoryLogic logic = StoryLogic.instance();
                Context context = getApplicationContext();
                StoryMusicAdapter.StoryMusicDataInfo[] musicDataArray = logic.listMusicUIDataLocal(context);
                int count = (null == musicDataArray ? 0 : musicDataArray.length);
                if(0 == count){
                    musicDataArray = logic.listMusicAndType(context, getCancelableTag());
                }
                msg.obj = musicDataArray;
                break;

            case WHAT_LOAD_MUSIC :
                msg = Message.obtain();
                logic = StoryLogic.instance();
                context = getApplicationContext();
                musicDataArray = logic.listMusicAndType(context, getCancelableTag());
                msg.obj = musicDataArray;
                break;
        }

        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        switch (data.what){
            default :
                return;

            case WHAT_LOAD_MUSIC_LOCAL :
                StoryMusicAdapter.StoryMusicDataInfo[] musicDataArray = (StoryMusicAdapter.StoryMusicDataInfo[])data.obj;
                adapter.update(musicDataArray);
                startLoad(WHAT_LOAD_MUSIC, null);
                break;

            case WHAT_LOAD_MUSIC :
                musicDataArray = (StoryMusicAdapter.StoryMusicDataInfo[])data.obj;
                adapter.update(musicDataArray);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != adapter){
            adapter.destroy();
            adapter = null;
        }
    }
}
