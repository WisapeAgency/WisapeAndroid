package com.wisape.android.network;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LeiGuoting on 5/6/15.
 */
/*package*/final class OkUrlHttpStack extends HurlStack{

    private final OkUrlFactory okUrlFactory;

    public OkUrlHttpStack() {
        this(new OkUrlFactory(new OkHttpClient()));
    }
    public OkUrlHttpStack(OkUrlFactory okUrlFactory) {
        if (okUrlFactory == null) {
            throw new NullPointerException("Client must not be null.");
        }
        this.okUrlFactory = okUrlFactory;
    }
    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        return okUrlFactory.open(url);
    }
}
