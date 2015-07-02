package com.wisape.android.network;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.wisape.android.common.UserManager;
import com.wisape.android.util.SecurityUtils;
import com.wisape.android.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * All request server using the class.
 *
 * Created by LeiGuoting on 2/7/15.
 */
public final class Requester{
    public static final String EXTRA_TOKEN = "token";
    public static final String EXTRA_ACCESS_TOKEN = "access_token";

    private static WeakReference<Requester> ref;

    public static Requester instance(){
        Requester requester;
        if(null == ref || null == (requester = ref.get())){
            synchronized (Requester.class){
                if(null == ref || null == (requester = ref.get())){
                    requester = new Requester();
                    ref = new WeakReference(requester);
                }
            }
        }
        return requester;
    }

    private final RetryPolicy defaultPolicy;
    private Requester(){
        defaultPolicy = new DefaultRetryPolicy(WWWConfig.timeoutMills, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public void cancelAll(Object tag){
        RequestQueue queue = VolleyHelper.getRequestQueue();
        queue.cancelAll(tag);
    }

    public ServerMessage verifyResponse(JSONObject jsonObject){
        if(null == jsonObject){
            return ServerMessage.obtain(ServerMessage.STATUS_LOCAL_NULL_OBJECT);
        }

        int status = jsonObject.optInt(ServerMessage.EXTRA_STATUS, ServerMessage.STATUS_LOCAL_OPT_JSON_FAILED);
        String message = jsonObject.optString(ServerMessage.EXTRA_MESSAGE, "");
        JSONObject data = jsonObject.optJSONObject(ServerMessage.EXTRA_DATA);
        return ServerMessage.obtain(status, message, data);
    }

    public ServerMessage post(Context context, Uri uri, Map<String, String> params, Object tag){
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequestImpl request = new JsonObjectRequestImpl(Request.Method.POST, uri.toString(), params, future, future);

        setting(context, request, params, tag);
        RequestQueue queue = VolleyHelper.getRequestQueue();
        queue.add(request);

        ServerMessage msg;
        try{
            JSONObject json = future.get(defaultPolicy.getCurrentTimeout(), TimeUnit.MILLISECONDS);
            msg = verifyResponse(json);
        } catch (InterruptedException e){
            //do nothing
            msg = ServerMessage.obtain(ServerMessage.STATUS_LOCAL_INTERRUPTED);
        } catch (ExecutionException e){
            throw new IllegalStateException(e);
        } catch (TimeoutException e){
            //do nothing
            msg = ServerMessage.obtain(ServerMessage.STATUS_LOCAL_TIMEOUT);
        }

        return msg;
    }

    public void postAsync(Context context, Uri uri, Map<String, String> params, ResponseListener listener, Object tag){
        VolleyResponseListener volleyListener = new VolleyResponseListener(listener);
        JsonObjectRequestImpl request = new JsonObjectRequestImpl(Request.Method.POST, uri.toString(), params, volleyListener, volleyListener);
        setting(context, request, params, tag);
        RequestQueue queue = VolleyHelper.getRequestQueue();
        queue.add(request);
    }

    private void setting(Context context, JsonObjectRequestImpl request, Map<String, String> params, Object tag){
        request.setRetryPolicy(defaultPolicy);
        Map<String, String> headers;
        if(null == params || 0 == params.size()){
            headers = new HashMap(1);
        }else{
            headers = new HashMap(2);
            headers.put(EXTRA_TOKEN, makeToken(params));
        }

        headers.put(EXTRA_ACCESS_TOKEN, UserManager.getInstance(context).acquireAccessToken());
        request.setHeaders(headers);
        request.setShouldCache(true);
        if(null != tag){
            request.setTag(tag);
        }
    }

    private String makeToken(Map<String, String> params){
        if(null == params || 0 == params.size()){
            return "";
        }

        Set<String> keySet = params.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray, TokenComparator.instance());
        StringBuilder builder = new StringBuilder(256);
        for(String key : keyArray){
            builder.append(key).append("=").append(params.get(key)).append("&");
        }
        String paramString = builder.substring(0, builder.length() - 1);
        return SecurityUtils.md5(paramString);
    }

    private static class TokenComparator implements Comparator<String> {
        private static WeakReference<Comparator> comparatorRef;

        public static Comparator instance(){
            Comparator comparator;
            if(null == comparatorRef || null == (comparator = comparatorRef.get())){
                synchronized (TokenComparator.class){
                    if(null == comparatorRef || null == (comparator = comparatorRef.get())){
                        comparator = new TokenComparator();
                        comparatorRef = new WeakReference(comparator);
                    }
                }
            }
            return comparator;
        }

        private TokenComparator(){}

        @Override
        public int compare(String lhs, String rhs) {
            return lhs.compareTo(rhs);
        }
    }

    private static class JsonObjectRequestImpl extends JsonObjectRequest{
        private Map<String, String> params;
        private Map<String, String> headers;

        public JsonObjectRequestImpl(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            this(method, url, null, listener, errorListener);
        }

        public JsonObjectRequestImpl(int method, String url, Map<String, String> params, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, null, listener, errorListener);
            this.params = params;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return params;
        }

        public void setHeaders(Map<String, String> headers){
            this.headers = headers;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return (null == headers || 0 == headers.size()) ? super.getHeaders() : headers;
        }
    }

    public static class ServerMessage implements Parcelable{
        public static final String EXTRA_STATUS = "success";
        public static final String EXTRA_MESSAGE = "message";
        public static final String EXTRA_DATA = "data";

        //These value come from server
        public static final int STATUS_SUCCESS = 1;

        //These value was defined by local.
        public static final int STATUS_LOCAL_NULL_OBJECT = -1;
        public static final int STATUS_LOCAL_OPT_JSON_FAILED = -2;
        public static final int STATUS_LOCAL_TIMEOUT = -3;
        public static final int STATUS_LOCAL_INTERRUPTED = -4;
        public static final int STATUS_LOCAL_EXCEPTION = -5;

        private static final int CACHE_DEFAULT_SIZE = 10;
        private static WeakReference<ServerMessage>[] cachePool = new WeakReference[CACHE_DEFAULT_SIZE];

        public int status;
        public String message;
        public JSONObject data;

        public static ServerMessage obtain(){
            ServerMessage msg = null;
            synchronized (cachePool){
                WeakReference<ServerMessage>[] cache = cachePool;
                for(WeakReference<ServerMessage> ref : cache){
                    if(null != ref && null != (msg = ref.get())){
                        break;
                    }
                }
            }

            if(null == msg){
                msg = new ServerMessage();
            }
            return msg;
        }

        public static ServerMessage obtain(int status){
            ServerMessage msg = obtain();
            msg.status = status;
            return  msg;
        }

        public static ServerMessage obtain(int status, String message){
            ServerMessage msg = obtain();
            msg.status = status;
            msg.message = message;
            return  msg;
        }

        public static ServerMessage obtain(int status, String message, JSONObject data){
            ServerMessage msg = obtain();
            msg.status = status;
            msg.message = message;
            msg.data = data;
            return  msg;
        }

        private ServerMessage(){}

        public boolean succeed(){
            return STATUS_SUCCESS == status;
        }

        public void recycle(){
            this.status = 0;
            this.message = null;
            this.data = null;

            synchronized (cachePool){
                WeakReference<ServerMessage>[] cache = cachePool;
                int size = cache.length;
                WeakReference<ServerMessage> ref;
                for(int i = 0; i < size; i ++){
                    ref = cache[i];
                    if(null == ref || null == ref.get()){
                        cache[i] = new WeakReference(this);
                        break;
                    }
                }
            }
        }

        @Override
        public String toString() {
            String statusStr = Integer.toString(status);
            String statusName = getStatusName();
            String dataStr = (null == data ? "" : data.toString());
            int length = statusStr.length() + statusName.length() + ((null == message) ? 0 : message.length()) + dataStr.length();
            StringBuilder builder = new StringBuilder(length + 31);
            builder.append("{");
            builder.append("status:").append(statusStr).append(", status name:").append(statusName).append(", ");
            builder.append("message:").append(message).append(", ");
            builder.append("data:").append(dataStr);
            builder.append("}");
            return builder.toString();
        }

        private String getStatusName(){
            String name;
            switch (status){
                default :
                    name = "";
                    break;

                case STATUS_SUCCESS :
                    name = "STATUS_SUCCESS";
                    break;
                case STATUS_LOCAL_NULL_OBJECT :
                    name = "STATUS_LOCAL_NULL_OBJECT";
                    break;
                case STATUS_LOCAL_OPT_JSON_FAILED :
                    name = "STATUS_LOCAL_OPT_JSON_FAILED";
                    break;
                case STATUS_LOCAL_TIMEOUT :
                    name = "STATUS_LOCAL_TIMEOUT";
                    break;
                case STATUS_LOCAL_INTERRUPTED :
                    name = "STATUS_LOCAL_INTERRUPTED";
                    break;
                case STATUS_LOCAL_EXCEPTION :
                    name = "STATUS_LOCAL_EXCEPTION";
                    break;
            }
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.status);
            dest.writeString(this.message);
            dest.writeString((null == data) ? "" : data.toString());
        }

        protected ServerMessage(Parcel in) {
            this.status = in.readInt();
            this.message = in.readString();
            JSONObject jsonObject;
            try{
                jsonObject = new JSONObject(in.readString());
            }catch (JSONException e){
                throw new IllegalStateException(e);
            }
            this.data = jsonObject;
        }

        public static final Creator<ServerMessage> CREATOR = new Creator<ServerMessage>() {
            public ServerMessage createFromParcel(Parcel source) {
                return new ServerMessage(source);
            }

            public ServerMessage[] newArray(int size) {
                return new ServerMessage[size];
            }
        };
    }

    private static class VolleyResponseListener implements Response.ErrorListener, Response.Listener<JSONObject>{
        private ResponseListener listener;
        VolleyResponseListener(ResponseListener listener){
            this.listener = listener;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if(null != listener){
                ServerMessage msg;
                Throwable cause = error.getCause();
                if(cause instanceof InterruptedException){
                    msg = ServerMessage.obtain(ServerMessage.STATUS_LOCAL_INTERRUPTED);
                }else if(cause instanceof TimeoutException){
                    msg = ServerMessage.obtain(ServerMessage.STATUS_LOCAL_TIMEOUT);
                }else if(null != error.networkResponse){
                    NetworkResponse response = error.networkResponse;
                    msg = ServerMessage.obtain(response.statusCode, "HTTP status code");
                }else{
                    StringBuilder dumpBuilder = new StringBuilder(255);
                    Utils.dumpThrowable(error, dumpBuilder);
                    msg = ServerMessage.obtain(ServerMessage.STATUS_LOCAL_EXCEPTION, dumpBuilder.toString());
                }
                listener.onResponse(msg);
                listener = null;
            }
        }

        @Override
        public void onResponse(JSONObject response) {
            if(null != listener){
                listener.onResponse(Requester.instance().verifyResponse(response));
                listener = null;
            }
        }
    }

    public interface ResponseListener extends Response.Listener<ServerMessage>{}
}
