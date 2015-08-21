package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.wisape.android.R;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.ZipUtils;

import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class TestActivity extends BaseActivity{
    private static final String TAG = TestActivity.class.getSimpleName();

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), TestActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button)
    @SuppressWarnings("unused")
    protected void doBtnTest(){
        //startLoad(5, null);
        StorySettingsActivity.launch(this, 0);
    }

    @OnClick(R.id.qr_code)
    @SuppressWarnings("unused")
    protected void doQr(){
        //startLoad(6, null);
        StoryReleaseActivity.launch(this, StoryReleaseActivity.REQUEST_CODE_STORY_RELEASE);
    }

    @OnClick(R.id.list_template)
    protected void doListTemplate(){
        startLoad(5, null);
    }

    @OnClick(R.id.download_template)
    protected void doDownloadTemplate(){

    }

    @OnClick(R.id.create_story)
    protected void doCreateStory(){
        StoryTemplateActivity.launch(this,0);
    }

    @OnClick(R.id.share)
    protected void doShare(){
//        Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
//        intent.setType("text/plain"); // 分享发送的数据类型
//        String msg = "推荐给大家，http://www.wisape.com/demo/playstory/index.html";
//        intent.putExtra(Intent.EXTRA_TEXT, msg); // 分享的内容
//        startActivity(Intent.createChooser(intent, "选择分享"));

//        File template = new File(StoryManager.getStoryTemplateDirectory(),"mingpian01");
//        File thumb = new File(template,"thumb.jpg");
//        String msg = "推荐给大家，http://www.wisape.com/demo/playstory/index.html";
//        shareMessage("标题", "消息标题", msg, thumb.getAbsolutePath());

        initShareIntent("com.sina.weibo");
    }

    /**
     * 分享功能
     *
     * @param context
     *            上下文
     * @param activityTitle
     *            Activity的名字
     * @param msgTitle
     *            消息标题
     * @param msgText
     *            消息内容
     * @param imgPath
     *            图片路径，不分享图片则传null
     */
    public void shareMessage(String activityTitle, String msgTitle, String msgText,
                         String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if ("".equals(imgPath)) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File image = new File(imgPath);
            if (image.exists() && image.isFile()) {
                intent.setType("image/jpg");
                Uri uri = Uri.fromFile(image);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, activityTitle));
    }

    private void initShareIntent(String type) {
        File template = new File(StoryManager.getStoryTemplateDirectory(),"mingpian01");
        File thumb = new File(template,"thumb.jpg");
        String msg = "推荐给大家，http://www.wisape.com/demo/playstory/index.html";
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/jpeg");
        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(Intent.EXTRA_SUBJECT,  "subject");
                    share.putExtra(Intent.EXTRA_TEXT,     msg);
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(thumb) ); // Optional, just if you wanna share an image.
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return;
            startActivity(Intent.createChooser(share, "Select"));
        }
    }


    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        if(1 == what){
            Uri source = Uri.fromFile(new File(EnvironmentUtils.getAppTemporaryDirectory(), "template_light.zip"));
            File targetDir = new File(EnvironmentUtils.getAppDataDirectory(), "template_light");
            try{
                Log.d(TAG, "#onLoadBackgroundRunning ___");
                ZipUtils.unzip(source, targetDir);
            }catch (IOException e){
                Log.e(TAG, "", e);
            }
        }else if(2 == what){
            Uri zipSource = Uri.fromFile(new File(EnvironmentUtils.getAppDataDirectory(), "template_light"));
            File targetDir = EnvironmentUtils.getAppTemporaryDirectory();
            try{
                ZipUtils.zip(zipSource, targetDir, "template_one.zip");
            }catch (IOException e){
                Log.e(TAG, "", e);
            }
        }

        else if(3 == what){
            ApiStory.AttrStoryInfo story = new ApiStory.AttrStoryInfo();
            Uri thumb = Uri.fromFile(new File(EnvironmentUtils.getAppTemporaryDirectory(), "_crop_0_66899726.jpeg"));
            story.attrStoryThumb = thumb;
            story.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
            story.story = Uri.fromFile(new File(EnvironmentUtils.getAppDataDirectory(), "template_light"));
            story.storyName = "story_one_1.zip";
            story.storyDescription = "hahahaha";
            StoryLogic.instance().update(getApplicationContext(), story, getCancelableTag());
        }

        else if(4 == what){
            //获取模板类型
            StoryLogic logic = StoryLogic.instance();
            JSONArray jsonArray = logic.listStoryTemplateTypeLocal(getApplicationContext());
            if(null == jsonArray){
                Log.d(TAG, "listStoryTemplateTypeLocal is null.");
                jsonArray = logic.listStoryTemplateType(getApplicationContext(), getCancelableTag());
            }

            if(null != jsonArray){
                Log.d(TAG, "#listStoryTemplateType TemplateType:" + jsonArray.toString());
            }else{
                Log.d(TAG, "#listStoryTemplateType jsonArray is null.");
            }
        }

        else if(5 == what){
            StoryLogic logic = StoryLogic.instance();
            ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
            attr.type = args.getInt("type", 0);
            StoryTemplateEntity[] templateEntities = logic.listStoryTemplate(getApplicationContext(), attr, getCancelableTag());
            if(null == templateEntities){
                Log.d(TAG, "#listStoryTemplate templateEntities is null");
            }else{
                Gson gson = new Gson();
                for(StoryTemplateEntity entity : templateEntities){
                    Log.d(TAG, "#listStoryTemplate entity:" + gson.toJson(entity));
                    if(6 == entity.serverId){
                        StoryManager.downTemplate(getApplicationContext(), entity, null, null);
                    }
                }
            }
        }

        else if(6 == what){
            //Uri source = Uri.parse("http://106.75.194.11/uploads/2015072108/c93f5445c3304227bb1538493f9c7a0e.zip");
            //StoryManager.downTemplate(getApplicationContext(), source, null);
            StoryLogic logic = StoryLogic.instance();
            StoryTemplateEntity[] templateEntities = logic.listStoryTemplateLocal(getApplicationContext());
            if(null == templateEntities){
                Log.d(TAG, "#listStoryTemplate templateEntities is null");
            }else{
                Gson gson = new Gson();
                for(StoryTemplateEntity entity : templateEntities){
                    Log.d(TAG, "#listStoryTemplate entity:" + gson.toJson(entity));
                    if(6 == entity.serverId){
                        StoryManager.createStory(getApplicationContext(), entity);
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
