package com.wisape.android.http;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.squareup.okhttp.OkHttpClient;
import com.wisape.android.network.VolleyHelper;

import java.util.Map;

/**
 * 使用volley封装的网络请求
 * Created by huangmeng on 15/8/18.
 */
public class HttpRequest {

    private static final String TAG = HttpRequest.class.getSimpleName();

    public static void addRequest(String url,Object tag,
                                  final HttpRequestListener httpRequestListener) {
        Log.e(TAG,"#addRequest:"+url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url,null,
                new Response.Listener<org.json.JSONObject>() {
                    @Override
                    public void onResponse(org.json.JSONObject response) {
                        httpRequestListener.onSuccess(JSONObject.parseObject(response.toString()));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        httpRequestListener.onError("Network Error");
                    }
                }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Log.e(TAG,"params == null:" + (null == params));
//                if(null == params){
//                    return super.getParams();
//                }
//                return params;
//            }
        };
        request.setTag(tag);
        VolleyHelper.getRequestQueue().add(request);
    }

    public static void cancleRequest(Object tag){
        VolleyHelper.getRequestQueue().cancelAll(tag);
    }


}
