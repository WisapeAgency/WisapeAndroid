package com.wisape.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

import com.bugtags.library.Bugtags;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.database.StoryEntity;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.service.NanoService;
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
    private UserInfo userInfo;
    private SharedPreferences sharedPreferences;
    private String installId;
    private StoryEntity storyEntity;


    public enum TrackerName {
        APP_TRACKER,
        // Tracker used only in this app.
        GLOBAL_TRACKER,
        // Tracker used by all the apps from a company.
    }

    private Map<TrackerName,Tracker> mTrackers = new HashMap<>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
                    : (trackerId ==
                    TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        final Context context = getApplicationContext();
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        WWWConfig.initialize(context);
        NanoService.startNanoServer(context);
        PlayerProxy.launch(context);
        Bugtags.start("f6843af99861f31d1af2ae6d74a8e9a9", this, Bugtags.BTGInvocationEventBubble);
        Bugtags.setTrackingCrashes(true);
        Bugtags.setTrackingUserSteps(true);
        Bugtags.setTrackingConsoleLog(false);
        //初始化parse通讯
        Parse.initialize(this, "L3WrrhBJmbPhRoJ4GYIUDMIErlR8IlvkJuQQJ0Px", "yfC5kFI4jLLeeDaKlepK1hgAGiYJJEHjXfnpaCks");
        PushService.subscribe(this, "abcde", MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().put("localeIdentifier", Utils.getCountry(this).toUpperCase());
        ParseInstallation.getCurrentInstallation().saveInBackground();
        installId = ParseInstallation.getCurrentInstallation().getInstallationId();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public String getInstallId() {
        return installId;
    }

    public SharedPreferences getSharePrefrence() {
        return sharedPreferences;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public List<StoryTemplateTypeInfo> getTemplateTypeList() {
        return templateTypeList;
    }

    public Map<Integer, List<StoryTemplateInfo>> getTemplateMap() {
        return templateMap;
    }

    public StoryEntity getStoryEntity() {
        return storyEntity;
    }

    public void setStoryEntity(StoryEntity storyEntity) {
        this.storyEntity = storyEntity;
    }
}