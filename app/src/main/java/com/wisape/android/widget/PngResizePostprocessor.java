package com.wisape.android.widget;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by LeiGuoting on 30/6/15.
 */
public class PngResizePostprocessor extends BasePostprocessor{
    private static final String TAG = PngResizePostprocessor.class.getSimpleName();
    private final int expectWidth;

    PngResizePostprocessor(int expectWidth, int expectHeight){
        this.expectWidth = expectWidth;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int bmpWidth = sourceBitmap.getWidth();
        int bmpHeight = sourceBitmap.getHeight();
        Log.d(TAG, "#process bitmap oldWidth:" + bmpWidth + ", oldHeight:" + bmpHeight);
        if(bmpWidth <= expectWidth){
            return super.process(sourceBitmap, bitmapFactory);
        }else{
            int newWidth = expectWidth;
            float scaleWidth = (float) newWidth / (float)bmpWidth;
            int newHeight = (int)(((float)bmpHeight) * scaleWidth);
            float scaleHeight = (float) newHeight / bmpHeight;

            CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(newWidth, newHeight);
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            Bitmap newBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
            CloseableReference var4;
            try {
                Bitmaps.copyBitmap(bitmapRef.get(), newBitmap);
                var4 = CloseableReference.cloneOrNull(bitmapRef);
            } finally {
                CloseableReference.closeSafely(bitmapRef);
            }

            return var4;
        }
    }

    @Override
    public String getName() {
        return "PNG-Postprocessor";
    }
}
