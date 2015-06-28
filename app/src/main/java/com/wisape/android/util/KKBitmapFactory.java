package com.wisape.android.util;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

/**
 *
 * @author Duke
 */
public class KKBitmapFactory {

    public static Bitmap getBitmapFromFile (String path, int destWidth,int destHeight,Config config) {
        if (path == null)
            return null;

        int scale = 1;

        if (destWidth >= 1 && destHeight >= 1) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            final int width = o.outWidth;// emptyBitmap.getWidth();
            final int height = o.outHeight;// emptyBitmap.getHeight();

            int tmp_scale = 2;
            if (width > destWidth || height > destHeight) {
                int pic_total = width * height;
                int total = destWidth * destWidth;
                if (total != 0)
                    tmp_scale = (int) Math.sqrt((double) pic_total / total);

            }
            scale = tmp_scale >= 2 ? tmp_scale : scale;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.inDither = false;
        opts.inPurgeable = true;
        if(config != null)
            opts.inPreferredConfig = config;

        Bitmap bitmap = BitmapFactory.decodeFile(path , opts);
        return bitmap;
    }

    public static Bitmap getBitmap (Context context, int resId,int destWidth,int destHeight,Config config) {
        if (resId <= 0 || context == null)
            return null;

        Bitmap b_tmp = null;
        InputStream is = context.getResources().openRawResource(resId);
        if (is == null)
            return null;
        int scale = 1;

        if(destWidth>=1&& destHeight>=1){
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, o);

            final int width = o.outWidth;
            final int height = o.outHeight;

            int tmp_scale = 2;
            if (width > destWidth || height > destHeight) {
                int pic_total = width * height;
                int total = destWidth * destWidth;
                if (total != 0)
                    tmp_scale = (int) Math.sqrt((double) pic_total / total);

            }
            scale = tmp_scale >= 2 ? tmp_scale : scale;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options ();
        opts.inSampleSize = scale;
        opts.inTempStorage = new byte[64 * 1024];
        opts.inDither = false;
        opts.inPurgeable = true;
        if(null != config)
            opts.inPreferredConfig = config;

        try {
            b_tmp = BitmapFactory.decodeStream(is, null, opts);
        } catch(OutOfMemoryError e) {
            e.printStackTrace();
        }
        try {
            is.close();
            is = null;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return b_tmp;
    }

}
