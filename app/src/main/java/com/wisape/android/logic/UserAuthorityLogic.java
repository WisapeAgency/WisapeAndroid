package com.wisape.android.logic;

import android.content.Context;

import com.wisape.android.model.UserInfo;
import com.wisape.android.network.ApiUserAuthority;

/**
 * Created by LeiGuoting on 3/7/15.
 */
public class UserAuthorityLogic{
    private static final String TAG = UserAuthorityLogic.class.getSimpleName();

    public static UserAuthorityLogic instance(){
        return new UserAuthorityLogic();
    }

    private UserAuthorityLogic(){}

    public UserInfo signup(Context context, ApiUserAuthority.AttrSignupInfo attrInfo, Object cancelableTag){
        ApiUserAuthority api = ApiUserAuthority.instance();
        UserInfo user = api.signup(context, attrInfo, cancelableTag);

        return user;
    }
}
