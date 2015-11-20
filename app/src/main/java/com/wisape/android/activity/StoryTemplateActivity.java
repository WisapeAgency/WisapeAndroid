package com.wisape.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;

//import com.parse.codec.digest.DigestUtils;
import com.parse.codec.digest.DigestUtils;
import com.wisape.android.R;
import com.wisape.android.WisapeApplication;
import com.wisape.android.api.ApiStory;
import com.wisape.android.common.StoryManager;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.logic.StoryLogic;
import com.wisape.android.logic.UserLogic;
import com.wisape.android.model.StoryFontInfo;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.network.DataSynchronizer;
import com.wisape.android.network.Downloader;
import com.wisape.android.network.FontDownloader;
import com.wisape.android.network.FontPreviewDownloader;
import com.wisape.android.network.TemplateDownloader;
import com.wisape.android.network.ThumbDownloader;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.LogUtil;
import com.wisape.android.util.Utils;
import com.wisape.android.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LeiGuoting on 7/7/15.
 */
public class StoryTemplateActivity extends AbsCordovaActivity {
    private static final String START_URL = "file:///android_asset/www/views/test.html";
    private static final String TEMPLATE_NAME = "stage.html";
    private static final String DOWNLOAD_PROGRESS = "progress";
    private static final String EXTRA_STORY_ID = "story_id";
    private static final String EXTRA_CATEGORY_ID = "extra_category_id";
    private static final String EXTRA_TEMPLATE_ID = "temp_id";
    private static final String EXTRA_TEMPLATE_NAME = "temp_name";
    private static final String EXTRA_TEMPLATE_PATH = "temp_path";
    private static final String EXTRA_TEMPLATE_URL = "temp_url";
    private static final String EXTRA_FONT_NAME = "font_name";
    private static final String EXTRA_EDIT_CONTENT = "html_content";
    private static final String FONT_FAMILY = "font-family";
    private static final String FONT_FILE_NAME = "fonts.css";

    private static final int WHAT_DOWNLOAD_TEMPLATE = 0x01;
    private static final int WHAT_DOWNLOAD_FONT = 0x02;
    private static final int WHAT_DOWNLOAD_PROGRESS = 0x03;
    private static final int WHAT_DOWNLOAD_COMPLETED = 0x04;
    private static final int WHAT_DOWNLOAD_ERROR = 0x05;
    private static final int WHAT_INIT = 0x06;
    private static final int WHAT_INIT_COMPLETED = 0x07;

    public static void launch(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), StoryTemplateActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Activity activity, String html, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), StoryTemplateActivity.class);
        intent.putExtra(EXTRA_EDIT_CONTENT, html);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), StoryTemplateActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    private String html;
    private Handler downloadTemplateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_DOWNLOAD_PROGRESS: {
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID, 0);
                    int categoryId = msg.getData().getInt(EXTRA_CATEGORY_ID, 0);
                    double progress = msg.getData().getDouble(DOWNLOAD_PROGRESS, 0);
                    try {
                        JSONObject json = new JSONObject();
                        json.put("id", id);
                        json.put("category_id", categoryId);
                        json.put("progress", progress);
                        loadUrl("javascript:onDownloading(" + json.toString() + ")");
                    } catch (JSONException e) {

                    }
                    break;
                }
                case WHAT_DOWNLOAD_COMPLETED: {
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID, 0);
                    int categoryId = msg.getData().getInt(EXTRA_CATEGORY_ID, 0);
                    String name = msg.getData().getString(EXTRA_TEMPLATE_NAME);
                    String path = msg.getData().getString(EXTRA_TEMPLATE_PATH);
                    File template = getTemplateUnzipDirectory(name);
                    try {
                        JSONObject json = new JSONObject();
                        json.put("id", id);
                        json.put("category_id", categoryId);
                        json.put("path", template);
                        LogUtil.d("下载模板完成:" + template);
                        loadUrl("javascript:onCompleted(" + json.toString() + ")");
                    } catch (JSONException e) {

                    }
                    unzipTemplate(Uri.fromFile(new File(path)), template, msg.getData());
                    downloadFont(template);
                    break;
                }
                case WHAT_DOWNLOAD_ERROR: {
                    int id = msg.getData().getInt(EXTRA_TEMPLATE_ID, 0);
                    LogUtil.d("下载模板出错：" + id);
                    loadUrl("javascript:onError(" + id + ")");
                    break;
                }

            }
        }
    };

    private Handler downloadFontHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_DOWNLOAD_PROGRESS: {
                    String name = msg.getData().getString(EXTRA_FONT_NAME);
                    double progress = msg.getData().getDouble(DOWNLOAD_PROGRESS, 0);
                    try {
                        JSONObject json = new JSONObject();
                        json.put("fontName", name);
                        json.put("progress", progress);
                        loadUrl("javascript:onFontDownloading(" + json.toString() + ")");
                    } catch (JSONException e) {

                    }
                    break;
                }
                case WHAT_DOWNLOAD_COMPLETED: {
                    String name = msg.getData().getString(EXTRA_FONT_NAME);
                    try {
                        JSONObject json = new JSONObject();
                        json.put("fontName", name);
                        LogUtil.d("字体下载完成:" + name);
                        loadUrl("javascript:onFontCompleted(" + json.toString() + ")");
                    } catch (JSONException e) {

                    }
                    break;
                }
                case WHAT_DOWNLOAD_ERROR: {
                    String name = msg.getData().getString(EXTRA_FONT_NAME);
                    LogUtil.d("下载字体出错：" + name);
                    loadUrl("javascript:onFontError(" + name + ")");
                    break;
                }

            }
        }
    };

    public String getContent() {
        return html;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d("建立界面");
        if(Utils.isEmpty(html)){
            html = getIntent().getStringExtra(EXTRA_EDIT_CONTENT);
        }
        loadUrl(START_URL);
        startLoad(WHAT_INIT, null);
    }

    @Override
    protected boolean onBackNavigation() {
        return super.onBackNavigation();
    }

    @Override
    public void onBackPressed() {
        loadUrl("javascript:doSaveStory()");
    }

    public void downloadTemplate(String data, int id, int categoryId) throws JSONException {
        JSONObject json = new JSONObject(data);
        String name = json.getString(EXTRA_TEMPLATE_NAME);
        String url = json.getString(EXTRA_TEMPLATE_URL);

        Bundle args = new Bundle();
        args.putInt(EXTRA_TEMPLATE_ID, id);
        args.putInt(EXTRA_CATEGORY_ID, categoryId);
        args.putString(EXTRA_TEMPLATE_NAME, name);
        args.putString(EXTRA_TEMPLATE_URL, url);
        startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
    }

    public void downloadFont(String fontName) {
        Bundle args = new Bundle();
        args.putString(EXTRA_FONT_NAME, fontName);
        startLoad(WHAT_DOWNLOAD_FONT, args);
    }

    public void downloadFont(File template) {
        BlockingQueue<String> fontQueue = new LinkedBlockingQueue<>();
        Set<String> fontSet = parseFont(template);
        if (fontSet.size() == 0) {
            return;
        }
        File fontDirectory = StoryManager.getStoryFontDirectory();
        for (String fontName : fontSet) {
            File font = new File(fontDirectory, fontName);
            if (!font.exists() || font.list().length <= 1) {
//                Bundle args = new Bundle();
//                args.putString(EXTRA_FONT_NAME, fontName);
//                startLoad(WHAT_DOWNLOAD_FONT, args);
                fontQueue.offer(fontName);
            }
        }
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new FontDownloader(fontQueue));
        service.shutdown();
    }

    private Set<String> parseFont(File template) {
        Set<String> fontSet = new HashSet<>();
        File file = new File(template, TEMPLATE_NAME);
        if (!file.exists()) {
            return fontSet;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(FONT_FAMILY)) {
                    line = line.substring(line.indexOf(FONT_FAMILY));
                    line = line.substring(0, line.indexOf(";"));
                    String font = line.split(":")[1].trim().replace("'", "");
                    fontSet.add(font);
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.d("StoryTemplate", "Error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
        return fontSet;
    }

    public void invokeJavascriptTest() {
        loadUrl("javascript:test2()");
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        final Message msg = Message.obtain();
        msg.what = what;
        switch (what) {
            case WHAT_INIT:
                StoryEntity story = StoryLogic.instance().getStoryEntityFromShare();
                if (story == null) {
                    story = new StoryEntity();
                    story.storyName = Utils.acquireUTCTimestamp();
                    story.status = ApiStory.AttrStoryInfo.STORY_STATUS_TEMPORARY;
                    story.userId = UserLogic.instance().getUserInfoFromLocal().user_id;
                    story.storyDesc = "Something wonderful is coming";
                    story.storyLocal = Utils.acquireUTCTimestamp();
                    try {
                        File file = new File(StoryManager.getStoryDirectory(), story.storyLocal);
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    try {
                        File file = new File(StoryManager.getStoryDirectory(), story.storyLocal + "/img");
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                    } catch (Exception e) {
                        LogUtil.e("初始化失败", e);
                    }
                    StoryLogic.instance().saveStoryEntityToShare(story);
                }
                break;
            case WHAT_DOWNLOAD_TEMPLATE: {
                final int id = args.getInt(EXTRA_TEMPLATE_ID, 0);
                final int categoryId = args.getInt(EXTRA_CATEGORY_ID, 0);
                final String name = args.getString(EXTRA_TEMPLATE_NAME);
                final String url = args.getString(EXTRA_TEMPLATE_URL);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryTemplateDirectory(), name));
                Downloader.download(Uri.parse(url), dest, new Downloader.DownloaderCallback() {
                    public void onDownloading(double progress) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        bundle.putInt(EXTRA_CATEGORY_ID, categoryId);
                        bundle.putDouble(DOWNLOAD_PROGRESS, progress);
                        msg.what = WHAT_DOWNLOAD_PROGRESS;
                        msg.setData(bundle);
                        downloadTemplateHandler.sendMessage(msg);
                    }

                    public void onCompleted(Uri downUri) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_COMPLETED;
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        bundle.putInt(EXTRA_CATEGORY_ID, categoryId);
                        bundle.putString(EXTRA_TEMPLATE_NAME, name);
                        bundle.putString(EXTRA_TEMPLATE_PATH, downUri.getPath());
                        bundle.putString(EXTRA_TEMPLATE_URL, url);
                        msg.setData(bundle);
                        downloadTemplateHandler.sendMessage(msg);
                    }

                    public void onError(Uri uri) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_ERROR;
                        Bundle bundle = new Bundle();
                        bundle.putInt(EXTRA_TEMPLATE_ID, id);
                        msg.setData(bundle);
                        downloadTemplateHandler.sendMessage(msg);
                    }
                });
                break;
            }
            case WHAT_DOWNLOAD_FONT: {
                final String name = args.getString(EXTRA_FONT_NAME);
//                final String name = "Trebuc";
                Uri uri = WWWConfig.acquireUri(getString(R.string.uri_font_download));
                String url = String.format("%s?name=%s", uri.toString(), name);
                Uri dest = Uri.fromFile(new File(StoryManager.getStoryFontDirectory(), name + ".zip"));
                Downloader.download(Uri.parse(url), dest, new Downloader.DownloaderCallback() {
                    public void onDownloading(double progress) {
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_FONT_NAME, name);
                        bundle.putDouble(DOWNLOAD_PROGRESS, progress);
                        msg.what = WHAT_DOWNLOAD_PROGRESS;
                        msg.setData(bundle);
                        downloadFontHandler.sendMessage(msg);
                    }

                    public void onCompleted(Uri downUri) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_COMPLETED;
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_FONT_NAME, name);
                        msg.setData(bundle);
                        downloadFontHandler.sendMessage(msg);

//                        File font = getFontUnzipDirectory(name);
                        File font = StoryManager.getStoryFontDirectory();
                        unzipFont(downUri, font);
                        appendFont(name);
                    }

                    public void onError(Uri uri) {
                        Message msg = Message.obtain();
                        msg.what = WHAT_DOWNLOAD_ERROR;
                        Bundle bundle = new Bundle();
                        bundle.putString(EXTRA_FONT_NAME, name);
                        msg.setData(bundle);
                        downloadFontHandler.sendMessage(msg);
                    }
                });
                break;
            }
            case WHAT_INIT_COMPLETED: {
                initHandler.sendMessage(Message.obtain());
            }
        }
        return msg;
    }

    private Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            StoryTemplateInfo templateInfo = DataSynchronizer.getInstance().getFirstTemplate();
            String content = "";
            if (templateInfo != null) {
                File path = new File(StoryManager.getStoryTemplateDirectory(), templateInfo.temp_name + "/" + "stage.html");
                content = readHtml(path.getAbsolutePath());
            }
//            loadUrl("javascript:onInitCompleted(" + content + ")");
        }
    };

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

    private synchronized void appendFont(String fontName) {
        File fonts = new File(StoryManager.getStoryFontDirectory(), FONT_FILE_NAME);
        PrintWriter writer = null;
        try {
            if (!fonts.exists()) {
                fonts.createNewFile();
            }
            writer = new PrintWriter(new FileWriter(fonts, true));
            writer.println("@font-face {");
            writer.println(String.format("    font-family: '%s';", fontName));
            writer.println(String.format("    src: url('%s/%s.eot');", fontName, fontName));
            writer.println(String.format("    src: url('%s/%s.eot?#iefix') format('embedded-opentype'),", fontName, fontName));
            writer.println(String.format("    url('%s/%s.woff') format('woff'),", fontName, fontName));
            writer.println(String.format("    url('%s/%s.ttf') format('truetype'),", fontName, fontName));
            writer.println(String.format("    url('%s/%s.svg') format('svg');", fontName, fontName));
            writer.println("    font-weight: normal;");
            writer.println("    font-style: normal;");
            writer.println("}");
        } catch (Exception e) {

        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    private File getTemplateUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if (0 < index) {
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryTemplateDirectory(), templateDir);
    }

    private File getFontUnzipDirectory(String name) {
        int index = name.lastIndexOf('.');
        String templateDir = name;
        if (0 < index) {
            templateDir = name.substring(0, index);
        }
        return new File(StoryManager.getStoryFontDirectory(), templateDir);
    }

    private void unzipTemplate(Uri downUri, File template, Bundle args) {
        try {
            if (template.isFile()) {
                FileUtils.forceDelete(template);
            } else {
                FileUtils.deleteDirectory(template);
            }
            ZipUtils.unzip(downUri, template);
        } catch (IOException e) {
            LogUtil.e("解压模版失败", e);
            e.printStackTrace();
            File destFile = new File(template + ".zip");
            if (destFile.exists()) {
                try {
                    InputStream is = new FileInputStream(destFile);
                    String md5 = DigestUtils.md5Hex(is);
                    StoryTemplateInfo templateInfo = StoryLogic.instance()
                            .getStoryTemplateLocalByName(this, template.getName());
                    if (md5.equals(templateInfo.hash_code)) {
                        unzipTemplate(downUri, template, args);
                    } else {
                        startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                startLoad(WHAT_DOWNLOAD_TEMPLATE, args);
            }
        }
    }

    private void unzipFont(Uri downUri, File font) {
        try {
            ZipUtils.unzip(downUri, font);
        } catch (IOException e) {
            LogUtil.e("解压字体出错:", e);
            loadUrl("javascript:onError('unzip error!')");
        }
    }

    @Override
    protected void onLoadCompleted(Message args) {
        if (isDestroyed() || null == args) {
            return;
        }
        switch (args.what) {
            case WHAT_DOWNLOAD_TEMPLATE: {

                break;
            }
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
////        appView.loadUrl("javascript:ImSave()");
//        StoryLogic.instance().saveTempHtml("<div class=\"stage-content edit-area pages-img pages-img-bg\"     style=\" text-align:center;word-break:break-all;background: url(/storage/sdcard0/wisape/com.wisape.android/data/template/cover/img/bg.jpg);background-size: cover;background-position:50% 50%;width:100%;height:100%;position: relative;\">    <div class=\"stage-content-box\" style=\"position: absolute;text-align: center;top:3.65rem;width:16rem;-webkit-transform-origin:0 0;\">        <div class=\"symbol\">            <div class=\"pages-txt edit-area\" data-animation=\"animated flash\"  style=\"display: inline-block;font-family: 'bebas';font-size:3.5rem;line-height: 3.5rem;min-height: 3.5rem;min-width:14rem;color:#cc0001;\">                JOITN            </div>        </div>        <div class=\"symbol\" style=\"z-index: 3;\">            <div class=\"pages-txt edit-area\" data-animation=\"animated flash\" style=\"display: inline-block;margin-top:0.5rem;font-family: 'bebas';font-size:3.5rem;line-height: 3.5rem;min-height: 3.5rem;min-width:14rem;color:#000;\">                WISAPE            </div>        </div>    </div>    <div class=\"stage-content-box\" style=\"position: absolute;bottom:1rem;text-align: center;width:16rem;-webkit-transform-origin:0 100%;\">        <div class=\"symbol\" style=\"z-index: 999; \">            <div class=\"pages-img edit-area\"  data-animation=\"animated fadeInUp\">                <img style=\"width:4.44rem;height:1.03rem;\" src=\"/storage/sdcard0/wisape/com.wisape.android/data/template/cover/img/logo.png\"></div>        </div>        <div class=\"symbol\" style=\"z-index: 3;\">            <div class=\"pages-txt edit-area\" data-animation=\"animated fadeInUp\" style=\"font-size:0.53rem;color:#000;font-family: 'Lato';margin-top:0.3rem;min-height: 0.53rem;min-width:14rem;\">                Something Wonderful is Coming            </div>        </div>    </div></div>\";");
//        LogUtil.d("保存数据");
//        appView = null;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        String tempHtml = StoryLogic.instance().getTempHtml();
//        LogUtil.d("重新创建获取到的数据:" + tempHtml);
//        if(!Utils.isEmpty(tempHtml)){
//            html = tempHtml;
//        }
//        StoryLogic.instance().clearTempHtml();
//    }
}