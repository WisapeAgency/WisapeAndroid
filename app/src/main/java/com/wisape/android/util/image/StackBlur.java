package com.wisape.android.util.image;

import android.graphics.Bitmap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Duke
 */
public class StackBlur {




    //final int AVAILABLE_THREADS = Runtime.getRuntime().availableProcessors();
    private static ExecutorService mExecutor ;

    final static int RADIUS = 130;


    public final static void init(){
        mExecutor =  Executors.newFixedThreadPool(1);
    }

    public final static void  blur(Bitmap original,BlurListener listener){
        mExecutor.execute(new NativeTask(original,listener));
    }


    public final static void free(){
        if(null != mExecutor){
            if(!mExecutor.isShutdown())mExecutor.shutdown();
            mExecutor = null;
        }
    }



    public interface BlurListener{
        void onSuccessed(Bitmap bitmap);
    }




    private static class NativeTask implements Runnable {
        private final Bitmap bitmapOut;
        private BlurListener listener;

        public NativeTask(Bitmap bitmapOut,BlurListener listener) {
            this.bitmapOut = bitmapOut;
            this.listener = listener;
        }


        @Override
        public void run() {
            NativeProcess.stack_blur(bitmapOut, RADIUS, 1, 0);
            NativeProcess.stack_blur(bitmapOut, RADIUS, 1, 0);
            if(null != listener) listener.onSuccessed(bitmapOut);
        }
    }
}
