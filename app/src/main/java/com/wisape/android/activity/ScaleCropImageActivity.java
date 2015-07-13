package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.soundcloud.android.crop.CropUtil;
import com.soundcloud.android.crop.HighlightView;
import com.soundcloud.android.crop.RotateBitmap;
import com.wisape.android.R;
import com.wisape.android.widget.ScaleCropImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by LeiGuoting on 11/7/15.
 */
public class ScaleCropImageActivity extends AbsMonitoredActivity{
    private static final String TAG = ScaleCropImageActivity.class.getSimpleName();
    private static final int DEFAULT_LOADER_ID = Integer.MAX_VALUE;
    private static final String EXTRA_WHAT = "loader_what";
    protected static final int STATUS_EXCEPTION = Integer.MIN_VALUE;
    protected static final int STATUS_SUCCESS = 1;

    private final Handler handler = new Handler();

    public static final int REQUEST_CODE_CROP = 0x103;
    public static final int RESULT_ERROR = 404;

    private static final String EXTRA_SOURCE = "_source";
    private static final String EXTRA_DESTINATION = "destination";

    private static final int WHAT_LOAD_IMAGE = 0x01;

    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    public interface Extra {
        String ASPECT_X = "aspect_x";
        String ASPECT_Y = "aspect_y";
        String MAX_X = "max_x";
        String MAX_Y = "max_y";
        String ERROR = "error";
    }

    public static void launch(Activity activity, int requestCode){
        Intent intent = new Intent(activity.getApplicationContext(), ScaleCropImageActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static Intent getIntent(Context context, Uri source, Uri destination){
        Intent intent = new Intent(context.getApplicationContext(), ScaleCropImageActivity.class);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_DESTINATION, destination);
        return intent;
    }

    private int aspectX;
    private int aspectY;
    // Output image
    private int maxX;
    private int maxY;
    private int exifRotation;

    private Uri source;
    private Uri destination;

    private RotateBitmap rotateBitmap;

    private HighlightView cropView;
    private ScaleCropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_crop_image);
        cropImageView = (ScaleCropImageView)findViewById(R.id.crop_image);
        Bundle args;
        if(null == savedInstanceState){
            args = getIntent().getExtras();
        }else{
            args = savedInstanceState;
        }
        source = args.getParcelable(EXTRA_SOURCE);
        destination = args.getParcelable(EXTRA_DESTINATION);
        aspectX = 1;
        aspectY = 1;
        Log.d(TAG, "#onCreate source:" + source);
        Log.d(TAG, "#onCreate destination:" + destination);

        Bundle params = new Bundle();
        params.putParcelable(EXTRA_SOURCE, source);
        startLoad(WHAT_LOAD_IMAGE, params);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_SOURCE, source);
        outState.putParcelable(EXTRA_DESTINATION, destination);
    }

    @Override
    protected Message onLoadBackgroundRunning(int what, Bundle args) throws AsyncLoaderError {
        Message msg = Message.obtain();
        msg.what = what;
        switch (what){
            default :
                return msg;

            case WHAT_LOAD_IMAGE :
                Uri source = args.getParcelable(EXTRA_SOURCE);
                int exifRotation = CropUtil.getExifRotation(CropUtil.getFromMediaUri(this, getContentResolver(), source));

                InputStream is = null;
                try {
                    int sampleSize = calculateBitmapSampleSize(source);
                    is = getContentResolver().openInputStream(source);
                    BitmapFactory.Options option = new BitmapFactory.Options();
                    option.inSampleSize = sampleSize;
                    RotateBitmap rotateBitmap = new RotateBitmap(BitmapFactory.decodeStream(is, null, option), exifRotation);
                    msg.obj = rotateBitmap;
                } catch (IOException e) {
                    Log.e(TAG, "Error reading image: " + e.getMessage(), e);
                    setResultException(e);
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "OOM reading image: " + e.getMessage(), e);
                    setResultException(e);
                } finally {
                    CropUtil.closeSilently(is);
                }
                break;
        }
        return msg;
    }

    @Override
    protected void onLoadCompleted(Message data) {
        switch (data.what){
            default :
                return;

            case WHAT_LOAD_IMAGE :
                RotateBitmap rotateBitmap = (RotateBitmap) data.obj;
                cropImageView.setImageRotateBitmapResetBase(rotateBitmap, true);
                this.rotateBitmap = rotateBitmap;

                CropUtil.startBackgroundJob(this, null, getResources().getString(com.soundcloud.android.crop.R.string.crop__wait),
                        new Runnable() {
                            public void run() {
                                final CountDownLatch latch = new CountDownLatch(1);
                                handler.post(new Runnable() {
                                    public void run() {
                                        if (cropImageView.getScale() == 1F) {
                                            cropImageView.center(true, true);
                                        }
                                        latch.countDown();
                                    }
                                });
                                try {
                                    latch.await();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                new Cropper().crop();
                            }
                        }, handler
                );
                break;
        }
    }

    private int calculateBitmapSampleSize(Uri bitmapUri) throws IOException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            CropUtil.closeSilently(is);
        }

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    private int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }

    private void setResultException(Throwable throwable) {
        setResult(RESULT_ERROR, new Intent().putExtra(Extra.ERROR, throwable));
    }

    private void doSaveCrop(){

    }

    private class Cropper {

        private void makeDefault() {
            if (rotateBitmap == null) {
                return;
            }

            HighlightView hv = new HighlightView(cropImageView);
            final int width = rotateBitmap.getWidth();
            final int height = rotateBitmap.getHeight();

            Rect imageRect = new Rect(0, 0, width, height);

            // Make the default size about 4/5 of the width or height
            int cropWidth = Math.min(width, height) * 4 / 5;
            @SuppressWarnings("SuspiciousNameCombination")
            int cropHeight = cropWidth;

            if (aspectX != 0 && aspectY != 0) {
                if (aspectX > aspectY) {
                    cropHeight = cropWidth * aspectY / aspectX;
                } else {
                    cropWidth = cropHeight * aspectX / aspectY;
                }
            }

            int x = (width - cropWidth) / 2;
            int y = (height - cropHeight) / 2;

            RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
            hv.setup(cropImageView.getUnrotatedMatrix(), imageRect, cropRect, aspectX != 0 && aspectY != 0);
            cropImageView.add(hv);
        }

        public void crop() {
            handler.post(new Runnable() {
                public void run() {
                    makeDefault();
                    cropImageView.invalidate();
                    ArrayList<HighlightView> highlightViews = cropImageView.getHighlightViews();
                    if (highlightViews.size() == 1) {
                        cropView = highlightViews.get(0);
                        cropView.setFocus(true);
                    }
                }
            });
        }
    }
}
