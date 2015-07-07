package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.zxing.WriterException;
import com.soundcloud.android.crop.Crop;
import com.wisape.android.R;
import com.wisape.android.common.QRCodeHelper;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.util.FrescoUriUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by LeiGuoting on 1/7/15.
 */
public class TestActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Message>{
    private static final String TAG = TestActivity.class.getSimpleName();

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(new Intent(activity.getApplicationContext(), TestActivity.class), requestCode);
    }

    @InjectView(R.id.imageView)
    protected SimpleDraweeView imageView;

    private Uri destUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button)
    @SuppressWarnings("unused")
    protected void doBtnClicked(){
        PhotoSelectorActivity.launch(this, 1);
    }

    @OnClick(R.id.qr_code)
    @SuppressWarnings("unused")
    protected void doQRCodeClicked(){
        getSupportLoaderManager().restartLoader(1, null, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(1 == requestCode){
            if(RESULT_OK == resultCode){
                Uri imageUri = data.getParcelableExtra(PhotoSelectorActivity.EXTRA_IMAGE_URI);
                File photoFile = new File(Environment.getExternalStorageDirectory(),  "CropPic.jpg");
                destUri = Uri.fromFile(photoFile);
                Crop.of(PhotoProvider.getPhotoUri(imageUri.getPath()), destUri).asSquare().start(this);
            }
        }else if(Crop.REQUEST_CROP == requestCode){
            if(RESULT_OK == resultCode){
                imageView.setImageURI(FrescoUriUtils.fromFilePath(destUri.getPath()));
                destUri = null;
            }
        }

        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Loader<Message> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Message>(getApplicationContext()) {
            @Override
            public Message loadInBackground() {
                Message msg = Message.obtain();
                try {
                    Bitmap qrCode = QRCodeHelper.createQRImage("https://github.com/zxing/zxing");
                    msg.what = 1;
                    msg.obj = qrCode;
                } catch (WriterException e) {
                    Log.e(TAG, "", e);
                    msg.what = -1;
                }
                return msg;
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        if(isDestroyed() || null == data){
            return;
        }

        try{
            if(1 == data.what){
                Bitmap qrCode = (Bitmap) data.obj;
                imageView.setImageBitmap(qrCode);
            }
        }finally {
            data.recycle();
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        //do nothing
    }
}
