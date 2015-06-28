package com.wisape.android.util.image;

import android.graphics.Bitmap;

/**
 * @author Duke
 */
public class NativeProcess {

    static{
        System.loadLibrary("blur");
    }

    public static native void stack_blur(Bitmap bitmapOut, int radius, int threadCount, int threadIndex);
}
