package com.wisape.android.common;

import android.content.Context;
import android.util.Log;

/**
 * 自定义全局未处理异常捕获器
 * Created  on 13-12-13.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {


    private static CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例

    private CrashHandler() {
    }

    public synchronized static CrashHandler getInstance() {  //同步方法，以免单例多线程环境下出现异常
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context ctx) {  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e("未捕获的异常：", ex.getMessage() == null ? "" : ex.getMessage());
    }
}