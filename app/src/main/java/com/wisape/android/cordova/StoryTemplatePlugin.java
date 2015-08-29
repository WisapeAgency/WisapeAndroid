package com.wisape.android.cordova;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.StorySettingsActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.util.EnvironmentUtils;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tony on 2015/7/19.
 */
public class StoryTemplatePlugin extends AbsPlugin{
    private static final String FILE_NAME_THUMB = "thumb.jpg";
    private static final String FILE_NAME_STORY = "story.html";
    private static final String FILE_NAME_TEMPLATE = "stage.html";
    private static final String DIR_NAME_IMAGE = "img";

    public static final String ACTION_GET_STAGE_CATEGORY = "getStageCategory";
    public static final String ACTION_GET_STAGE_LIST = "getStageList";
    public static final String ACTION_START = "start";
    public static final String ACTION_READ = "read";
    public static final String ACTION_REPLACE_FILE = "replaceFile";
    public static final String ACTION_STORY_PATH = "getStoryPath";
    public static final String ACTION_STAGE_PATH = "getStagePath";
    public static final String ACTION_MUSIC_PATH = "getMusicPath";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_PUBLISH = "publish";
    public static final String ACTION_SETTING = "setting";

    private static final int WHAT_GET_STAGE_CATEGORY = 0x01;
    private static final int WHAT_GET_STAGE_LIST = 0x02;
    private static final int WHAT_START = 0x03;
    private static final int WHAT_SAVE = 0x04;
    private static final int WHAT_PUBLISH = 0x05;

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "extra_template_id";
    private static final String EXTRA_STORY_ID = "extra_story_id";
    private static final String EXTRA_STORY_HTML = "extra_story_html";
    private static final String EXTRA_FILE_PATH = "extra_file_path";

    private CallbackContext callbackContext;
    private StoryLogic logic = StoryLogic.instance();
    private WisapeApplication app = WisapeApplication.getInstance();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(null == action || 0 == action.length()){
            return true;
        }
        this.callbackContext = callbackContext;
        if(ACTION_GET_STAGE_CATEGORY.equals(action)){//getStageCategory
            startLoad(WHAT_GET_STAGE_CATEGORY, null);
        } else if (ACTION_GET_STAGE_LIST.equals(action)){//getStageList
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));//
            }
            startLoad(WHAT_GET_STAGE_LIST, bundle);
        } else if (ACTION_START.equals(action)) {//start
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_TEMPLATE_ID, args.getInt(0));//
            }
            startLoad(WHAT_START, bundle);
        } else if (ACTION_READ.equals(action)) {//read
            if(null != args && args.length() != 0){
                String path = args.getString(0);//
                String content = readHtml(path);
                callbackContext.success(content);
            }
        }else if(ACTION_REPLACE_FILE.equals(action)){//replaceFile
            if(null != args && args.length() == 2){
                String newFilePath = args.getString(0);//
                String oldFilePath = args.getString(1);//
                replaceFile(newFilePath, oldFilePath);
            }
        }else if (ACTION_STORY_PATH.equals(action)){
            if(null != args && args.length() == 1){
                int id = args.getInt(0);
                getStoryPath(id);
            }
        }else if (ACTION_STAGE_PATH.equals(action)){
            if(null != args && args.length() == 1){
                int id = args.getInt(0);
                getStagePath(id);
            }
        }else if (ACTION_MUSIC_PATH.equals(action)){
            if(null != args && args.length() == 1){
                int id = args.getInt(0);
                getMusicPath(id);
            }
        }else if (ACTION_SAVE.equals(action)){//save
            Bundle bundle = new Bundle();
            if(null != args && args.length() == 3){
                bundle.putInt(EXTRA_STORY_ID, args.optInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH,args.getString(2));
            }
            startLoad(WHAT_SAVE, bundle);
        }else if (ACTION_PUBLISH.equals(action)){//publish
            Bundle bundle = new Bundle();
            if(null != args && args.length() == 3){
                bundle.putInt(EXTRA_STORY_ID, args.optInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
            startLoad(WHAT_PUBLISH, bundle);
        }else if (ACTION_SETTING.equals(action)){
            StorySettingsActivity.launch(getCurrentActivity(), 0);
        }
        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Context context = getCurrentActivity().getApplicationContext();
        switch (what){
            default :
                return null;
            case WHAT_GET_STAGE_CATEGORY : {
//                JSONArray jsonStr = logic.listStoryTemplateType(context, null);
                JSONArray jsonStr = logic.listStoryTemplateTypeLocal(context);
                callbackContext.success(jsonStr);
                System.out.println(jsonStr.toString());
                break;
            }
            case WHAT_GET_STAGE_LIST: {
//                ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
//                attr.type = args.getInt(EXTRA_CATEGORY_ID, 0);
//                StoryTemplateEntity[] entities = logic.listStoryTemplate(context, attr, null);
                int type = args.getInt(EXTRA_CATEGORY_ID, 0);
                List<StoryTemplateInfo> entities = app.getTemplateMap().get(type);
                if(entities == null){
                    entities = new ArrayList<>();
                }
                System.out.println(entities.toString());
//                System.out.println(entities.toString());
                callbackContext.success(new Gson().toJson(entities));
                break;
            }
            case WHAT_START: {
                ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
                attr.id = args.getInt(EXTRA_TEMPLATE_ID, 0);
                Requester.ServerMessage message = logic.getStoryTemplateUrl(context, attr, null);
                if (!message.succeed()){
                    callbackContext.error(message.status);
                    return null;
                }
                if (cordova.getActivity() instanceof StoryTemplateActivity){
                    StoryTemplateActivity activity = (StoryTemplateActivity)cordova.getActivity();
                    try {
                        activity.downloadTemplate(message.data.toString(),attr.id);
                    }catch (JSONException e){
                        callbackContext.error(-1);
                    }
                }
                break;
            }
            case WHAT_SAVE:{
                int storyId = args.getInt(EXTRA_STORY_ID,0);
                String html = args.getString(EXTRA_STORY_HTML);
                String path = args.getString(EXTRA_FILE_PATH);
                com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);
                String storyName;
                if (storyId == 0){
                    storyName = UUID.randomUUID().toString().substring(0,8);
                }else{
                    StoryEntity story = logic.getStoryLocalById(context,storyId);
                    storyName = story.storyName;
                }
                File storyDirectory = new File(StoryManager.getStoryDirectory(), storyName);
                if (!storyDirectory.exists()){
                    storyDirectory.mkdirs();
                }
                if(!saveStory(storyDirectory,html,paths)){
                    callbackContext.error(-1);
                    return null;
                }
                StoryEntity story = new StoryEntity();
                story.storyName = storyName;
                logic.saveStoryLocal(context,story);
                break;
            }
            case WHAT_PUBLISH:{
                int storyId = args.getInt(EXTRA_STORY_ID,0);
                String html = args.getString(EXTRA_STORY_HTML);
                String path = args.getString(EXTRA_FILE_PATH);
                com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);
                String storyName;
                if (storyId == 0){
                    storyName = UUID.randomUUID().toString().substring(0,8);
                }else{
                    StoryEntity story = logic.getStoryLocalById(context,storyId);
                    storyName = story.storyName;
                }
                File storyDirectory = new File(StoryManager.getStoryDirectory(), storyName);
                if (!storyDirectory.exists()){
                    storyDirectory.mkdirs();
                }
                if(!saveStory(storyDirectory,html,paths)){
                    callbackContext.error(-1);
                    return null;
                }
                StoryEntity story = new StoryEntity();
                story.storyName = storyName;
                logic.saveStoryLocal(context,story);

                ApiStory.AttrStoryInfo storyInfo = new ApiStory.AttrStoryInfo();
                storyInfo.attrStoryThumb = Uri.fromFile(new File(storyDirectory, FILE_NAME_THUMB));
                storyInfo.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
                storyInfo.story = Uri.fromFile(storyDirectory);
                storyInfo.storyName = storyName;
                storyInfo.storyDescription = "hahahaha";
                logic.update(context,storyInfo,null);
                break;
            }
        }
        return null;
    }

    private boolean saveStory(File storyDirectory,String html,com.alibaba.fastjson.JSONArray paths){
        File storyHTML = new File(storyDirectory,FILE_NAME_STORY);
        PrintWriter writer = null;
        try{
            writer = new PrintWriter(storyHTML);
            writer.write(html);
            writer.close();
        }catch (IOException e){
            Log.e("saveStory","",e);
            return false;
        }finally {
            if (writer != null){
                writer.close();
            }
        }
        File storyImg = new File(storyDirectory,DIR_NAME_IMAGE);
        if (storyImg.exists()){
            try{
                FileUtils.deleteDirectory(storyImg);
            }catch (IOException e){
                Log.e("saveStory","",e);
            }
        }
        storyImg.mkdirs();
        try{
            for (int i=0;i<paths.size();i++){
                File file = new File(paths.getString(i));
                FileUtils.copyFile(file,new File(storyImg,file.getName()));
            }
        }catch (IOException e){
            Log.e("saveStory","",e);
        }
        return true;
    }

    private String readHtml(String path){
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null){
                content.append(line);
            }
            reader.close();
            return content.toString();
        }catch (IOException e){
            return "";
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){

                }

            }
        }
    }

    private void replaceFile(String newFilePath,String oldFilePath){
        File newFile = new File(newFilePath);
        if (!newFile.exists()){
            callbackContext.error("New file does not exists!");
        }
        File oldFile = new File(oldFilePath);
        try {
            org.apache.commons.io.FileUtils.copyFile(newFile, oldFile);
        }catch (IOException e){
            callbackContext.error("copy file error!" + e.getMessage());
        }
        callbackContext.success();
    }

    private void getStoryPath(int id){
        Context context = getCurrentActivity().getApplicationContext();
        StoryEntity story = logic.getStoryLocalById(context, id);
        if(story != null){
            callbackContext.success(story.storyLocal);
        }else{
            callbackContext.error(1);//not fond
        }
    }

    private void getStagePath(int id){
        Context context = getCurrentActivity().getApplicationContext();
        StoryTemplateEntity template = logic.getStoryTemplateLocalById(context,id);
        if(template != null){
            callbackContext.success(template.templateLocal);
        }else{
            callbackContext.error(1);//not fond
        }
    }

    private void getMusicPath(int id){
        Context context = getCurrentActivity().getApplicationContext();
        StoryMusicEntity music = logic.getStoryMusicLocalById(context, id);
        if(music != null){
            callbackContext.success(music.musicLocal);
        }else{
            callbackContext.error(1);//not fond
        }
    }
}
