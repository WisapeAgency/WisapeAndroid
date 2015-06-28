package com.wisape.android.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * NetHelper
 * Created by Xugm on 15/6/16.
 */
public class NetHelper {
    private static NetHelper sInstance;
    private static RequestQueue mRequestQueue;

    public static NetHelper getInstance() {
        return sInstance == null ? sInstance = new NetHelper() : sInstance;
    }

    private NetHelper() {
        mRequestQueue = VolleyHelper.getRequestQueue();
    }

    public static void cancelAll(Object tag) {
        if (tag != null)
            mRequestQueue.cancelAll(tag);
    }

    public void post(String url, Map<String, String> params ,RequestListener listener){
        post(url, params, null, listener);
    }

    public void post(String url, final Map<String, String> params, Object tag, RequestListener listener) {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        if (null != tag) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
        try {
            listener.onComplete(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
            listener.onError(1, "error 1");
        } catch (ExecutionException e) {
            e.printStackTrace();
            listener.onError(2, "error 2");
        }
    }

    public void get(String url, Map<String, String> params, RequestListener listener) {
        get(url, params, null, listener);
    }

    public void get(String url, Map<String, String> params, Object tag, RequestListener listener) {
        url += parseParams(params);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, url, future, future);
        if (null != tag) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
        try {
            listener.onComplete(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
            listener.onError(1, "error 1");
        } catch (ExecutionException e) {
            e.printStackTrace();
            listener.onError(2, "error 2");
        }
    }

    public static String parseParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder builder = new StringBuilder("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            builder.append("&");
        }
        return builder.toString();
    }
}
