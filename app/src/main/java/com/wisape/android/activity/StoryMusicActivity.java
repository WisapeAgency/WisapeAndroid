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
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.network.Downloader;
import com.wisape.android.widget.StoryMusicAdapter;

import org.cubieline.lplayer.PlayerProxy;

/**
 * Created by tony on 2015/7/22.
 */
public class StoryMusicActivity extends BaseActivity implements StoryMusicAdapter.StoryMusicCallback, DynamicBroadcastReceiver.OnDynamicBroadcastReceiverListener{
    private static final String TAG = StoryMusicActivity.class.getSimpleName();

    public static final int REQUEST_CODE_STORY_MUSIC = 11;

    private static final String ACTION_DOWNLOAD_MUSIC = "com.wisape.android.action.DOWNLOAD_MUSIC";

    public static final String EXTRA_SELECTED_MUSIC = "_selected_music";
    private static final String EXTRA_POSITION = "_position";
    private static final String EXTRA_ACTION_DOWNLOAD_MUSIC = "_action_download_music";

    private static final int WHAT_LOAD_MUSIC_LOCAL = 0x01;
    private static final int WHAT_LOAD_MUSIC = 0x02;
    private static final int WHAT_DOWNLOAD_MUSIC = 0x03;
    private static final int WHAT_SAVE_MUSIC_STATUS = 0x04;

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
        if(isDestroyed() || null == intent){
            return;
        }

        String action = intent.getAction();
        if(null == action || 0 == action.length()){
            return;
        }

        if(ACTION_DOWNLOAD_MUSIC.equals(action)){
            Bundle tag = intent.getBundleExtra(Downloader.EXTRA_TAG);
            int position = tag.getInt(EXTRA_POSITION);
            int progress = (int)(100 * intent.getDoubleExtra(Downloader.EXTRA_PROGRESS, 0));
            intent.getExtras().clear();
            //Log.d(TAG, "#onReceiveBroadcast progress:" + progress + ", downloadBytes:" + intent.getLongExtra(Downloader.EXTRA_TRANSFERRED_BYTES, 0));
            adapter.notifyMusicDownloadProgress(position, progress, recyclerView.getLayoutManager());
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
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SELECTED_MUSIC, selectedMusic);
            setResult(RESULT_OK, intent);
        }else{
            setResult(RESULT_CANCELED);
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
                msg.obj = uri;
                msg.arg2 = args.getInt(EXTRA_POSITION);
                break;

            case WHAT_SAVE_MUSIC_STATUS :
                msg = null;
                music = args.getParcelable(EXTRA_SELECTED_MUSIC);
                Log.d(TAG, "#onLoadBackgroundRunning ___ WHAT_SAVE_MUSIC_STATUS ___");
                StoryLogic.instance().updateStoryMusic(getApplicationContext(), music);
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
                Uri download = (Uri)data.obj;
                int position = data.arg2;
                adapter.notifyMusicDownloadCompleted(position, download);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayerProxy.getLPlayerClient().pause();
        StoryMusicEntity music = adapter.getDownloadingStoryMusic();
        if(null != music){
            Bundle args = new Bundle();
            args.putParcelable(EXTRA_SELECTED_MUSIC, music);
            startLoad(WHAT_SAVE_MUSIC_STATUS, args);
        }
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
