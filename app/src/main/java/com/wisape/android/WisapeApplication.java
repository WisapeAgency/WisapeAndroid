package com.wisape.android;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.wisape.android.activity.MainActivity;
import com.wisape.android.database.DatabaseHelper;
import com.wisape.android.model.StoryTemplateInfo;
import com.wisape.android.model.StoryTemplateTypeInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.WWWConfig;
import com.wisape.android.service.NanoService;

import org.cubieline.lplayer.PlayerProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Duke
 */
public class WisapeApplication extends Application {

    private static WisapeApplication instance;
    public static WisapeApplication getInstance() {
        return instance;
    }

    private List<StoryTemplateTypeInfo> templateTypeList = new ArrayList<>();
    private Map<Integer,List<StoryTemplateInfo>> templateMap = new HashMap<>();
    private UserInfo userInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        final Context context = getApplicationContext();
        Fresco.initialize(context);
        WWWConfig.initialize(context);
        NanoService.startNanoServer(context);
        PlayerProxy.launch(context);
        //初始化parse通讯
        Parse.initialize(getApplicationContext(), "L3WrrhBJmbPhRoJ4GYIUDMIErlR8IlvkJuQQJ0Px", "yfC5kFI4jLLeeDaKlepK1hgAGiYJJEHjXfnpaCks");
        PushService.subscribe(this, "abcde", MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class);
        ParseInstallation.getCurrentInstallation().saveInBackground();

    }


    public void setUserInfo(UserInfo userInfo){
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo(){
        return userInfo;
    }

    public List<StoryTemplateTypeInfo> getTemplateTypeList() {
        return templateTypeList;
    }

    public Map<Integer, List<StoryTemplateInfo>> getTemplateMap() {
        return templateMap;
    }
}
