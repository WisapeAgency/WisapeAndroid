package com.wisape.android.network;

import android.content.Context;
import android.util.Log;

import com.wisape.android.WisapeApplication;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by William on 2015/8/16.
 */
public class DataSynchronizer {
    private static final String THUMB_NAME = "thumb.jpg";
    private static final String STAGE_NAME = "stage.html";
    private static DataSynchronizer instance = new DataSynchronizer();

    public static DataSynchronizer getInstance() {
        return instance;
    }

    private Context context;
    private WisapeApplication application;
    private StoryTemplateInfo firstTemplate;
    private StoryLogic logic = StoryLogic.instance();
    private List<StoryTemplateInfo> allTemplate = new ArrayList<>();
    private BlockingQueue<StoryFontInfo> fontQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<StoryFontInfo> fontPreviewQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<StoryTemplateInfo> downloadQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<StoryTemplateInfo> downloadTempQueue = new LinkedBlockingQueue<>();

    private DataSynchronizer() {

    }

    public List<StoryTemplateInfo> getAllTemplate(){
        return allTemplate;
    }

    public StoryTemplateInfo getFirstTemplate() {
        if (allTemplate.size() != 0){
            return allTemplate.get(0);
        }
        return null;
    }

    void setFirstTemplate(StoryTemplateInfo firstTemplate) {
        this.firstTemplate = firstTemplate;
    }

    public boolean isDownloading() {
        return downloadTempQueue.size() != 0;
    }

    public void synchronous(Context context) {
        this.context = context;
        this.application = WisapeApplication.getInstance();
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new ThumbDownloader(downloadQueue));
        service.execute(new ThumbDownloader(downloadQueue));
        service.execute(new ThumbDownloader(downloadQueue));
        service.execute(new TemplateDownloader(downloadTempQueue));
        service.execute(new FontDownloader(fontQueue));
        service.execute(new FontPreviewDownloader(fontPreviewQueue));
        service.shutdown();

        //获取story模板
        JSONArray localData = logic.listStoryTemplateTypeLocal(context);
        JSONArray remoteData = logic.listStoryTemplateType(context, null);
        System.out.println(remoteData);
        try {
            for (int i = 1; i < remoteData.length(); i++) {
                JSONObject obj = (JSONObject) remoteData.get(i);
                getStoryTemplate(obj);
            }
        } catch (JSONException e) {
            Log.e("DataSynchronizer", "", e);
        }

        //获取字体模板
        StoryFontInfo[] fonts = logic.listFont(context, "getFonts");
        fontQueue.addAll(Arrays.asList(fonts));
        fontPreviewQueue.addAll(Arrays.asList(fonts));
    }

    private void getStoryTemplate(JSONObject obj) throws JSONException {
        StoryTemplateTypeInfo templateType = new StoryTemplateTypeInfo();
        templateType.id = obj.getInt("id");
        templateType.name = obj.getString("name");
        templateType.order = obj.optInt("order");
        application.getTemplateTypeList().add(templateType);

        ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
        attr.type = templateType.id;
        StoryTemplateEntity[] entities = logic.listStoryTemplate(context, attr, null);
        boolean isFirstTemplate = true;
        for (StoryTemplateEntity template : entities) {
            StoryTemplateInfo templateInfo = StoryTemplateEntity.convert(template);
            allTemplate.add(templateInfo);
            File templateFile = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + ".zip");
            if (templateFile.exists()){
                try{
                    InputStream is = new FileInputStream(templateFile);
                    String md5 = Utils.Md5Util(is);
                    LogUtil.d("本地MD5值：" + md5 +":服务器上的模版的MD5值：" + template.hashCode);
                    //服务器文件与本地文件的md5不相同需要重新下载
                    if (!md5.toLowerCase().equals(template.hashCode)){
                        LogUtil.d("MD5值不相同，进行模版下载");
                        downloadTempQueue.offer(templateInfo);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (isFirstTemplate){
                isFirstTemplate = false;
                downloadTempQueue.offer(templateInfo);
            }
            File temp_dir = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name);
            templateInfo.temp_img_local = new File(temp_dir, THUMB_NAME).getAbsolutePath();
            templateInfo.exists = new File(temp_dir, STAGE_NAME).exists();
            List<StoryTemplateInfo> storyTemplateInfoList = application.getTemplateMap().get(templateType.id);
            if (storyTemplateInfoList == null) {
                storyTemplateInfoList = new ArrayList<>();
            }
            storyTemplateInfoList.add(templateInfo);
            application.getTemplateMap().put(templateType.id, storyTemplateInfoList);
            downloadQueue.offer(templateInfo);
        }
    }
}
