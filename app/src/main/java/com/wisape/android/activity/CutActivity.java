package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.wisape.android.R;
import com.wisape.android.widget.ClipImageLayout;

/**
 * 图片裁剪
 * Created by huangmeng on 15/8/28.
 */
public class CutActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URI = "extra_img_uri";
    public static final int RQEUST_CODE_CROP_IMG = 0x01;
    public static final String EXRA_WIDTH = "width";
    public static final String EXRA_HEIGHT = "height";

    public static void launch(Activity activity,Uri imgUri,int width,int height,int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(),CutActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI,imgUri);
        intent.putExtra(EXRA_WIDTH,width);
        intent.putExtra(EXRA_HEIGHT,height);
        activity.startActivityForResult(intent,requestCode);
    }


    private ClipImageLayout cropImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        int clipWidth = getIntent().getExtras().getInt(EXRA_WIDTH);
        int clipHeight = getIntent().getExtras().getInt(EXRA_HEIGHT);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        int width = metric.widthPixels; // 屏幕宽度（像素）
        int height = metric.heightPixels; // 屏幕高度（像素）

        Uri uri = getIntent().getExtras().getParcelable(EXTRA_IMAGE_URI);
        cropImageView = (ClipImageLayout)findViewById(R.id.clipImageLayout);
        cropImageView.setHorizontalPadding((width - clipWidth) / 2);
        cropImageView.setVertrialPadding((height - clipHeight) / 2);
        cropImageView.setImageBitmap(BitmapFactory.decodeFile(uri.getPath()));

    }

    @Override
    protected boolean onBackNavigation() {
        Bitmap bitmap = cropImageView.clip();
        if(null != bitmap){
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,null,null));
            bitmap.recycle();
            Intent intent = new Intent();
            intent.putExtra(EXTRA_IMAGE_URI, uri);
            setResult(RESULT_OK, intent);
        }
        finish();
        return true;
    }
}
