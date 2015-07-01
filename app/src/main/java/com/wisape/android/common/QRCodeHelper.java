package com.wisape.android.common;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by LeiGuoting on 1/7/15.
 */
public class QRCodeHelper {

    public static Bitmap createQRImage(String url) throws WriterException {
        final int width = 500;
        final int height = 500;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, width, height);
        int[] pixels = new int[width * height];
        for(int y = 0; y < height; y ++){
            for (int x = 0; x < width; x ++){
                if (matrix.get(x, y)){
                    pixels[y * width + x] = 0xff000000;
                }else{
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }
}
