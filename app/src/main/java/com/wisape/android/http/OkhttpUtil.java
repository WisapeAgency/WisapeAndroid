package com.wisape.android.http;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wisape.android.util.LogUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ok网络请求工具类
 * Created by huangmeng on 15/9/2.
 */
public class OkhttpUtil {

    private static final String TAG = OkhttpUtil.class.getSimpleName();

    public static final int SERVER_RESPONSE_SUCCESS = 1;

    private static final String KEY_RESPONSE_SUCCESS = "success";
    private static final String KEY_RESPONSE_MESSAGE = "message";
    private static final String KEY_RESPONSE_DATA = "data";

    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    static {
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
    }

    public static <T> T executePost(String url, RequestBody requestBody, Class<T> clazz) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new IOException("No Internet Connection");
        }
        if (response.isSuccessful()) {
            String body = response.body().string();
            Log.e(TAG, body);
            JSONObject jsonObject = JSON.parseObject(body);
            if (SERVER_RESPONSE_SUCCESS == jsonObject.getIntValue(KEY_RESPONSE_SUCCESS)) {
                return JSONObject.parseObject(jsonObject.getJSONObject(KEY_RESPONSE_DATA).toJSONString(), clazz);
            } else {
                throw new IOException(jsonObject.getString(KEY_RESPONSE_MESSAGE));
            }

        } else {
            throw new IOException("No Internet Connection");
        }
    }

    public static <T> T execute(String url, Map<String, String> params, Class<T> clazz) throws IOException {

        url = HttpUtils.getUrlWithParas(url, params);
        Log.e(TAG, url);
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new IOException("No Internet Connection");
        }
        if (response.isSuccessful()) {
            String body = response.body().string();
            LogUtil.d("网络" + url + "返回数据:" + body);

            JSONObject jsonObject = JSON.parseObject(body);
            if (SERVER_RESPONSE_SUCCESS == jsonObject.getIntValue(KEY_RESPONSE_SUCCESS)) {
                return JSONObject.parseObject(jsonObject.getJSONObject(KEY_RESPONSE_DATA).toJSONString(), clazz);
            } else {
                throw new IOException(jsonObject.getString(KEY_RESPONSE_MESSAGE));
            }

        } else {
            throw new IOException("No Internet Connection");
        }

    }

    public static <T> List<T> execute(Map<String, String> params, String url, Class<T> clazz) throws IOException {
        url = HttpUtils.getUrlWithParas(url, params);
        Log.e(TAG, url);
        Request request = new Request.Builder().url(url).build();

        Response response = null;
        try {
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new IOException("No Internet Connection");
        }
        if (response.isSuccessful()) {
            String body = response.body().string();
            Log.e(TAG, body);
            JSONObject jsonObject = JSON.parseObject(body);
            LogUtil.d("网络" + url + "返回数据:" + body);
            if (SERVER_RESPONSE_SUCCESS == jsonObject.getIntValue(KEY_RESPONSE_SUCCESS)) {
                return JSONObject.parseArray(jsonObject.getJSONArray(KEY_RESPONSE_DATA).toJSONString(), clazz);
            } else {
                throw new IOException(jsonObject.getString(KEY_RESPONSE_MESSAGE));
            }
        } else {
            throw new IOException("No Internet Connection");
        }
    }


    public static void execute(String url, Map<String, String> params) throws IOException {
        url = HttpUtils.getUrlWithParas(url, params);
        Log.e(TAG, url);
        Request request = new Request.Builder().url(url).build();
        Response response = mOkHttpClient.newCall(request).execute();

        if (response.isSuccessful()) {
            String body = response.body().string();
            Log.e(TAG, body);
            JSONObject jsonObject = JSON.parseObject(body);
            LogUtil.d("网络" + url + "返回数据:" + body);
            if (SERVER_RESPONSE_SUCCESS != jsonObject.getIntValue(KEY_RESPONSE_SUCCESS)) {
                throw new IOException(jsonObject.getString(KEY_RESPONSE_MESSAGE));
            }

        } else {
            throw new IOException("No Internet Connection");
        }
    }

    /**
     * 文件下载
     *
     * @param url 文件在服务器端的地址
     */
    public static void downLoadFile(final String url, final FileDownloadListener listener) {
        final Request request = new Request.Builder().url(url)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Accept-Encoding", "identity")
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                listener.onError(url + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                listener.onSuccess(response.body().bytes());
            }
        });
    }


}
