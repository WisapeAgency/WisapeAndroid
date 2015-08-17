package com.wisape.android.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryTemplateEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.util.EnvironmentUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by William on 2015/8/16.
 */
public class DataSynchronizer {
    private static DataSynchronizer instance = new DataSynchronizer();
    public static DataSynchronizer getInstance(){
        return instance;
    }

    private Context context;
    private StoryLogic logic = StoryLogic.instance();
    private List<StoryTemplateTypeInfo> templateTypeList = new ArrayList<>();
    private Map<Long,List<StoryTemplateInfo>> templateMap = new HashMap<>();
    private BlockingQueue<StoryTemplateInfo> downloadQueue = new LinkedBlockingQueue<>();
    private DataSynchronizer(){

    }

    public void synchronous(Context context){
        this.context = context;
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new Downloader(downloadQueue));
        service.execute(new Downloader(downloadQueue));
        service.execute(new Downloader(downloadQueue));
        service.shutdown();

        JSONArray localData = logic.listStoryTemplateTypeLocal(context);
        JSONArray remoteData = logic.listStoryTemplateType(context, null);
        System.out.println(remoteData);
        try{
            for(int i=0;i<remoteData.length();i++){
                JSONObject obj = (JSONObject)remoteData.get(i);
                getStoryTemplate(obj);
            }
        }catch (JSONException e){
            Log.e("DataSynchronizer","",e);
        }
    }

    private void getStoryTemplate(JSONObject obj) throws JSONException{
        StoryTemplateTypeInfo templateType = new StoryTemplateTypeInfo();
        templateType.id = obj.getInt("id");
        templateType.name = obj.getString("name");
        try{
            templateType.order = obj.getInt("order");
        }catch (JSONException e){
            templateType.order = 0;
        }
        templateTypeList.add(templateType);

        ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
        attr.type = templateType.id;
        StoryTemplateEntity[] entities = logic.listStoryTemplate(context, attr, null);
        for (StoryTemplateEntity template : entities){
            StoryTemplateInfo templateInfo = StoryTemplateEntity.convert(template);
            templateInfo.temp_img_local = new File(StoryManager.getStoryTemplateDirectory(),
                    templateInfo.temp_name).getAbsolutePath();
            List<StoryTemplateInfo> storyTemplateInfoList = templateMap.get(templateType.id);
            if (storyTemplateInfoList == null){
                storyTemplateInfoList = new ArrayList<>();
            }
            storyTemplateInfoList.add(templateInfo);
            templateMap.put(templateType.id, storyTemplateInfoList);
            downloadQueue.offer(templateInfo);
        }
    }

    public static class Downloader implements Runnable{
        private static final String THUMB_NAME = "thumb.jpg";
        private BlockingQueue<StoryTemplateInfo> downloadQueue;

        public Downloader(BlockingQueue<StoryTemplateInfo> downloadQueue){
            this.downloadQueue = downloadQueue;
        }

        @Override
        public void run() {
            boolean isRunning = true;
            try {
                while (isRunning) {
                    StoryTemplateInfo templateInfo = downloadQueue.poll(30, TimeUnit.SECONDS);
                    if (null != templateInfo) {
                        download(templateInfo);
                    } else {
                        // 超过30s还没数据，自动退出下载线程。
                        isRunning = false;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        private void download(StoryTemplateInfo templateInfo){
            if(!EnvironmentUtils.isMounted()){
                return;
            }
            File destFile = new File(templateInfo.temp_img_local,THUMB_NAME);
            File parent = destFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (destFile.exists()) {
                destFile.delete();
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(templateInfo.temp_img)
                    .addHeader("Content-Type", "application/octet-stream")
                    .addHeader("Accept-Encoding", "identity").build();
            InputStream input = null;
            OutputStream output = null;
            try {
                destFile.createNewFile();
                Response response = client.newCall(request).execute();
                input = response.body().byteStream();
                output = new BufferedOutputStream(new FileOutputStream(destFile));

                int count;
                byte[] buffer = new byte[1024 * 5];
                while (0 < (count = input.read(buffer))) {
                    output.write(buffer, 0, count);
                }
            }catch (IOException e){

            } finally {
                if (null != input) {
                    try {
                        input.close();
                    }catch (Exception e){

                    }
                }
                if (null != output) {
                    try {
                        output.close();
                    }catch (Exception e){

                    }
                }
            }
        }

    }

}
