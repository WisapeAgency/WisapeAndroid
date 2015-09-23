package com.wisape.android.cordova;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.wisape.android.WisapeApplication;
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
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.Requester;
import com.wisape.android.util.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
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

    private static final int WHAT_GET_STAGE_CATEGORY = 0x01;
    private static final int WHAT_GET_STAGE_LIST = 0x02;
    private static final int WHAT_START = 0x03;
    private static final int WHAT_SAVE = 0x04;
    private static final int WHAT_PREVIEW = 0x05;
    private static final int WHAT_PUBLISH = 0x06;
    private static final int WHAT_GET_FONTS = 0x07;
    private static final int WHAT_DOWNLOAD_FONT = 0x08;

    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "extra_template_id";
    private static final String EXTRA_FONT_NAME = "extra_font_name";
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
        if (null == action || 0 == action.length()) {
            return true;
        }
        this.callbackContext = callbackContext;
        if (ACTION_GET_STAGE_CATEGORY.equals(action)) {//getStageCategory
            System.out.println("getStageCategory");
            startLoad(WHAT_GET_STAGE_CATEGORY, null);
        } else if (ACTION_GET_STAGE_LIST.equals(action)) {//getStageList
            System.out.println("getStageList:" + args.getInt(0));
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(0));//
            }
            startLoad(WHAT_GET_STAGE_LIST, bundle);
        } else if (ACTION_START.equals(action)) {//start
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putInt(EXTRA_TEMPLATE_ID, args.getInt(0));
                bundle.putInt(EXTRA_CATEGORY_ID, args.getInt(1));
            }
            startLoad(WHAT_START, bundle);
        } else if (ACTION_READ.equals(action)) {//read
            if (null != args && args.length() != 0) {
                String path = args.getString(0);//
                String content = readHtml(path);
                callbackContext.success(content);
            }
        } else if (ACTION_MUSIC_PATH.equals(action)) {//getMusicPath
            if (null != args && args.length() == 1) {
                int id = args.getInt(0);
                getMusicPath(id);
            }
        } else if (ACTION_GET_FONTS.equals(action)) {//getFonts
            startLoad(WHAT_GET_FONTS, null);
        } else if (ACTION_DOWNLOAD_FONT.equals(action)) {
            Bundle bundle = new Bundle();
            if (null != args && args.length() != 0) {
                bundle.putString(EXTRA_FONT_NAME, args.getString(0));
            }
            startLoad(WHAT_DOWNLOAD_FONT, bundle);
        } else if (ACTION_SAVE.equals(action)) {//save
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 3) {
//                bundle.putInt(EXTRA_STORY_ID, args.optInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(0));
                bundle.putString(EXTRA_FILE_PATH, args.getString(1));
            }
            startLoad(WHAT_SAVE, bundle);
        } else if (ACTION_PREVIEW.equals(action)) {//preview
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 2) {
//                bundle.putInt(EXTRA_STORY_ID, args.optInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(0));
                bundle.putString(EXTRA_FILE_PATH, args.getString(1));
            }
            startLoad(WHAT_PREVIEW, bundle);
        } else if (ACTION_PUBLISH.equals(action)) {//publish
            Bundle bundle = new Bundle();
            if (null != args && args.length() == 2) {
//                bundle.putInt(EXTRA_STORY_ID, args.optInt(0));//story_id
                bundle.putString(EXTRA_STORY_HTML, args.getString(0));
                bundle.putString(EXTRA_FILE_PATH, args.getString(1));
            }
            startLoad(WHAT_PUBLISH, bundle);
        } else if (ACTION_SETTING.equals(action)) {
            StorySettingsActivity.launch(getCurrentActivity(), 0);
        } else if (ACTION_BACK.equals(action)) {
            cordova.getActivity().finish();
        } else if (ACTION_EDIT.equals(action)) {
            StoryEntity storyEntity = app.getStoryEntity();
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
        }
        return true;
    }

    private void doEditStory(StoryEntity storyEntity) {
        File htmlFile = new File(storyEntity.storyLocal, FILE_NAME_STORY);
        String html = readFile(htmlFile.getAbsolutePath());
        StoryTemplateActivity.launch(getCurrentActivity(), html, 0);
//        callbackContext.success(html);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Context context = getCurrentActivity().getApplicationContext();
        switch (what) {
            default:
                return null;
            case WHAT_GET_STAGE_CATEGORY: {
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
//                List<StoryTemplateInfo> entities = app.getTemplateMap().get(type);
                List<StoryTemplateInfo> entities = logic.listStoryTemplateLocalByType(context, type);
                System.out.println(entities.toString());
//                System.out.println(entities.toString());
                callbackContext.success(new Gson().toJson(entities));
                break;
            }
            case WHAT_START: {
                ApiStory.AttrTemplateInfo attr = new ApiStory.AttrTemplateInfo();
                attr.id = args.getInt(EXTRA_TEMPLATE_ID, 0);
                int categoryId = args.getInt(EXTRA_CATEGORY_ID, 1);
                Requester.ServerMessage message = logic.getStoryTemplateUrl(context, attr, null);
                if (!message.succeed()) {
                    callbackContext.error(message.status);
                    return null;
                }
                if (cordova.getActivity() instanceof StoryTemplateActivity) {
                    StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                    try {
                        activity.downloadTemplate(message.data.toString(), attr.id, categoryId);
                    } catch (JSONException e) {
                        callbackContext.error(-1);
                    }
                }
                break;
            }
            case WHAT_GET_FONTS: {

                StoryFontInfo[] fonts = logic.listFont(context, "getFonts");
                List<StoryFontInfo> fontList = Arrays.asList(fonts);
                System.out.println(fontList);
                getFonts(fontList);
            }
            case WHAT_DOWNLOAD_FONT: {
                String fontName = args.getString(EXTRA_FONT_NAME);
                if (cordova.getActivity() instanceof StoryTemplateActivity) {
                    StoryTemplateActivity activity = (StoryTemplateActivity) cordova.getActivity();
                    activity.downloadFont(fontName);
                }
                break;
            }
            case WHAT_SAVE: {
//                int storyId = args.getInt(EXTRA_STORY_ID, 0);
                String html = args.getString(EXTRA_STORY_HTML);
                String path = args.getString(EXTRA_FILE_PATH);
                com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);
                WisapeApplication app = WisapeApplication.getInstance();
                StoryEntity story = app.getStoryEntity();
//                logic.saveStoryLocal(context,story);
                File myStory = new File(StoryManager.getStoryDirectory(), story.storyLocal);
                if (!myStory.exists()) {
                    myStory.mkdirs();
                }
                if (!saveStory(myStory, html, paths)) {
                    callbackContext.error(-1);
                    return null;
                }
                cordova.getActivity().finish();
                break;
            }
            case WHAT_PREVIEW: {
//                int storyId = args.getInt(EXTRA_STORY_ID, 0);
                String html = args.getString(EXTRA_STORY_HTML);
                String path = args.getString(EXTRA_FILE_PATH);
                com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);
                WisapeApplication app = WisapeApplication.getInstance();
                StoryEntity story = app.getStoryEntity();
//                logic.saveStoryLocal(context,story);
                File myStory = new File(StoryManager.getStoryDirectory(), story.storyLocal);
                if (!myStory.exists()) {
                    myStory.mkdirs();
                }
                if (!saveStory(myStory, html, paths)) {
                    callbackContext.error(-1);
                    return null;
                }
                File previewFile = new File(myStory, FILE_NAME_PREVIEW);
                if (saveStoryPreview(previewFile, html, story)) {
                    StoryPreviewActivity.launch(cordova.getActivity(), previewFile.getAbsolutePath());
                } else {
                    callbackContext.error(-1);
                }
                break;
            }
            case WHAT_PUBLISH: {
                String html = args.getString(EXTRA_STORY_HTML);
                String path = args.getString(EXTRA_FILE_PATH);
                com.alibaba.fastjson.JSONArray paths = JSON.parseArray(path);
                WisapeApplication app = WisapeApplication.getInstance();
                StoryEntity story = app.getStoryEntity();
                File myStory = new File(StoryManager.getStoryDirectory(), story.storyLocal);
                if (!myStory.exists()) {
                    myStory.mkdirs();
                }
                if (!saveStory(myStory, html, paths)) {
                    callbackContext.error(-1);
                    return null;
                }
                if (Utils.isEmpty(story.storyThumbUri) || !new File(story.storyThumbUri).exists()) {
                    com.wisape.android.util.FileUtils.copyAssetsFile(getCurrentActivity(), "www/public/img/photo_cover.png",
                            new File(StoryManager.getStoryDirectory(), story.storyLocal + "/thumb.jpg").getAbsolutePath());
                }
                ApiStory.AttrStoryInfo storyAttr = new ApiStory.AttrStoryInfo();

                Uri thumb = Uri.parse((new File(StoryManager.getStoryDirectory(), story.storyLocal + "/thumb.jpg")).getAbsolutePath());
                storyAttr.attrStoryThumb = thumb;
                storyAttr.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
                storyAttr.story = Uri.fromFile(new File(StoryManager.getStoryDirectory(), story.storyLocal));
                storyAttr.storyName = story.storyName;
                if (Utils.isEmpty(story.storyMusicName)) {
                    storyAttr.bgMusic = "";
                } else {
                    storyAttr.bgMusic = story.storyMusicName;
                }
                storyAttr.storyDescription = story.storyDesc;
                storyAttr.userId = WisapeApplication.getInstance().getUserInfo().user_id;
                storyAttr.storyStatus = ApiStory.AttrStoryInfo.STORY_STATUS_RELEASE;
                storyAttr.imgPrefix = StoryManager.getStoryDirectory().getAbsolutePath();
                storyAttr.sid = story.storyServerId;

                logic.update(cordova.getActivity().getApplicationContext(), storyAttr, "release");

                Intent intent = new Intent();
                intent.setAction(StoryBroadcastReciver.STORY_ACTION);
                intent.putExtra(StoryBroadcastReciver.EXTRAS_TYPE, StoryBroadcastReciverListener.TYPE_ADD_STORY);
                getCurrentActivity().sendBroadcast(intent);

                StoryReleaseActivity.launch(cordova.getActivity());
            }
        }
        return null;
    }

    private boolean saveStory(File myStory, String html, com.alibaba.fastjson.JSONArray paths) {
        for (int i = 0; i < paths.size(); i++){
            String path = paths.getString(i);
            System.out.println("saveStory:" + path);
            File imagePath = new File(path).getParentFile();
            if (imagePath.getParentFile().getName().equals(StoryManager.TEMPLATE_DIRECTORY)){
                String templateName = imagePath.getName();
                File newImagePath = new File(myStory.getAbsolutePath(),templateName);
                html = html.replace(imagePath.getAbsolutePath(), newImagePath.getAbsolutePath());
            }else{
                html = html.replace(imagePath.getAbsolutePath(), myStory.getAbsolutePath());
            }
        }
        File storyHTML = new File(myStory, FILE_NAME_STORY);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(storyHTML);
            writer.write(html);
            writer.close();
        } catch (IOException e) {
            Log.e("saveStory", "", e);
            return false;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        File storyImg = new File(myStory, DIR_NAME_IMAGE);
        if (!storyImg.exists()) {
            storyImg.mkdirs();
        }
        for (int i = 0; i < paths.size(); i++) {
            try {
                String path = paths.getString(i);
                File imagePath = new File(path).getParentFile();
                String newPath;
                if (imagePath.getParentFile().getName().equals(StoryManager.TEMPLATE_DIRECTORY)){
                    String templateName = imagePath.getName();
                    File newImagePath = new File(myStory.getAbsolutePath(),templateName);
                    newPath = path.replace(imagePath.getAbsolutePath(), newImagePath.getAbsolutePath());
                }else{
                    newPath = path.replace(imagePath.getAbsolutePath(), myStory.getAbsolutePath());
                }
                File file = new File(path);
                File newPathFile = new File(newPath);
                File imgDirectory = newPathFile.getParentFile();
                if (!imgDirectory.exists()) {
                    imgDirectory.mkdirs();
                }
                FileUtils.copyFile(file, new File(newPathFile, file.getName()));
            } catch (IOException e) {
                Log.e("saveStory", "", e);
            }
        }
        return true;
    }

    private boolean saveStoryPreview(File previewFile, String html, StoryEntity story) {
        String header = getFromAssets(PREVIEW_HEADER);
        String footer = getFromAssets(PREVIEW_FOOTER);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(previewFile);
            writer.println(header);
            writer.println(html);
            if (!"".equals(story.storyMusicLocal)) {
                writer.println("<div id=\"audio-btn\" class=\"on\">");
                writer.println(String.format("    <audio loop=\"loop\" src=\"%s\" id=\"media\" preload=\"preload\"></audio>",
                        story.storyMusicLocal));
                writer.println("</div>");
            }
            writer.println(footer);
            writer.close();
        } catch (IOException e) {
            Log.e("saveStoryPreview", "", e);
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
            while ((line = bufReader.readLine()) != null)
                result.append(line);
            return result.toString();
        } catch (Exception e) {
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
            return content.toString();
        } catch (IOException e) {
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
        List<StoryFontInfo> resultFontlist = new ArrayList<>();
        try {
            File fontDirectory = StoryManager.getStoryFontDirectory();
            File fontFile = new File(fontDirectory, FILE_NAME_FONT);
            File[] fonts = fontDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            json.put("filePath", fontFile.getAbsolutePath());
            if (fontList == null || fontList.size() == 0) {
                StoryFontInfo fontInfo = null;
                for (File font : fonts) {
                    fontInfo = new StoryFontInfo();
                    fontInfo.name = font.getName();
                    fontInfo.downloaded = 1;
                    resultFontlist.add(fontInfo);
                }
            } else {
                int serverFontSize = fontList.size();
                int localFontSize = fonts.length;
                //如果服务器的字体数量大于本地数量
                if (serverFontSize > localFontSize) {
                    for (int i = 0; i < serverFontSize; i++) {
                        StoryFontInfo serverFontInfo = fontList.get(i);
                        if (i >= fonts.length) {
                            resultFontlist.add(serverFontInfo);
                        } else {
                            String localFonInName = fonts[i].getName();
                            if (localFonInName.equals(serverFontInfo.name)) {
                                serverFontInfo.downloaded = 1;
                            } else {
                                serverFontInfo.downloaded = 0;
                            }
                            resultFontlist.add(serverFontInfo);
                        }
                    }
                } else {
                    for (int i = 0; i < fonts.length; i++) {
                        String localFontName = fonts[i].getName();
                        if (i >= fontList.size()) {
                            StoryFontInfo storyFontInfo = new StoryFontInfo();
                            storyFontInfo.name = localFontName;
                            storyFontInfo.downloaded = 1;
                            resultFontlist.add(storyFontInfo);
                        } else {
                            StoryFontInfo storyFontInfo = fontList.get(i);
                            if (localFontName.equals(storyFontInfo.name)) {
                                storyFontInfo.downloaded = 1;
                            } else {
                                storyFontInfo.downloaded = 0;
                            }
                            resultFontlist.add(storyFontInfo);
                        }
                    }
                }

            }
            json.put("fonts", resultFontlist);
        } catch (JSONException e) {
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
}
