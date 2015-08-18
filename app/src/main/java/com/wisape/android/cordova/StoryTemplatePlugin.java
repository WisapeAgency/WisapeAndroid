package com.wisape.android.cordova;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.api.ApiUser;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.Requester;

import org.apache.commons.io.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tony on 2015/7/19.
 */
public class StoryTemplatePlugin extends AbsPlugin{
    private static final String TEMPLATE_NAME = "stage.html";

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
        if(ACTION_GET_STAGE_CATEGORY.equals(action)){//getStageCategory  获取列表类型
            startLoad(WHAT_GET_STAGE_CATEGORY, null);
        } else if (ACTION_GET_STAGE_LIST.equals(action)){//getStageList  获取模板列表
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));//模板类型id
            }
            startLoad(WHAT_GET_STAGE_LIST, bundle);
        } else if (ACTION_START.equals(action)) {//start   下载模板
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_TEMPLATE_ID, args.getInt(0));//模板id
            }
            startLoad(WHAT_START, bundle);
        } else if (ACTION_READ.equals(action)) {//read 读取场景文件
            if(null != args && args.length() != 0){
                String templateName = args.getString(0);//模板名称
                String content = readHtml(templateName);
                callbackContext.success(content);
            }
        }else if(ACTION_REPLACE_FILE.equals(action)){//replaceFile
            if(null != args && args.length() == 2){
                String newFilePath = args.getString(0);//用户新增资源文件的硬盘路径
                String oldFilePath = args.getString(1);//被替换的文件路径
                replaceFile(newFilePath, oldFilePath);
            }
        }else if (ACTION_STORY_PATH.equals(action)){
            if(null != args && args.length() != 0){
                int id = args.getInt(0);
                getStoryPath(id);
            }
        }else if (ACTION_STAGE_PATH.equals(action)){
            if(null != args && args.length() != 0){
                int id = args.getInt(0);
                getStagePath(id);
            }
        }else if (ACTION_MUSIC_PATH.equals(action)){
            if(null != args && args.length() != 0){
                int id = args.getInt(0);
                getMusicPath(id);
            }
        }else if (ACTION_SAVE.equals(action)){//save
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 3){
                bundle.putInt(EXTRA_STORY_ID, args.getInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH,args.getString(2));
            }
            startLoad(WHAT_SAVE, bundle);
        }else if (ACTION_PUBLISH.equals(action)){//publish
            Bundle bundle = new Bundle();
            if(null != args && args.length() != 0){
                bundle.putInt(EXTRA_STORY_ID, args.getInt(0));//story_id
            }
            startLoad(WHAT_PUBLISH, bundle);
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
            }
        }
        return null;
    }

    private String readHtml(String templateName){
        File file = new File(StoryManager.getStoryTemplateDirectory(), templateName);
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(file, TEMPLATE_NAME)));
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
