package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.wisape.android.R;
import com.wisape.android.http.HttpUrlConstancts;
import com.wisape.android.util.EnvironmentUtils;
import com.wisape.android.util.Utils;
import com.wisape.android.widget.ClipImageLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片裁剪
 * Created by huangmeng on 15/8/28.
 */
public class CutActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URI = "extra_img_uri";
    public static final int RQEUST_CODE_CROP_IMG = 0x01;
    public static final String EXRA_WIDTH = "width";
    public static final String EXRA_HEIGHT = "height";

    private static final int LOADER_SAVE_HEADER = 1;
    private static final String ARGS_HADER = "header";
    private static final String IMG_NAME = "img_name";

    private String thembImg;

    private Uri uri;

    public static void launch(Activity activity, Uri imgUri, int width, int height,String thembImg, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), CutActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, imgUri);
        intent.putExtra(EXRA_WIDTH, width);
        intent.putExtra(EXRA_HEIGHT, height);
        intent.putExtra(IMG_NAME,thembImg);
        activity.startActivityForResult(intent, requestCode);
    }


    private ClipImageLayout cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        int clipWidth = getIntent().getExtras().getInt(EXRA_WIDTH);
        int clipHeight = getIntent().getExtras().getInt(EXRA_HEIGHT);

        thembImg = getIntent().getExtras().getString(IMG_NAME);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）

        uri = getIntent().getExtras().getParcelable(EXTRA_IMAGE_URI);
        cropImageView = (ClipImageLayout) findViewById(R.id.clipImageLayout);
        if (clipWidth > width) {
            clipWidth = width;
        }
        if (clipHeight > height) {
            clipHeight = height;
        }
        cropImageView.setHorizontalPadding((width - clipWidth) / 2);
        cropImageView.setVertrialPadding((height - clipHeight) / 2);
        cropImageView.setImageBitmap(BitmapFactory.decodeFile(uri.getPath()));

    }

    @Override
    protected boolean onBackNavigation() {
        Bitmap bitmap = cropImageView.clip(uri);
        if (null != bitmap) {
            Bundle ars = new Bundle();
            ars.putParcelable(ARGS_HADER, bitmap);
            startLoad(LOADER_SAVE_HEADER, ars);
            return true;
        }
        finish();
        return true;
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message message = Message.obtain();
        Bitmap bitmap = args.getParcelable(ARGS_HADER);

        FileOutputStream fileOutputStream = null;
        File headerFile = new File(thembImg + ".jpg");
        if (headerFile.exists()){
            headerFile.delete();
        }
        try {
            fileOutputStream = new FileOutputStream(headerFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            message.arg1 = HttpUrlConstancts.STATUS_SUCCESS;
            message.obj = headerFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            message.arg1 = HttpUrlConstancts.STATUS_EXCEPTION;
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {

                }
            }
        }
        bitmap.recycle();
        return message;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        if (HttpUrlConstancts.STATUS_SUCCESS == data.arg1){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_IMAGE_URI, (String)data.obj);
            setResult(RESULT_OK, intent);
        }
        finish();
    }
}
