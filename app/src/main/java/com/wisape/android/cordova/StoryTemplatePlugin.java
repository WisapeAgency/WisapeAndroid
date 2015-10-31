package com.wisape.android.cordova;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.StoryMusicActivity;
import com.wisape.android.activity.StoryPreviewActivity;
import com.wisape.android.activity.StoryReleaseActivity;
import com.wisape.android.activity.StorySettingsActivity;
import com.wisape.android.activity.StoryTemplateActivity;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.content.StoryBroadcastReciver;
import com.wisape.android.content.StoryBroadcastReciverListener;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.database.StoryMusicEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.network.Requester;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.CustomProgress;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
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
    public static final String ACTION_CHECK_DOWNLOAD = "checkInitState";//onInitCompleted
    public static final String ACTION_OPEN_LINK = "openLink";

    private static final int WHAT_GET_STAGE_CATEGORY = 0x01;
    private static final int WHAT_GET_STAGE_LIST = 0x02;
    private static final int WHAT_START = 0x03;
    private static final int WHAT_SAVE = 0x04;
    private static final int WHAT_PREVIEW = 0x05;
    private static final int WHAT_PUBLISH = 0x06;
    private static final int WHAT_GET_FONTS = 0x07;
    private static final int WHAT_DOWNLOAD_FONT = 0x08;
    private static final int WHAT_EDIT_INIT = 0x09;

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "extra_template_id";
    private static final String EXTRA_FONT_NAME = "extra_font_name";
    private static final String EXTRA_STORY_THUMB = "extra_story_thumb";
    private static final String EXTRA_STORY_HTML = "extra_story_html";
    private static final String EXTRA_FILE_PATH = "extra_file_path";

    private CustomProgress customProgress;
    private StoryLogic logic = StoryLogic.instance();
    private WisapeApplication app = WisapeApplication.getInstance();
    private CallbackContext callbackContext;
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

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    private interface TemplateOp {
        void run(Bundle bundle) throws Exception;
    }


    /* helper to execute functions async and handle the result codes
     *
     */
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
        this.callbackContext = callbackContext;
        final Context context = getCurrentActivity().getApplicationContext();
        if (ACTION_GET_STAGE_CATEGORY.equals(action)) {//getStageCategory
//            System.out.println("getStageCategory");
//            startLoad(WHAT_GET_STAGE_CATEGORY, null);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    JSONArray jsonStr = logic.listStoryTemplateTypeLocal(context);
                    LogUtil.d("模版分类信息:" + jsonStr);
                    callbackContext.success(jsonStr);
                    System.out.println(jsonStr.toString());
                }
            }, null, callbackContext);
        } else if (ACTION_GET_STAGE_LIST.equals(action)) {//getStageList
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));//
            }
//            startLoad(WHAT_GET_STAGE_LIST, bundle);
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
//            startLoad(WHAT_START, bundle);
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
                }
            }, bundle, callbackContext);
        } else if (ACTION_READ.equals(action)) {//read
//            if (null != args && args.length() != 0) {
//                String path = args.getString(0);//
//                String content = readHtml(path);
//                callbackContext.success(content);
//            }
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
//            if (null != args && args.length() == 1) {
//                int id = args.getInt(0);
//                getMusicPath(id);
//            }
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    if (null != args && args.length() == 1) {
                        int id = args.optInt(0);
                        getMusicPath(id);
                    }
                }
            }, null, callbackContext);
        } else if (ACTION_GET_FONTS.equals(action)) {//getFonts
//            startLoad(WHAT_GET_FONTS, null);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    StoryFontInfo[] fonts = logic.listFont(context, "getFonts");
                    List<StoryFontInfo> fontList = Arrays.asList(fonts);
                    System.out.println(fontList);
                    getFonts(fontList);
                }
            }, null, callbackContext);
        } else if (ACTION_DOWNLOAD_FONT.equals(action)) {
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putString(EXTRA_FONT_NAME, args.getString(0));
            }
//            startLoad(WHAT_DOWNLOAD_FONT, bundle);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String fontName = bundle.getString(EXTRA_FONT_NAME);
                    if (cordova.getActivity() instanceof StoryTemplateActivity) {
                        StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                        activity.downloadFont(fontName);
                    }
                }
            }, bundle, callbackContext);
        } else if (ACTION_SAVE.equals(action)) {//save
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
                bundle.putString(EXTRA_STORY_THUMB, args.getString(0));
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
//            startLoad(WHAT_SAVE, bundle);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String storyThumb = bundle.getString(EXTRA_STORY_THUMB);
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    String path = bundle.getString(EXTRA_FILE_PATH);
                    com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);

                    LogUtil.d("保存story前端返回的封面路径:" + storyThumb);
                    LogUtil.d("保存story前端返回的需要替换路径:" + paths.toJSONString());

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
                    StoryEntity storyEntity = StoryLogic.instance().updateStory(WisapeApplication.getInstance(), story);
                    StoryLogic.instance().saveStoryEntityToShare(storyEntity);
                    sendBroadcastUpdateStory();
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
//            startLoad(WHAT_PREVIEW, bundle);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String storyThumb = bundle.getString(EXTRA_STORY_THUMB);
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    String path = bundle.getString(EXTRA_FILE_PATH);
                    com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);

                    LogUtil.d("预览story前端返回的封面路径:" + storyThumb);
                    LogUtil.d("预览story前端返回需要替换的路径:" + paths.toJSONString());

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
                    StoryEntity storyEntity = StoryLogic.instance().updateStory(getCurrentActivity(), StoryLogic.instance().getStoryEntityFromShare());
                    LogUtil.d("预览保存story信息:" + storyEntity.storyThumbUri);
                    StoryLogic.instance().saveStoryEntityToShare(storyEntity);
//                sendBroadcastUpdateStory();
                    File previewFile = new File(myStory, FILE_NAME_PREVIEW);
                    if (saveStoryPreview(previewFile, html, story)) {
                        StoryPreviewActivity.launch(cordova.getActivity(), previewFile.getAbsolutePath());
                    } else {
                        callbackContext.error(-1);
                    }
                }
            }, bundle, callbackContext);
        } else if (ACTION_PUBLISH.equals(action)) {//publish
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
                bundle.putString(EXTRA_STORY_THUMB, args.getString(0));
                bundle.putString(EXTRA_STORY_HTML, args.getString(1));
                bundle.putString(EXTRA_FILE_PATH, args.getString(2));
            }
            showProgressDialog();
//            startLoad(WHAT_PUBLISH, bundle);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    String storyThumb = bundle.getString(EXTRA_STORY_THUMB);
                    String html = bundle.getString(EXTRA_STORY_HTML);
                    String path = bundle.getString(EXTRA_FILE_PATH);
                    com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);

                    LogUtil.d("发布story前端返回的封面路径:" + storyThumb);
                    LogUtil.d("发布story前端返回需要替换的路径:" + paths.toJSONString());

                    StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
                    ApiStory.AttrStoryInfo storyAttr = new ApiStory.AttrStoryInfo();
                    storyAttr.story = Uri.fromFile(new File(StoryManager.getStoryDirectory(), story.storyLocal));
                    storyAttr.storyName = story.storyName;
                    if (Utils.isEmpty(story.storyMusicName)) {
                        storyAttr.bgMusic = "";
                    } else {
                        storyAttr.bgMusic = story.storyMusicName;
                    }
                    storyAttr.storyDescription = story.storyDesc;
                    storyAttr.userId = UserLogic.instance().getUserInfoFromLocal().user_id;
                    storyAttr.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
                    storyAttr.imgPrefix = StoryManager.getStoryDirectory().getAbsolutePath() + "/" + story.storyLocal;
                    storyAttr.story_local = story.storyLocal;
                    storyAttr.attrStoryThumb = Uri.parse(storyThumb);

                    if ("-1".equals(story.status)) {
                        storyAttr.sid = -1;
                    } else {
                        storyAttr.sid = story.storyServerId;
                    }

                    logic.update(WisapeApplication.getInstance().getApplicationContext(), storyAttr, "release");
                    sendBroadcastUpdateStory();
                    StoryReleaseActivity.launch(cordova.getActivity());
                    getCurrentActivity().finish();
                }
            }, bundle, callbackContext);
        } else if (ACTION_SETTING.equals(action)) {
            StoryMusicEntity musicEntity = new StoryMusicEntity();
            StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
            String musicName = storyEntity.storyMusicName;
            musicEntity.musicLocal = storyEntity.storyMusicLocal;
            musicEntity.name = storyEntity.storyMusicName;
            musicEntity.serverId = storyEntity.musicServerId;
            StoryMusicActivity.launch(getCurrentActivity(),musicEntity,0);
//            StorySettingsActivity.launch(getCurrentActivity(), 0);
        } else if (ACTION_BACK.equals(action)) {
            cordova.getActivity().finish();
        } else if (ACTION_EDIT.equals(action)) {
            StoryEntity storyEntity = StoryLogic.instance().getStoryEntityFromShare();
            if (storyEntity != null) {
                doEditStory(storyEntity);
            }
            cordova.getActivity().finish();
        } else if (ACTION_GET_CONTENT.equals(action)) {
            if (cordova.getActivity() instanceof StoryTemplateActivity) {
                StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                String html = activity.getContent();
                callbackContext.success(html);
            }
        } else if (ACTION_CHECK_DOWNLOAD.equals(action)) {
//            startLoad(WHAT_EDIT_INIT, null);
            threadhelper(new TemplateOp() {
                public void run(Bundle bundle) {
                    while (DataSynchronizer.getInstance().isDownloading()){
                        try{
                            Thread.sleep(300);
                        }catch (InterruptedException e){

                        }
                    }
                    StoryTemplateInfo templateInfo = DataSynchronizer.getInstance().getFirstTemplate();
                    String content = "";
                    if (templateInfo != null) {
                        File path = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + "/" + "stage.html");
                        content = readHtml(path.getAbsolutePath());
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
        }
        return true;
    }

    private void doEditStory(StoryEntity storyEntity) {
        File file = new File(StoryManager.getStoryDirectory(), storyEntity.storyLocal + "/story.html");
        String html = readFile(file.getAbsolutePath());
        StoryTemplateActivity.launch(getCurrentActivity(), html, 0);
//        callbackContext.success(html);
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
        if (paths == null) {
            return true;
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


        File storyHTML = new File(myStory, FILE_NAME_STORY);
        if(storyHTML.exists()){
            LogUtil.d("删除本地storyHtml:" + storyHTML.delete());
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(storyHTML);
            writer.write(html);
            writer.close();
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
        StringBuffer content = new StringBuffer();
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

    private void getFonts(List<StoryFontInfo> fontList) {
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

    private void getMusicPath(int id) {
        Context context = getCurrentActivity().getApplicationContext();
        StoryMusicEntity music = logic.getStoryMusicLocalById(context, id);
        if (music != null) {
            callbackContext.success(music.musicLocal);
        } else {
            callbackContext.error(1);//not fond
        }
    }

    /**
     * 发送消息通知首页更新数据
     */
    private void sendBroadcastUpdateStory() {
        Intent intent = new Intent();
        intent.setAction(StoryBroadcastReciver.STORY_ACTION);
        intent.putExtra(StoryBroadcastReciver.EXTRAS_TYPE, StoryBroadcastReciverListener.TYPE_ADD_STORY);
        WisapeApplication.getInstance().getApplicationContext().sendBroadcast(intent);
    }
}