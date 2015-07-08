package com.wisape.android.logic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.wisape.android.activity.MainActivity;
import com.wisape.android.activity.UserProfileActivity;
import com.wisape.android.common.ProfileRequester;
import com.wisape.android.common.UserManager;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUser;
import com.wisape.android.network.Requester;
import com.wisape.android.util.Utils;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class UserLogic {
    private static final String TAG = UserLogic.class.getSimpleName();

    public static UserLogic instance(){
        return new UserLogic();
    }

    private UserLogic(){}

    public UserInfo signUp(Context context, ApiUser.AttrSignUpInfo attrInfo, Object cancelableTag){
        ApiUser api = ApiUser.instance();
        UserInfo user = api.signUp(context, attrInfo, cancelableTag);
        if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
            UserManager.instance().saveUser(context, user);
        }
        return user;
    }

    public UserInfo signUpWith(Context context, ProfileRequester.ProfileInfo profile, Object cancelableTag){
        ApiUser.AttrSignUpInfo attr = new ApiUser.AttrSignUpInfo();
        attr.type = profile.platform;
        attr.email = profile.email;
        attr.userIcon = profile.icon;
        attr.nickName = profile.nickName;
        attr.uniqueStr = profile.uniqueStr;
        return signUp(context, attr, cancelableTag);
    }

    /**
     * We will send a Local Broadcast if update success.
     *
     * @param context
     * @param profile
     * @param iconUri
     * @param cancelableTag
     * @return
     */
    public UserInfo updateProfile(Context context, ApiUser.AttrUserProfile profile, Uri iconUri, Object cancelableTag){
        String iconBase64 = "";
        if(null != iconUri){
            iconBase64 = Utils.base64ForImage(iconUri);
        }
        profile.userIcon = iconBase64;
        ApiUser api = ApiUser.instance();
        UserInfo user = api.updateProfile(context, profile, cancelableTag);

        if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
            UserManager.instance().saveUser(context, user);

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent(UserProfileActivity.ACTION_PROFILE_UPDATED);
            intent.putExtra(MainActivity.EXTRA_USER_INFO, user);
            broadcastManager.sendBroadcast(intent);
        }
        return user;
    }
}
