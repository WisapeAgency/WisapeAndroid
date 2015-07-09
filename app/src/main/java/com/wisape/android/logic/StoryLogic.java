package com.wisape.android.logic;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.wisape.android.api.ApiStory;
import com.wisape.android.common.UserManager;
import com.wisape.android.model.StoryInfo;
import com.wisape.android.model.UserInfo;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.util.ZipUtils;

import java.io.IOException;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class StoryLogic{
    private static final String TAG = StoryLogic.class.getSimpleName();

    public static StoryLogic instance(){
        return new StoryLogic();
    }

    private StoryLogic(){}

    public StoryInfo update(Context context, ApiStory.AttrStoryInfo attr, Object tag){
        UserInfo user = UserManager.instance().signIn(context);
        attr.userId = user.user_id;

        Uri storyUri = attr.story;
        Log.d(TAG, "#update story' uri:" + storyUri);

        try {
            //TODO zip name?
            Uri storyZip = ZipUtils.zip(storyUri, EnvironmentUtils.getAppTemporaryDirectory(), attr.storyName);
            attr.story = storyZip;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        String thumb = Utils.base64ForImage(attr.attrStoryThumb);
        attr.storyThumb = thumb;

        ApiStory api = ApiStory.instance();
        StoryInfo story = api.updateStory(context, attr, tag);

        Log.d(TAG, "#update story:" + story);
        //TODO save local
        return story;
    }
}
