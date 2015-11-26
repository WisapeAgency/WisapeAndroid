package com.wisape.android.cordova;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.StoryMusicActivity;
import com.wisape.android.activity.StoryPreviewActivity;
import com.wisape.android.activity.StoryReleaseActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.network.Requester;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.CustomProgress;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tony on 2015/7/19.
 */
public class StoryTemplatePlugin extends AbsPlugin {
    private static final String FILE_NAME_THUMB = "thumb.jpg";
    private static final String FILE_NAME_STORY = "story.html";
    private static final String FILE_NAME_PREVIEW = "preview.html";
    private static final String FILE_NAME_TEMPLATE = "stage.html";
    private static final String FILE_NAME_FONT = "fonts.css";
    private static final String DIR_NAME_IMAGE = "img";
    private static final String PREVIEW_HEADER = "www/views/header.html";
    private static final String PREVIEW_FOOTER = "www/views/footer.html";
    private static final String PLACE_HODLER_FONT_CSS = "FONT_STYLE_FILE_LOCATION";
    private static final String WISAPE_SD_CARD_LOCATION = "/WISAPE_SD_CARD_LOCATION/";

    public static final String ACTION_GET_STAGE_CATEGORY = "getStageCategory";
    public static final String ACTION_GET_STAGE_LIST = "getStageList";
    public static final String ACTION_START = "start";
    public static final String ACTION_READ = "read";
    public static final String ACTION_GET_FONTS = "getFonts";
    public static final String ACTION_DOWNLOAD_FONT = "downloadFont";
    public static final String ACTION_MUSIC_PATH = "getMusicPath";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_PREVIEW = "preview";
    public static final String ACTION_PUBLISH = "publish";
    public static final String ACTION_SETTING = "setting";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_GET_CONTENT = "getContent";
    public static final String ACTION_INIT_STATE = "checkInitState";
    public static final String ACTION_OPEN_LINK = "openLink";
    public static final String ACTION_IM_SAVE="isave";

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "extra_template_id";
    private static final String EXTRA_FONT_NAME = "extra_font_name";
    private static final String EXTRA_STORY_THUMB = "extra_story_thumb";
    private static final String EXTRA_STORY_HTML = "extra_story_html";
    private static final String EXTRA_FILE_PATH = "extra_file_path";

    private CustomProgress customProgress;
    private StoryLogic logic = StoryLogic.instance();


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private interface TemplateOp {
        void run(Bundle bundle) throws Exception;
    }

    private void threadhelper(final TemplateOp f, final Bundle bundle, final CallbackContext callbackContext){
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    f.run(bundle);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (null == action || 0 == action.length()) {
            return true;
        }
        LogUtil.d("前端调用原生提供接口动作:" + action);
        final Context context = getCurrentActivity().getApplicationContext();
        if (ACTION_GET_STAGE_CATEGORY.equals(action)) {//getStageCategory
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    JSONArray jsonStr = logic.listStoryTemplateTypeLocal(context);
                    callbackContext.success(jsonStr);
                }
            }, null, callbackContext);
        } else if (ACTION_GET_STAGE_LIST.equals(action)) {//getStageList
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));//
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    int type = bundle.getInt(EXTRA_CATEGORY_ID, 0);
                    List<StoryTemplateInfo> entities = logic.listStoryTemplateLocalByType(context, type);
                    LogUtil.d("story模版信息:" + entities.toString());
                    callbackContext.success(new Gson().toJson(entities));
                }
            }, bundle, callbackContext);
        } else if (ACTION_START.equals(action)) {//start
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putInt(EXTRA_TEMPLATE_ID, args.getInt(0));
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(1));
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
                    attr.id = bundle.getInt(EXTRA_TEMPLATE_ID, 0);
                    int categoryId = bundle.getInt(EXTRA_CATEGORY_ID, 1);
                    Requester.ServerMessage message = logic.getStoryTemplateUrl(context, attr, null);
                    if (!message.succeed()) {
                        callbackContext.error(message.status);
                        return;
                    }
                    if (cordova.getActivity() instanceof StoryTemplateActivity) {
                        StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                        try {
                            activity.downloadTemplate(message.data.toString(), attr.id, categoryId);
                        } catch (JSONException e) {
                            callbackContext.error(-1);
                        }
                    }
                    callbackContext.success();
                }
            }, bundle, callbackContext);
        } else if (ACTION_READ.equals(action)) {//read
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    if (null != args && args.length() != 0) {
                        String path = args.optString(0);//
                        String content = readHtml(path);
                        callbackContext.success(content);
                    }
                }
            }, null, callbackContext);
        } else if (ACTION_MUSIC_PATH.equals(action)) {//getMusicPath
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    if (null != args && args.length() == 1) {
                        int id = args.optInt(0);
                        getMusicPath(callbackContext, id);
                    }
                }
            }, null, callbackContext);
        } else if (ACTION_GET_FONTS.equals(action)) {//getFonts
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    StoryFontInfo[] fonts = logic.listFont(context, "getFonts");
                    List<StoryFontInfo> fontList = Arrays.asList(fonts);
                    System.out.println(fontList);
                    getFonts(callbackContext, fontList);
                }
            }, null, callbackContext);
        } else if (ACTION_DOWNLOAD_FONT.equals(action)) {
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putString(EXTRA_FONT_NAME, args.getString(0));
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String fontName = bundle.getString(EXTRA_FONT_NAME);
                    if (cordova.getActivity() instanceof StoryTemplateActivity) {
                        StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                        activity.downloadFont(fontName);
                    }
                    callbackContext.success();
                }
            }, bundle, callbackContext);
        } else if (ACTION_SAVE.equals(action)) {//save
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
                bundle.putString(EXTRA_STORY_THUMB, args.getString(0));
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {

                    String storyThumb = bundle.getString(EXTRA_STORY_THUMB);
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    String path = bundle.getString(EXTRA_FILE_PATH);
                    com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);

                    StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
                    story.status = ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
                    File myStory = new File(StoryManager.getStoryDirectory(), story.storyLocal);

                    if (!myStory.exists()) {
                        myStory.mkdirs();
                    }

                    if (!saveStory(myStory, story, storyThumb, html, paths)) {
                        callbackContext.error(-1);
                        return;
                    }
                    if(!StoryLogic.instance().updateStory(WisapeApplication.getInstance(), story)){
                        callbackContext.error(-1);
                        return;
                    }
                    callbackContext.success();
                    MainActivity.launch(getCurrentActivity());
                    cordova.getActivity().finish();
                }
            }, bundle, callbackContext);
        } else if (ACTION_PREVIEW.equals(action)) {//preview
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
                bundle.putString(EXTRA_STORY_THUMB, args.getString(0));
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String storyThumb = bundle.getString(EXTRA_STORY_THUMB);
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    String path = bundle.getString(EXTRA_FILE_PATH);
                    com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);

                    StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
                    story.status = ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
                    File myStory = new File(StoryManager.getStoryDirectory(), story.storyLocal);
                    if (!myStory.exists()) {
                        myStory.mkdirs();
                    }

                    if (!saveStory(myStory, story, storyThumb, html, paths)) {
                        callbackContext.error(-1);
                        return;
                    }
                    StoryLogic.instance().saveStoryEntityToShare(story);
                    File previewFile = new File(myStory, FILE_NAME_PREVIEW);
                    if (saveStoryPreview(previewFile, html, story)) {
                        callbackContext.success();
                        StoryPreviewActivity.launch(cordova.getActivity(), previewFile.getAbsolutePath());
                    } else {
                        callbackContext.error(-1);
                    }
                }
            }, bundle, callbackContext);
        } else if (ACTION_PUBLISH.equals(action)) {//publish
            callbackContext.success();
            StoryReleaseActivity.launch(cordova.getActivity());
            getCurrentActivity().finish();
        } else if (ACTION_SETTING.equals(action)) {
            StoryMusicEntity musicEntity = new StoryMusicEntity();
            StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
            musicEntity.musicLocal = storyEntity.storyMusicLocal;
            musicEntity.name = storyEntity.storyMusicName;
            musicEntity.serverId = storyEntity.musicServerId;
            callbackContext.success();
            StoryMusicActivity.launch(getCurrentActivity(),musicEntity,0);
        } else if (ACTION_BACK.equals(action)) {
            callbackContext.success();
            cordova.getActivity().finish();
        } else if (ACTION_EDIT.equals(action)) {
            StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
            if (storyEntity != null) {
                doEditStory(storyEntity);
            }
            callbackContext.success();
            cordova.getActivity().finish();
        } else if (ACTION_GET_CONTENT.equals(action)) {
            if (cordova.getActivity() instanceof StoryTemplateActivity) {
                StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                String html = activity.getContent();
                LogUtil.d("获取内容数据的内容:" + html);
                callbackContext.success(html);
            }
        } else if (ACTION_INIT_STATE.equals(action)) {
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    while (DataSynchronizer.getInstance().isDownloading()) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            LogUtil.e("休眠被打断:",e);
                        }
                    }
                    StoryTemplateInfo templateInfo = DataSynchronizer.getInstance().getFirstTemplate();
                    String content = "";
                    if (templateInfo != null) {
                        File path = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + "/" + "stage.html");
                        content = readHtml(path.getAbsolutePath());
                        StoryLogic.instance().saveTempFirstPage(content);
                    }else {
                        LogUtil.d("获取第一个page信息为null使用默认信息");
                        content = StoryLogic.instance().getTempFirstPage();
                    }
                    callbackContext.success(content);
                }
            }, null, callbackContext);
        } else if (ACTION_OPEN_LINK.equals(action)) {
            if (null != args && args.length() == 1) {
                String url = args.getString(0);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                cordova.getActivity().startActivity(intent);
            }
            callbackContext.success();
        }else if(ACTION_IM_SAVE.equals(action)){
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
                bundle.putString(EXTRA_STORY_THUMB, args.getString(0));
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    StoryLogic.instance().saveTempHtml(html);
                }
            }, bundle, callbackContext);
        }
        return true;
    }


    private void doEditStory(StoryEntity storyEntity) {
        File file = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal + "/story.html");
        String html = readFile(file.getAbsolutePath());
        File dataFile = EnvironmentUtils.getAppDataDirectory();
        html = html.replace(WISAPE_SD_CARD_LOCATION, dataFile.getAbsolutePath());
        StoryTemplateActivity.launch(getCurrentActivity(), html, 0);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        return Message.obtain();
    }

    @Override
    protected void onLoadCompleted(Message data) {
        closeProgressDialog();
    }

    /**
     * 保存story.html文件
     *
     * @param myStory    文件根目录
     * @param story
     * @param storyThumb 封面
     * @param html       文件内容
     * @param paths      需要替换的路径
     * @return
     */
    private boolean saveStory(File myStory, StoryEntity story, String storyThumb, String html, com.alibaba.fastjson.JSONArray paths) {
        if (paths == null || paths.size() == 0) {
            return false;
        }

        //生成story字符串
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.getString(i);
            File imagePath = new File(path).getParentFile().getParentFile();
            if (imagePath.getParentFile().getName().equals(StoryManager.TEMPLATE_DIRECTORY)) {
                String templateName = imagePath.getName();
                File newImagePath = new File(myStory.getAbsolutePath(), templateName);
                html = html.replace(imagePath.getAbsolutePath(), newImagePath.getAbsolutePath());
            }
        }

        File dataFile = EnvironmentUtils.getAppDataDirectory();
        html = html.replace(dataFile.getAbsolutePath(), WISAPE_SD_CARD_LOCATION);

        File storyHTML = new File(myStory, FILE_NAME_STORY);
        if(storyHTML.exists()){
            LogUtil.d("删除本地storyHtml:" + storyHTML.delete());
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(storyHTML);
            writer.write(html);
            writer.close();
            LogUtil.d("生成story.html文件成功");
        } catch (IOException e) {
            LogUtil.e("保存story文件出错", e);
            return false;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        //没有设置过封面，由story的第一个模板背景做封面
        if (story.localCover == 0) {
            try {
                FileUtils.copyFile(new File(storyThumb),
                        new File(StoryManager.getStoryDirectory(), story.storyLocal + "/thumb.jpg"));
                LogUtil.d("复制封面成功");
            } catch (IOException e) {
                LogUtil.e("生成封面出错", e);
            }
        }

        File storyImg = new File(myStory, DIR_NAME_IMAGE);
        if (!storyImg.exists()) {
            storyImg.mkdirs();
        }

        for (int i = 0; i < paths.size(); i++) {
            try {
                String path = paths.getString(i);
                File imagePath = new File(path).getParentFile().getParentFile();

                String newPath = path;
                if (imagePath.getParentFile().getName().equals(StoryManager.TEMPLATE_DIRECTORY)) {
                    String templateName = imagePath.getName();
                    File newImagePath = new File(myStory.getAbsolutePath(), templateName);
                    newPath = path.replace(imagePath.getAbsolutePath(), newImagePath.getAbsolutePath());
                }
                File file = new File(path);
                File newPathFile = new File(newPath);
                File imgDirectory = newPathFile.getParentFile();
                if (!imgDirectory.exists()) {
                    imgDirectory.mkdirs();
                }
                File targetFile = new File(imgDirectory, file.getName());
                if(!targetFile.exists()){
                    FileUtils.copyFile(file, targetFile);
                }
            } catch (IOException e) {
                LogUtil.e("保存story需要资源文件出错", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 保存预览wenjian
     *
     * @param previewFile 预览文件
     * @param html        预览文件内容
     * @param story       story实体
     * @return
     */
    private boolean saveStoryPreview(File previewFile, String html, StoryEntity story) {
        if(previewFile.exists()){
            LogUtil.d("删除本地previewFile:" + previewFile.delete());
        }
        String header = getFromAssets(PREVIEW_HEADER);
        String footer = getFromAssets(PREVIEW_FOOTER);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(previewFile);
            writer.println(header);
            writer.println(html);
            if (!Utils.isEmpty(story.storyMusicLocal)) {
                writer.println("<div id=\"audio-btn\" class=\"on\" onclick=\"lanren.changeClass(this,'media')\">");
                writer.println(String.format("    <audio loop=\"loop\" src=\"%s\" id=\"media\" preload=\"preload\"></audio>",
                        story.storyMusicLocal));
                writer.println("</div>");
            }
            writer.println(footer);
            writer.close();
        } catch (IOException e) {
            LogUtil.e("保存预览story失败:", e);
            return false;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return true;
    }

    private void copyAssetsFile(String src, String dest) {
        InputStream in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            in = cordova.getActivity().getAssets().open(src);
            byte[] buffer = new byte[1024];
            int length = in.read(buffer);
            while (length > 0) {
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }
            out.flush();
            in.close();
            out.close();
        } catch (IOException e) {
            LogUtil.e("复制assets文件出错:" + src, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private String readFile(String path) {
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            return content.toString();
        } catch (IOException e) {
            LogUtil.e("根据文件路径读取文件出错:" + path, e);
            return "";
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }

            }
        }
    }

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(cordova.getActivity().getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
            File fontDirectory = StoryManager.getStoryFontDirectory();
            String fontFile = new File(fontDirectory, FILE_NAME_FONT).getAbsolutePath();
            return result.toString().replace(PLACE_HODLER_FONT_CSS, fontFile);
        } catch (Exception e) {
            LogUtil.e("从assetes文件读取出错:" + fileName, e);
            e.printStackTrace();
        }
        return "";
    }

    private String readHtml(String path) {
        String parent = new File(path).getParent();
        StringBuilder content = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            Pattern p = Pattern.compile("img/[a-zA-Z0-9-_]+(\\.jpg|\\.png)");
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                while (m.find()) {
                    String result = m.group();
                    line = line.replace(result, new File(parent, result).getAbsolutePath());
                }
                content.append(line);
            }
            reader.close();
            LogUtil.d("读取html文件内容:" + content.toString());
            return content.toString();
        } catch (IOException e) {
            LogUtil.e("读取html文件内容出错:" + path, e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }

            }
        }
    }

    private void getFonts(CallbackContext callbackContext, List<StoryFontInfo> fontList) {
        JSONObject json = new JSONObject();
        try {
            File fontDirectory = StoryManager.getStoryFontDirectory();
            File fontFile = new File(fontDirectory, FILE_NAME_FONT);
            if (!fontFile.exists()) {
                fontFile.createNewFile();
            }
            json.put("filePath", fontFile.getAbsolutePath());

            for (StoryFontInfo font : fontList) {
                File fontNameDirectory = new File(StoryManager.getStoryFontDirectory(), font.name);
                File previewFile = new File(fontNameDirectory, "preview.jpg");
                if (previewFile.exists()) {
                    font.preview_img_local = previewFile.getAbsolutePath();
                }
                font.downloaded = fontNameDirectory.listFiles().length == 5 ? 1 : 0;
            }
            json.put("fonts", fontList);
        } catch (Exception e) {
            callbackContext.success(1);
        }
        callbackContext.success(json);
    }

    private void getMusicPath(CallbackContext callbackContext, int id) {
        Context context = getCurrentActivity().getApplicationContext();
        StoryMusicEntity music = logic.getStoryMusicLocalById(context, id);
        if (music != null) {
            callbackContext.success(music.musicLocal);
        } else {
            callbackContext.error(1);//not fond
        }
    }

    /**
     * 显示进度对话框
     */
    public void showProgressDialog() {

        if (customProgress == null) {
            customProgress = CustomProgress.show(getCurrentActivity(), getCurrentActivity().getResources()
                    .getString(R.string.progress_loading_data), true);
        }
        if (customProgress.isShowing()) {
            customProgress.setMessage(getCurrentActivity().getResources().getString(R.string.progress_loading_data));
            return;
        }
        customProgress.setMessage(getCurrentActivity().getResources().getString(R.string.progress_loading_data));
        customProgress.show();
    }

    /**
     * 关闭进度对话框
     */
    public void closeProgressDialog() {
        if (customProgress != null && customProgress.isShowing()) {
            customProgress.dismiss();
        }
    }
}