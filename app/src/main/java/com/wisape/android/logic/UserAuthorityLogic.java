package com.wisape.android.logic;

import android.content.Context;

import com.wisape.android.common.UserManager;
import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUserAuthority;
import com.wisape.android.network.Requester;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class UserAuthorityLogic{
    private static final String TAG = UserAuthorityLogic.class.getSimpleName();

    public static UserAuthorityLogic instance(){
        return new UserAuthorityLogic();
    }

    private UserAuthorityLogic(){}

    public UserInfo signUp(Context context, ApiUserAuthority.AttrSignUpInfo attrInfo, Object cancelableTag){
        ApiUserAuthority api = ApiUserAuthority.instance();
        UserInfo user = api.signUp(context, attrInfo, cancelableTag);
        if(Requester.ServerMessage.STATUS_SUCCESS == user.status){
            UserManager.instance().saveUser(context, user);
        }
        return user;
    }
}
