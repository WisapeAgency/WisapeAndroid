package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.DynamicBroadcastReceiver;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.network.Downloader;
import com.wisape.android.util.LogUtil;
import com.wisape.android.widget.LinearDividerItemDecoration;
import com.wisape.android.widget.StoryMusicAdapter;

import org.cubieline.lplayer.PlayerProxy;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicActivity extends BaseActivity implements StoryMusicAdapter.StoryMusicCallback, DynamicBroadcastReceiver.OnDynamicBroadcastReceiverListener{
    private static final String TAG = StoryMusicActivity.class.getSimpleName();

    public static final int REQUEST_CODE_STORY_MUSIC = 11;

    public static final String ACTION_DOWNLOAD_MUSIC = "com.wisape.android.action.DOWNLOAD_MUSIC";

    public static final String EXTRA_SELECTED_MUSIC = "_selected_music";
    private static final String EXTRA_POSITION = "_position";
    private static final String EXTRA_ACTION_DOWNLOAD_MUSIC = "_action_download_music";

    private static final int WHAT_LOAD_MUSIC_LOCAL = 0x01;
    private static final int WHAT_LOAD_MUSIC = 0x02;
    private static final int WHAT_DOWNLOAD_MUSIC = 0x03;

    public static void launch(Activity activity, StoryMusicEntity music, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), StoryMusicActivity.class);
        if(null != music){
            intent.putExtra(EXTRA_SELECTED_MUSIC, music);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    protected RecyclerView recyclerView;
    private StoryMusicAdapter adapter;
    private StoryMusicEntity selectedMusic;
    private LocalBroadcastManager localBroadcastManager;
    private DynamicBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intentExtras = getIntent().getExtras();
        if(null != intentExtras && intentExtras.containsKey(EXTRA_SELECTED_MUSIC)){
            selectedMusic = intentExtras.getParcelable(EXTRA_SELECTED_MUSIC);
        }
        setContentView(R.layout.activity_story_music);
        recyclerView = (RecyclerView)findViewById(R.id.story_music_content);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        LinearDividerItemDecoration divider = new LinearDividerItemDecoration(getResources(), R.drawable.app_divider);
        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StoryMusicAdapter(this, null == selectedMusic ? 0 : selectedMusic.serverId);
        recyclerView.setAdapter(adapter);

        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        receiver = new DynamicBroadcastReceiver(this);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ACTION_DOWNLOAD_MUSIC));
        startLoad(WHAT_LOAD_MUSIC_LOCAL, null);
    }

    @Override
    public void onReceiveBroadcast(Context context, Intent intent) {
        LogUtil.d("收到更新音乐下载进度消息");
        if(isDestroyed() || null == intent){
            return;
        }

        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return;
        }

        if(ACTION_DOWNLOAD_MUSIC.equals(action)){
            Bundle tag = intent.getBundleExtra(Downloader.EXTRA_TAG);
            StoryMusicEntity music = tag.getParcelable(EXTRA_SELECTED_MUSIC);
            int position = tag.getInt(EXTRA_POSITION);
            int progress = (int)(100 * intent.getDoubleExtra(Downloader.EXTRA_PROGRESS, 0));
            intent.getExtras().clear();
            LogUtil.d("#onReceiveBroadcast progress:" + progress + ", downloadBytes:" + intent.getLongExtra(Downloader.EXTRA_TRANSFERRED_BYTES, 0));
            adapter.notifyMusicDownloadProgress(position, music.getId(), progress, recyclerView.getLayoutManager());
        }
    }

    @Override
    public void onStoryMusicDownload(int position, StoryMusicEntity music) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_SELECTED_MUSIC, music);
        args.putInt(EXTRA_POSITION, position);
        args.putString(EXTRA_ACTION_DOWNLOAD_MUSIC, ACTION_DOWNLOAD_MUSIC);
        startLoad(WHAT_DOWNLOAD_MUSIC, args);
    }

    @Override
    public void onStoryMusicPlay(StoryMusicEntity music) {
        PlayerProxy.getTracksClient().addTrackAndPlay(music, 0, 0);
    }

    private void doSelectedMusic(){
        StoryMusicEntity selectedMusic = adapter.getSelectedStoryMusic();
        if(null != selectedMusic){

            StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
            storyEntity.storyMusicName = selectedMusic.name;
            storyEntity.storyMusicLocal = Uri.parse(selectedMusic.musicLocal).getPath();
            storyEntity.musicServerId = selectedMusic.serverId;
            StoryLogic.instance().updateStory(getApplicationContext(), storyEntity);
        }
    }

    @Override
    protected boolean onBackNavigation() {
        doSelectedMusic();
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        doSelectedMusic();
        finish();
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

            case WHAT_DOWNLOAD_MUSIC :
                StoryMusicEntity music = args.getParcelable(EXTRA_SELECTED_MUSIC);
                String action = args.getString(EXTRA_ACTION_DOWNLOAD_MUSIC);
                Uri uri = StoryManager.downStoryMusic(getApplicationContext(), music, action, args);

                msg = Message.obtain();
                music.musicLocal = (null == uri ? null : uri.toString());
                msg.obj = music;
                msg.arg2 = args.getInt(EXTRA_POSITION);
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
                if(null != musicDataArray){
                    adapter.update(musicDataArray);
                }
                break;

            case WHAT_DOWNLOAD_MUSIC :
                StoryMusicEntity music = (StoryMusicEntity) data.obj;
                Uri download = (null == music.musicLocal ? null : Uri.parse(music.musicLocal));
                int position = data.arg2;
                adapter.notifyMusicDownloadCompleted(getApplicationContext(), music.getId(), position, download);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayerProxy.getLPlayerClient().pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != adapter){
            adapter.destroy();
            adapter = null;
        }

        if(null != receiver){
            localBroadcastManager.unregisterReceiver(receiver);
            localBroadcastManager = null;
            receiver.destroy();
            receiver = null;
        }
    }
}
