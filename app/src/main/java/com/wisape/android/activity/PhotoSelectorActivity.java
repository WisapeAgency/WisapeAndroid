package com.wisape.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.wisape.android.R;
import com.wisape.android.bean.AppPhotoBucketInfo;
import com.wisape.android.bean.AppPhotoInfo;
import com.wisape.android.common.PhotoSelector;
import com.wisape.android.fragment.PhotoBucketsFragment;
import com.wisape.android.fragment.PhotoWallsFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * The class support UI where you can select some photo that from Android System or open camera to take a photo.
 * if you want to receive some photo that be selected by USER from this UI, you can do so as following:
 * 1)implementing onActivityResult method in Activity or Fragment.
 * 2)fetching photos from Intent.
 * <p/>
 * Created by LeiGuoting on 10/6/15.
 */
public class PhotoSelectorActivity extends BaseCompatActivity implements LoaderManager.LoaderCallbacks<Message>,
        PhotoWallsFragment.WallsCallback {
    private static final String TAG = "PhotoSelector";
    private static final int WHAT_PHOTOS = 1;
    private static final int WHAT_BUCKETS = 2;
    private static final int WHAT_ERROR = 3;

    private static final long ALL_IN_BUCKETS_ID = 0;
    private static final int CONTENT_ID = android.R.id.content;

    public static final String EXTRA_BUCKET_ID = "extra_bucket_id";
    public static final String EXTRA_BUCKET_LIST = "extra_bucket_list";

    public static void launch(Activity activity, int requestCode) {
        Intent intent = new Intent(activity.getApplicationContext(), PhotoSelectorActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void launch(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getActivity().getApplicationContext(), PhotoSelectorActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    private boolean isBucketView;
    private long bucketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null == savedInstanceState) {
            Fragment fragment = new PhotoWallsFragment();
            fragment.setHasOptionsMenu(true);
            getSupportFragmentManager().beginTransaction().add(CONTENT_ID, fragment).commit();
        }
        loadPhotos(bucketId);
    }

    private void loadPhotos(long bucketId){
        Bundle args = new Bundle();
        args.putLong(EXTRA_BUCKET_ID, bucketId);
        getSupportLoaderManager().restartLoader(WHAT_PHOTOS, args, this);
    }


    @Override
    public void onSwitchToBuckets() {
        getSupportLoaderManager().restartLoader(WHAT_BUCKETS, null, this);
    }

    @Override
    public Loader<Message> onCreateLoader(int id, final Bundle args) {
        AsyncTaskLoader<Message> loader;
        switch (id) {
            default:
                loader = null;
                break;

            case WHAT_PHOTOS :
                loader = new PhotosLoader(getApplicationContext(), args);
                break;

            case WHAT_BUCKETS:
                loader = new BucketsLoader(getApplicationContext());
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Message> loader, Message data) {
        if(isDestroyed() || null == data){
            return;
        }

        try{
            switch (loader.getId()){
                default :
                    break;

                case WHAT_PHOTOS :
                    AppPhotoInfo[] photos = (AppPhotoInfo[]) data.obj;
                    Fragment fragment = getSupportFragmentManager().findFragmentById(/*R.id.photo_walls*/CONTENT_ID);
                    if(null != fragment){
                        PhotoWallsFragment photoWallsFragment = (PhotoWallsFragment) fragment;
                        photoWallsFragment.updateData(photos);
                    }
                    break;

                case WHAT_BUCKETS :
                    ArrayList<AppPhotoBucketInfo> buckets = (ArrayList<AppPhotoBucketInfo>) data.obj;
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragment = fragmentManager.findFragmentById(R.id.photo_buckets);
                    if(null != fragment){
                        PhotoBucketsFragment bucketFragment = (PhotoBucketsFragment) fragment;
                        bucketFragment.updateData(buckets);
                    }else{
                        fragment = new PhotoBucketsFragment();
                        Bundle args = new Bundle();
                        fragment.setArguments(args);
                        args.putParcelableArrayList(EXTRA_BUCKET_LIST, buckets);
                        fragment.setHasOptionsMenu(true);
                        fragmentManager.beginTransaction().replace(CONTENT_ID, fragment).commitAllowingStateLoss();
                    }
                    break;

                case WHAT_ERROR :
                    Log.e(TAG, "", (Throwable) data.obj);
                    break;
            }
        }finally {
            data.recycle();
        }
    }

    @Override
    public void onLoaderReset(Loader<Message> loader) {
        loader.reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.destroyLoader(WHAT_BUCKETS);
        loaderManager.destroyLoader(WHAT_PHOTOS);
    }

    private static class PhotosLoader extends AsyncTaskLoader<Message>{
        private long bucketId;

        public PhotosLoader(Context context, Bundle args) {
            super(context);
            bucketId = null == args ? 0 : args.getLong(EXTRA_BUCKET_ID, 0);
        }

        @Override
        public Message loadInBackground() {
            final Context context = getContext();
            PhotoSelector<AppPhotoInfo, AppPhotoBucketInfo> selector = PhotoSelector.instance(AppPhotoInfo.class, AppPhotoBucketInfo.class);
            AppPhotoInfo[] photos;
            Message msg = Message.obtain();
            try {
                if (0 == bucketId) {
                    photos = selector.acquireAllPhotos(context);
                } else {
                    photos = selector.acquirePhotos(context, bucketId);
                }

                int size = (null == photos ? 0 : photos.length);

                AppPhotoInfo[] newDatas = new AppPhotoInfo[size + 1];
                newDatas[0] = new AppPhotoInfo(AppPhotoInfo.VIEW_TYPE_CAMERA);
                if (0 < size) {
                    System.arraycopy(photos, 0, newDatas, 1, size);
                }
                photos = newDatas;
                msg.what = getId();
                msg.obj = photos;
            } catch (java.lang.InstantiationException e) {
                msg.what = WHAT_ERROR;
                msg.obj = e;
            } catch (IllegalAccessException e) {
                msg.what = WHAT_ERROR;
                msg.obj = e;
            }
            return msg;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onReset() {
            forceLoad();
        }
    }

    private static class BucketsLoader extends AsyncTaskLoader<Message>{
        public BucketsLoader(Context context) {
            super(context);
        }

        @Override
        public Message loadInBackground() {
            Message msg = Message.obtain();
            try {
                final Context context = getContext();
                List<AppPhotoBucketInfo> buckets = PhotoSelector.instance(AppPhotoInfo.class, AppPhotoBucketInfo.class).acquireBuckets(context);
                int size = (null == buckets ? 0 : buckets.size());
                if(0 != size){
                    AppPhotoBucketInfo allInBucket = new AppPhotoBucketInfo();
                    allInBucket.id = ALL_IN_BUCKETS_ID;
                    allInBucket.displayName = context.getString(R.string.photo_bucket_all);
                    int total = 0;
                    for(AppPhotoBucketInfo bucket : buckets){
                        total += bucket.childrenCount;
                    }
                    allInBucket.childrenCount = total;
                    List<AppPhotoBucketInfo> newBuckets = new ArrayList(size + 1);
                    newBuckets.add(allInBucket);
                    newBuckets.addAll(buckets);
                    buckets.clear();
                    msg.obj = newBuckets;
                    msg.what = getId();
                }
            } catch (java.lang.InstantiationException e) {
                msg.what = WHAT_ERROR;
                msg.obj = e;
            } catch (IllegalAccessException e) {
                msg.what = WHAT_ERROR;
                msg.obj = e;
            }
            return msg;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        protected void onReset() {
            forceLoad();
        }
    }
}
