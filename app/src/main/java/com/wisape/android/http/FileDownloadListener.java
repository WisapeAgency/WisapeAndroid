package com.wisape.android.http;

/**
 * 文件下载监听接口
 * Created by huangmeng on 15/9/17.
 */
public interface FileDownloadListener {

    /**
     * 下载与保存成功
     */
    void onSuccess(String filePath);

    /**
     * 下载或者保存失败
     */
    void onError(String msg);

}
