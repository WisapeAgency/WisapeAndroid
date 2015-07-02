package com.wisape.android.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.wisape.android.model.AppPhotoBucketInfo;
import com.wisape.android.common.PhotoSelector;
import com.wisape.android.model.AppPhotoInfo;
import com.wisape.android.model.PhotoBucketInfo;
import com.wisape.android.util.FrescoUriUtils;

import java.io.File;
import java.io.FileNotFoundException;

import static android.content.ContentResolver.SCHEME_CONTENT;

/**
 * Created by LeiGuoting on 17/6/15.
 */
public class PhotoProvider extends ContentProvider{
    private static final String TAG = PhotoProvider.class.getSimpleName();

    private static final String AUTHORITY = "com.wisape.photo";
    private static final String PATH_THUMB_BUCKET = "/thumb/bucket";
    private static final String PATH_THUMB_PHOTO = "/thumb/photo";
    private static final String EXTRA_ID = "extra_id";

    public static Uri getBucketThumbUri(long bucketId){
        return new Uri.Builder().scheme(SCHEME_CONTENT).authority(AUTHORITY).path(PATH_THUMB_BUCKET).appendQueryParameter(EXTRA_ID, Long.toString(bucketId)).build();
    }

    public static Uri getPhotoThumbUri(long photoId){
        return new Uri.Builder().scheme(SCHEME_CONTENT).authority(AUTHORITY).path(PATH_THUMB_PHOTO).appendQueryParameter(EXTRA_ID, Long.toString(photoId)).build();
    }

    public static Uri getPhotoUri(String path){
        return new Uri.Builder().scheme(SCHEME_CONTENT).authority(AUTHORITY).path(path).build();
    }

    @Override
    public boolean onCreate() {
        //do nothing
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //do nothing
        return null;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "# getType uri:" + uri.toString());
        return uri.getPath().startsWith("thumb") ? "image/jpeg" : null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //do nothing
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //do nothing
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //do nothing
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        final File file = new File(uri.getPath());
        final int fileMode = modeToMode(mode);
        return ParcelFileDescriptor.open(file, fileMode);
    }

    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal) throws FileNotFoundException {
        String path = uri.getPath();
        Log.d(TAG,"#openTypedAssetFile uri:" + uri + ", path:" + path + ", PATH_THUMB_BUCKET:" + PATH_THUMB_BUCKET + ", currentThread:" + Thread.currentThread().getName());
        if(null == path || 0 == path.length()){
            return null;
        }

        String data;
        final Context context = getContext();
        if(PATH_THUMB_BUCKET.equals(path)){
            long id = Long.parseLong(uri.getQueryParameter(EXTRA_ID));
            PhotoSelector<AppPhotoInfo, PhotoBucketInfo> selector = PhotoSelector.instance(AppPhotoInfo.class, AppPhotoBucketInfo.class);
            data = selector.acquireBucketMiniThumbData(context, id);
        }else if(PATH_THUMB_PHOTO.equals(path)){
            long id = Long.parseLong(uri.getQueryParameter(EXTRA_ID));
            PhotoSelector<AppPhotoInfo, PhotoBucketInfo> selector = PhotoSelector.instance(AppPhotoInfo.class, AppPhotoBucketInfo.class);
            data = selector.acquirePhotoMiniThumbData(context, id);
        }else{
            return super.openTypedAssetFile(uri, mimeTypeFilter, opts, signal);
        }

        if(null == data){
            Log.d(TAG, "#openTypedAssetFile data is null");
            return null;
        }

        Uri thumbUri = FrescoUriUtils.fromFilePath(data);
        Log.d(TAG, "#openTypedAssetFile thumbUri:" + thumbUri.toString());
        return super.openTypedAssetFile(thumbUri, mimeTypeFilter, opts, signal);
    }

    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }
}
