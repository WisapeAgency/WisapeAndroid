package com.wisape.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.bugtags.library.Bugtags;
import com.flurry.android.FlurryAgent;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.model.UserInfo;
//import com.wisape.android.network.NanoServer;
import com.wisape.android.network.WWWConfig;
//import com.wisape.android.service.NanoService;
//import com.wisape.android.service.NanoService;
import com.wisape.android.util.Utils;

import org.cubieline.lplayer.PlayerProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//    private UserInfo userInfo;
    private SharedPreferences sharedPreferences;
    private String installId;
//    private StoryEntity storyEntity;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        final Context context = getApplicationContext();
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        WWWConfig.initialize(context);
//        FlurryAgent.init(this, "BKNHHSXHP986YBR666ZY");
        PlayerProxy.launch(context);
        Bugtags.start("f6843af99861f31d1af2ae6d74a8e9a9", this, Bugtags.BTGInvocationEventNone);
        Bugtags.setTrackingCrashes(true);
        //初始化parse通讯
        Parse.initialize(this, "L3WrrhBJmbPhRoJ4GYIUDMIErlR8IlvkJuQQJ0Px", "yfC5kFI4jLLeeDaKlepK1hgAGiYJJEHjXfnpaCks");
        PushService.subscribe(this, "abcde", MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().put("localeIdentifier", Utils.getCountry(this).toUpperCase());
        ParseInstallation.getCurrentInstallation().saveInBackground();
        installId = ParseInstallation.getCurrentInstallation().getInstallationId();

    }

    public String getInstallId() {
        return installId;
    }

    public SharedPreferences getSharePrefrence() {
        return sharedPreferences;
    }

    public List<StoryTemplateTypeInfo> getTemplateTypeList() {
        return templateTypeList;
    }

    public Map<Integer, List<StoryTemplateInfo>> getTemplateMap() {
        return templateMap;
    }

//    public StoryEntity getStoryEntity() {
//        return storyEntity;
//    }
//
//    public void setStoryEntity(StoryEntity storyEntity) {
//        this.storyEntity = storyEntity;
//    }
}