package com.wisape.android.common;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.RequestFuture;
import com.wisape.android.activity.SignUpActivity;
import com.wisape.android.network.Requester;
import com.wisape.android.network.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by LeiGuoting on 6/7/15.
 */
public class ProfileForGooglePlusRequester implements ProfileRequester<ProfileRequester.Param>{
    private static final String TAG = ProfileForGooglePlusRequester.class.getSimpleName();
    private static final String URL_PROFILE = "https://www.googleapis.com/oauth2/v1/userinfo";

    @Override
    public ProfileInfo request(Param param) {
        RetryPolicy retryPolicy = Requester.instance().getDefaultPolicy();
        RequestQueue queue = VolleyHelper.getRequestQueue();

        StringBuilder builder = new StringBuilder(URL_PROFILE.length() + 32);
        builder.append(URL_PROFILE).append("?");
        builder.append("alt=").append("json");
        builder.append("&access_token=").append(param.token);

        String url = builder.toString();
        Log.d(TAG, "#request url:" + url);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, url, future, future);
        request.setRetryPolicy(retryPolicy);
        request.setShouldCache(false);
        queue.add(request);

        ProfileInfo profile = null;
        try {
            String data = future.get();
            Log.d(TAG, "#request data:" + data);
            JSONObject jsonObj = new JSONObject(data);

            profile = new ProfileInfo(SignUpActivity.SIGN_UP_WITH_GOOGLE_PLUS);
            profile.uniqueStr = jsonObj.optString("id");
            profile.nickName = jsonObj.optString("name");
            profile.email = jsonObj.optString("email");
            profile.icon = jsonObj.optString("picture");
        } catch (InterruptedException e) {
            //do nothing
        } catch (ExecutionException e) {
            Log.e(TAG, "", e);
        } catch (JSONException e){
            Log.e(TAG, "", e);
        }
        return profile;
    }
}
