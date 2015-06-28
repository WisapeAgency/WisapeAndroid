package com.wisape.android.network;

/**
 * RequestListener
 * Created by Xugm on 15/6/16.
 */
public interface RequestListener {
    void onComplete(String respString);
    void onError(int errorCode, String errorMessage);
}
