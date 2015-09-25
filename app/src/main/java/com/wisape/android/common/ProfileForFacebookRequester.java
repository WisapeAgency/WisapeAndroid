//package com.wisape.android.common;
//
//import android.util.Log;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.RetryPolicy;
//import com.android.volley.error.VolleyError;
//import com.android.volley.request.StringRequest;
//import com.wisape.android.activity.SignUpActivity;
//import com.wisape.android.network.Requester;
//import com.wisape.android.network.VolleyHelper;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.concurrent.CountDownLatch;
//
///**
// * Created by LeiGuoting on 6/7/15.
// */
//public class ProfileForFacebookRequester implements ProfileRequester<ProfileRequester.Param>{
//    private static final String TAG = ProfileForFacebookRequester.class.getSimpleName();
//    private static final String URL_PROFILE = "https://graph.facebook.com/v2.2/me";
//    private static final String URL_ICON = "https://graph.facebook.com/v2.2/me/picture";
//
//    @Override
//    public ProfileInfo request(Param params) {
//        RetryPolicy retryPolicy = Requester.instance().getDefaultPolicy();
//        RequestQueue queue = VolleyHelper.getRequestQueue();
//        CountDownLatch latch = new CountDownLatch(2);
//
//        //profile
//        String url = new StringBuilder(URL_PROFILE.length() + params.token.length() + 14).append(URL_PROFILE).append("?").append("access_token=").append(params.token).toString();
//        Log.d(TAG, "#request Profile Url:" + url);
//        ResponseListener profileListener = new ResponseListener(latch);
//        StringRequest profileRequest = new StringRequest(Request.Method.GET, url, profileListener, profileListener);
//        profileRequest.setRetryPolicy(retryPolicy);
//        profileRequest.setShouldCache(false);
//
//        //icon
//        StringBuilder builder = new StringBuilder(127);
//        builder.append(URL_ICON).append("?");
//        builder.append("access_token=").append(params.token);
//        builder.append("&redirect=").append("false");
//        builder.append("&height=").append("128");
//        builder.append("&width=").append("128");
//        String iconUrl = builder.toString();
//        Log.d(TAG, "#request Icon Url:" + iconUrl);
//        ResponseListener iconListener = new ResponseListener(latch);
//        StringRequest iconRequest = new StringRequest(Request.Method.GET, iconUrl, iconListener, iconListener);
//        iconRequest.setRetryPolicy(retryPolicy);
//        iconRequest.setShouldCache(false);
//
//
//        //request
//        queue.add(profileRequest);
//        queue.add(iconRequest);
//
//        ProfileInfo profile = null;
//        try {
//            latch.await();
//
//            if(profileListener.isSuccess() && iconListener.isSuccess()){
//                profile = new ProfileInfo(SignUpActivity.SIGN_UP_WITH_FACE_BOOK);
//                try{
//                    setProfile(profileListener.getData(), profile);
//                    setIcon(iconListener.getData(), profile);
//                }catch (JSONException e){
//                    Log.e(TAG, "", e);
//                    profile = null;
//                }
//            }
//        } catch (InterruptedException e) {
//            //do nothing
//        } finally {
//            profileListener.destroy();
//            iconListener.destroy();
//        }
//
//        return profile;
//    }
//
//    private void setProfile(String dataJson, ProfileInfo profile) throws JSONException{
//        Log.d(TAG, "#setProfile dataJson:" + dataJson);
//        JSONObject jsonObj = new JSONObject(dataJson);
//
//        profile.uniqueStr = jsonObj.optString("id");
//        profile.nickName = jsonObj.optString("name");
//    }
//
//    private void setIcon(String dataJson, ProfileInfo profile)throws JSONException{
//        Log.d(TAG, "#setIcon dataJson:" + dataJson);
//        JSONObject jsonObj = new JSONObject(dataJson);
//        profile.icon = jsonObj.optJSONObject("data").optString("url");
//    }
//
//    private static class ResponseListener implements Response.Listener<String>, Response.ErrorListener{
//        private final CountDownLatch latch;
//        private volatile boolean success;
//        private VolleyError error;
//        private String data;
//
//
//        ResponseListener(CountDownLatch latch){
//            this.latch = latch;
//        }
//
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            success = false;
//            this.error = error;
//            latch.countDown();
//        }
//
//        @Override
//        public void onResponse(String response) {
//            success = true;
//            data = response;
//            latch.countDown();
//        }
//
//        public boolean isSuccess(){
//            return success;
//        }
//
//        public String getData(){
//            return data;
//        }
//
//        public VolleyError getError(){
//            return error;
//        }
//
//        public void destroy(){
//            this.data = null;
//            this.error = null;
//        }
//    }
//}
