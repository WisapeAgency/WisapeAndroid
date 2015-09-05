package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.wisape.android.R;
import com.wisape.android.widget.ClipImageLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 图片裁剪
 * Created by huangmeng on 15/8/28.
 */
public class CutActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URI = "extra_img_uri";
    public static final int RQEUST_CODE_CROP_IMG = 0x01;

    public static void launch(Activity activity,Uri imgUri,int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(),CutActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI,imgUri);
        activity.startActivityForResult(intent,requestCode);
    }


    @InjectView(R.id.clip_image_layout)
    protected ClipImageLayout clipImageLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        ButterKnife.inject(this);
        Uri uri = getIntent().getExtras().getParcelable(EXTRA_IMAGE_URI);
        clipImageLayout.setImgIconUri(uri);
    }

    @Override
    protected boolean onBackNavigation() {

        Bitmap bitmap = clipImageLayout.clip();
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
