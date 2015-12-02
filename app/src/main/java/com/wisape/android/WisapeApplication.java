package com.wisape.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.bugtags.library.Bugtags;
import com.bugtags.library.BugtagsOptions;
import com.flurry.android.FlurryAgent;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.network.WWWConfig;

import org.cubieline.lplayer.PlayerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.wisape.android.content.DataSynchronizerReceiver;
//import com.wisape.android.content.MessageCenterReceiver;

/**
 * @author Duke
 */
public class WisapeApplication extends Application {

    private static final String PREFERENCES_NAME = "_user_preferences";

    private static final String PROPERTY_ID = "UA-64553657-1";

    private static WisapeApplication instance;

    public static WisapeApplication getInstance() {
        return instance;
    }

    private List<StoryTemplateTypeInfo> templateTypeList = new ArrayList<>();
    private Map<Integer, List<StoryTemplateInfo>> templateMap = new HashMap<>();
    private SharedPreferences sharedPreferences;
//    private String installId;
//    private DataSynchronizerReceiver dataSynchronizerReceiver;



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PropertyConfigurator.getConfigurator(this).configure();
        final Context context = getApplicationContext();
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        WWWConfig.initialize(context);
        FlurryAgent.init(this, "BKNHHSXHP986YBR666ZY");
        PlayerProxy.launch(context);

        initBugTags();
        //初始化parse通讯
//        initParse();

//        dataSynchronizerReceiver = new DataSynchronizerReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.wisape.android.content.DataSynchronizerReceiver");
//        registerReceiver(dataSynchronizerReceiver, intentFilter);

    }

    private void initBugTags(){
        BugtagsOptions options = new BugtagsOptions.Builder().
                trackingLocation(false).//是否获取位置
                trackingCrashLog(true).//是否收集crash
                trackingConsoleLog(false).//是否收集console log
                trackingUserSteps(false).//是否收集用户操作步骤
                build();
        Bugtags.start("2de05eeb48ef1f51c597ed035315fabe", this, Bugtags.BTGInvocationEventNone, options);
    }


    /**
     * 初始化parse
     */
//    private void initParse(){
//
//
//        Parse.initialize(this, "L3WrrhBJmbPhRoJ4GYIUDMIErlR8IlvkJuQQJ0Px", "yfC5kFI4jLLeeDaKlepK1hgAGiYJJEHjXfnpaCks");
//        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
//        PushService.subscribe(this, "abcde", MainActivity.class);
//        PushService.setDefaultPushCallback(this, MainActivity.class);
//        ParseInstallation.getCurrentInstallation().put("localeIdentifier", Utils.getCountry(this).toUpperCase());
//        ParseInstallation.getCurrentInstallation().saveInBackground();
//        installId = ParseInstallation.getCurrentInstallation().getInstallationId();
//
//
//    }


//    public String getInstallId() {
//        return installId;
//    }

    public SharedPreferences getSharePrefrence() {
        return sharedPreferences;
    }

    public List<StoryTemplateTypeInfo> getTemplateTypeList() {
        return templateTypeList;
    }

    public Map<Integer, List<StoryTemplateInfo>> getTemplateMap() {
        return templateMap;
    }

//    @Override
//    public void onTerminate() {
//        super.onTerminate();
//        unregisterReceiver(dataSynchronizerReceiver);
//    }
}