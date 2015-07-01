package com.wisape.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.facebook.drawee.view.SimpleDraweeView;
import com.soundcloud.android.crop.Crop;
import com.wisape.android.R;
import com.wisape.android.content.PhotoProvider;
import com.wisape.android.util.FrescoUriUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by LeiGuoting on 1/7/15.
 */
public class UserProfileActivity extends AbsCompatActivity {

    public static void launch(Activity activity, int requestCode){
        activity.startActivityForResult(new Intent(activity.getApplicationContext(), UserProfileActivity.class), requestCode);
    }

    @InjectView(R.id.imageView)
    protected SimpleDraweeView imageView;

    private Uri destUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button)
    @SuppressWarnings("unused")
    protected void doBtnClicked(){
        PhotoSelectorActivity.launch(this, 1);
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
}
