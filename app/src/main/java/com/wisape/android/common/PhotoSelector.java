package com.wisape.android.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;

import com.wisape.android.bean.PhotoBucketInfo;
import com.wisape.android.bean.PhotoInfo;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.Images.ImageColumns.DATA;
import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.Images.ImageColumns.DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC;
import static android.provider.MediaStore.Images.ImageColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;


/**
 * Created by LeiGuoting on 15/6/15.
 */
public class PhotoSelector<P extends PhotoInfo, B extends PhotoBucketInfo> {
    public static final short ORDER_BY_DISPLAY_NAME_ASC = 1;
    public static final short ORDER_BY_DISPLAY_NAME_DESC = 1 << 1;
    public static final short ORDER_BY_DATE_ASC = 1 << 2;
    public static final short ORDER_BY_DATE_DESC = 1 << 3;
    public static final short ORDER_BY_ID_ASC = 1 << 4;
    public static final short ORDER_BY_ID_DESC = 1 << 5;

    private static final String DESC = "DESC";
    private static final String ASC = "ASC";

    private static WeakReference<PhotoSelector> reference;

    public static PhotoSelector instance(Class<? extends PhotoInfo> photoClass, Class<? extends PhotoBucketInfo> bucketClass) {
        PhotoSelector selector;
        if (null == reference || (null == (selector = reference.get()))) {
            synchronized (PhotoSelector.class) {
                if (null == reference || (null == (selector = reference.get()))) {
                    selector = new PhotoSelector(photoClass, bucketClass);
                    reference = new WeakReference(selector);
                }
            }
        }
        return selector;
    }

    private Class<? extends PhotoInfo> photoClass;
    private Class<? extends PhotoBucketInfo> bucketClass;

    private PhotoSelector(Class<? extends PhotoInfo> photoClass, Class<? extends PhotoBucketInfo> bucketClass) {
        if (null == photoClass || null == bucketClass) {
            throw new IllegalArgumentException("The PhotoInfo Class or PhotoBucketInfo Class can not be null.");
        }

        this.photoClass = photoClass;
        this.bucketClass = bucketClass;
    }

    /**
     * @param context
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public List<B> acquireBuckets(Context context)throws InstantiationException, IllegalAccessException{
        return acquireBuckets(context, makeBucketOrderBy(ORDER_BY_DISPLAY_NAME_ASC));
    }

    /**
     * @param context
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public List<B> acquireBuckets(Context context, int orderBy)throws InstantiationException, IllegalAccessException{
        return acquireBuckets(context, makeBucketOrderBy(orderBy));
    }

    /**
     * @param context
     * @param orderBy
     * @return maybe return null
     */
    public List<B> acquireBuckets(Context context, String orderBy) throws InstantiationException, IllegalAccessException {
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        String[] projection = {BUCKET_ID,
                BUCKET_DISPLAY_NAME,
                "count(" + MediaStore.Images.ImageColumns.BUCKET_ID + ") AS count"
        };

        StringBuilder groupByBuilder = new StringBuilder(64);
        groupByBuilder.append("1) GROUP BY ").append(MediaStore.Images.ImageColumns.BUCKET_ID).append(", (").append(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        String groupBy = groupByBuilder.toString();
        Cursor cursor = MediaStore.Images.Media.query(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, groupBy, null, orderBy);

        final int count = (null == cursor ? 0 : cursor.getCount());
        if (0 == count) {
            if (null != cursor) {
                cursor.close();
            }
            return null;
        }

        ArrayList<B> buckets = null;
        try {
            final int idIndex = cursor.getColumnIndexOrThrow(BUCKET_ID);
            final int displayNameIndex = cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME);
            final int countIndex = cursor.getColumnIndexOrThrow("count");

            B bucket;
            buckets = new ArrayList(count);
            int childrenCount;
            for (; cursor.moveToNext(); ) {
                childrenCount = cursor.getInt(countIndex);
                if(0 == childrenCount){
                    continue;
                }

                bucket = (B) bucketClass.newInstance();
                buckets.add(bucket);
                bucket.id = cursor.getLong(idIndex);
                bucket.displayName = cursor.getString(displayNameIndex);
                bucket.childrenCount = childrenCount;
            }
        } finally {
            cursor.close();
        }
        return buckets;
    }

    /**
     * @param context
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquireAllPhotos(Context context)throws InstantiationException, IllegalAccessException{
        return acquirePhotos(context, null, null, makePhotoOrderBy(ORDER_BY_DATE_DESC));
    }

    /**
     * @param context
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquireAllPhotos(Context context, int orderBy)throws InstantiationException, IllegalAccessException{
        return acquirePhotos(context, null, null, makePhotoOrderBy(orderBy));
    }

    /**
     * @param context
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquireAllPhotos(Context context, String orderBy)throws InstantiationException, IllegalAccessException{
        return acquirePhotos(context, null, null, orderBy);
    }

    /**
     * @param context
     * @param bucketId
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquirePhotos(Context context, long bucketId)throws InstantiationException, IllegalAccessException{
        return acquirePhotos(context, bucketId, makePhotoOrderBy(ORDER_BY_DISPLAY_NAME_ASC));
    }

    /**
     *
     * @param context
     * @param bucketId
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquirePhotos(Context context, long bucketId, int orderBy)throws InstantiationException, IllegalAccessException{
        return acquirePhotos(context, bucketId, makePhotoOrderBy(orderBy));
    }

    /**
     *
     * @param context
     * @param bucketId
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquirePhotos(Context context, long bucketId, String orderBy)throws InstantiationException, IllegalAccessException{
        String selection = new StringBuilder(BUCKET_ID.length() + 2).append(BUCKET_ID).append("=?").toString();
        String selectionArgs[] = {Long.toString(bucketId)};
        return acquirePhotos(context, selection, selectionArgs, orderBy);
    }

    /**
     *
     * @param context
     * @param selection
     * @param selectionArgs
     * @param orderBy
     * @return maybe return null.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public P[] acquirePhotos(Context context, String selection, String [] selectionArgs, String orderBy)throws InstantiationException, IllegalAccessException{
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        String [] projection ={_ID,
                BUCKET_ID,
                DISPLAY_NAME,
                BUCKET_DISPLAY_NAME,
                DATE_TAKEN,
                DATA
        };

        Cursor cursor = MediaStore.Images.Media.query(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, orderBy);
        final int count = (null == cursor ? 0 : cursor.getCount());
        if (0 == count) {
            if (null != cursor) {
                cursor.close();
            }
            return null;
        }

        ArrayList<P> photos = null;
        try{
            final int idIndex = cursor.getColumnIndexOrThrow(_ID);
            final int bucketIdIndex = cursor.getColumnIndexOrThrow(BUCKET_ID);
            final int displayNameIndex = cursor.getColumnIndexOrThrow(DISPLAY_NAME);
            final int bucketDisplayNameIndex = cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME);
            final int dateTakenIndex = cursor.getColumnIndexOrThrow(DATE_TAKEN);
            final int dataIndex = cursor.getColumnIndexOrThrow(DATA);

            P photo;
            photos = new ArrayList(count);
            for(; cursor.moveToNext();){
                photo = (P)photoClass.newInstance();
                photos.add(photo);
                photo.id = cursor.getLong(idIndex);
                photo.bucketId = cursor.getLong(bucketIdIndex);
                photo.displayName = cursor.getString(displayNameIndex);
                photo.bucketDisplayName = cursor.getString(bucketDisplayNameIndex);
                photo.dateTakenInMills = cursor.getLong(dateTakenIndex);
                photo.data = cursor.getString(dataIndex);
            }
        }finally {
            cursor.close();
        }

        return photos.toArray((P[])Array.newInstance(photoClass, photos.size()));
    }

    private String makePhotoOrderBy(int orderBy){
        if(0 == orderBy){
            return null;
        }

        final String token = ", ";
        StringBuilder orderByBuilder = new StringBuilder(64);
        if(0 != (ORDER_BY_DISPLAY_NAME_DESC & orderBy)){
            orderByBuilder.append(DISPLAY_NAME).append(" ").append(DESC).append(token);
        }else if(0 != (ORDER_BY_DISPLAY_NAME_ASC & orderBy)){
            orderByBuilder.append(DISPLAY_NAME).append(" ").append(ASC).append(token);
        }

        if(0 != (ORDER_BY_DATE_DESC & orderBy)){
            orderByBuilder.append(DATE_ADDED).append(" ").append(DESC).append(token);
        }else if(0 != (ORDER_BY_DATE_ASC & orderBy)){
            orderByBuilder.append(DATE_ADDED).append(" ").append(ASC).append(token);
        }

        if(0 != (ORDER_BY_ID_DESC & orderBy)){
            orderByBuilder.append(_ID).append(" ").append(DESC).append(token);
        }else if(0 != (ORDER_BY_ID_ASC & orderBy)){
            orderByBuilder.append(_ID).append(" ").append(ASC).append(token);
        }

        return 0 == orderByBuilder.length() ? null : orderByBuilder.substring(0, orderByBuilder.length() - token.length());
    }

    private String makeBucketOrderBy(int orderBy){
        if(0 == orderBy){
            return null;
        }

        final String token = ", ";
        StringBuilder orderByBuilder = new StringBuilder(32);
        if(0 != (ORDER_BY_DISPLAY_NAME_DESC & orderBy)){
            orderByBuilder.append(BUCKET_DISPLAY_NAME).append(" ").append(DESC).append(token);
        }else if(0 != (ORDER_BY_DISPLAY_NAME_ASC & orderBy)){
            orderByBuilder.append(BUCKET_DISPLAY_NAME).append(" ").append(ASC).append(token);
        }

        if(0 != (ORDER_BY_ID_DESC & orderBy)){
            orderByBuilder.append(BUCKET_ID).append(" ").append(DESC).append(token);
        }else if(0 != (ORDER_BY_ID_ASC & orderBy)){
            orderByBuilder.append(BUCKET_ID).append(" ").append(ASC).append(token);
        }

        return 0 == orderByBuilder.length() ? null : orderByBuilder.substring(0, orderByBuilder.length() - token.length());
    }

    /**
     *
     * @param context
     * @param bucketId
     * @return maybe return null.
     */
    public Bitmap acquireBucketMiniThumb(Context context, long bucketId){
        return acquireBucketMiniThumb(context, bucketId, ORDER_BY_DATE_DESC);
    }

    /**
     *
     * @param context
     * @param bucketId
     * @param orderBy
     * @return maybe return null.
     */
    public Bitmap acquireBucketMiniThumb(Context context, long bucketId, int orderBy){
        return acquireBucketMiniThumb(context, bucketId, makePhotoOrderBy(orderBy));
    }

    /**
     *
     * @param context
     * @param bucketId
     * @param orderBy
     * @return maybe return null.
     */
    public Bitmap acquireBucketMiniThumb(Context context, long bucketId, String orderBy){
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        String [] projection ={_ID,
                BUCKET_ID,
                MINI_THUMB_MAGIC
        };

        final String selection;
        final String[] selectionArgs;
        if(0 == bucketId){
            selection = null;
            selectionArgs = null;
        }else{
            selection = new StringBuilder(BUCKET_ID.length() + 2).append(BUCKET_ID).append("=?").toString();
            selectionArgs = new String[]{Long.toString(bucketId)};
        }
        String orderByAndLimit = new StringBuilder(orderBy.length() + 10).append(orderBy).append(" LIMIT 1").toString();
        Cursor cursor = Media.query(contentResolver, Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, orderByAndLimit);
        final int count = (null == cursor ? 0 : cursor.getCount());
        if (0 == count) {
            if (null != cursor) {
                cursor.close();
            }
            return null;
        }

        Bitmap thumb = null;
        try{
            final int miniThumbMagicIndex = cursor.getColumnIndexOrThrow(MINI_THUMB_MAGIC);
            if(cursor.moveToNext()){
                long miniThumbId = cursor.getLong(miniThumbMagicIndex);
                BitmapFactory.Options options = new BitmapFactory.Options();
                thumb = Thumbnails.getThumbnail(contentResolver, miniThumbId, Thumbnails.MINI_KIND, options);
            }
        }finally {
            cursor.close();
        }
        return thumb;
    }

    public String acquireBucketMiniThumbData(Context context, long bucketId){
        return acquireBucketMiniThumbData(context, bucketId, ORDER_BY_DATE_DESC);
    }

    public String acquireBucketMiniThumbData(Context context, long bucketId, int orderBy){
        return acquireBucketMiniThumbData(context, bucketId, makePhotoOrderBy(orderBy));
    }

    public String acquireBucketMiniThumbData(Context context, long bucketId, String orderBy){
        String [] projection ={_ID,
                BUCKET_ID,
                DATA
        };

        final String selection;
        final String[] selectionArgs;
        if(0 == bucketId){
            selection = null;
            selectionArgs = null;
        }else{
            selection = new StringBuilder(BUCKET_ID.length() + 2).append(BUCKET_ID).append("=?").toString();
            selectionArgs = new String[]{Long.toString(bucketId)};
        }
        String orderByAndLimit = new StringBuilder(orderBy.length() + 10).append(orderBy).append(" LIMIT 1").toString();
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        Cursor cursor = Media.query(contentResolver, Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, orderByAndLimit);
        final int count = (null == cursor ? 0 : cursor.getCount());
        if (0 == count) {
            if (null != cursor) {
                cursor.close();
            }
            return null;
        }

        String data = null;
        try{
            final int dataIndex = cursor.getColumnIndexOrThrow(DATA);
            if(cursor.moveToNext()){
                data = cursor.getString(dataIndex);
            }
        }finally {
            cursor.close();
        }
        return data;
    }

    public String acquirePhotoMiniThumbData(Context context, long photoId){
        return acquirePhotoMiniThumbData(context.getContentResolver(), photoId);
    }

    private String acquirePhotoMiniThumbData(ContentResolver contentResolver, long photoId){
        String [] projection ={Thumbnails.IMAGE_ID,
                Thumbnails.DATA,
                Thumbnails.HEIGHT,
                Thumbnails.WIDTH
        };

        Cursor cursor = contentResolver.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, Thumbnails.IMAGE_ID + "=?", new String[]{Long.toString(photoId)}, Thumbnails.DEFAULT_SORT_ORDER);
        int count = (null == cursor ? 0 : cursor.getCount());
        if (0 == count) {
            if (null != cursor) {
                cursor.close();
            }

            cursor = contentResolver.query(Thumbnails.INTERNAL_CONTENT_URI, projection, Thumbnails.IMAGE_ID + "=?", new String[]{Long.toString(photoId)}, Thumbnails.DEFAULT_SORT_ORDER);
            count = (null == cursor ? 0 : cursor.getCount());
            //Log.d("##", "#acquirePhotoMiniThumbData from INTERNAL_CONTENT_URI, count:" + count);
            if(0 == count){
                if (null != cursor) {
                    cursor.close();
                }
                return null;
            }
        }

        String data = null;
        try{
            final int dataIndex = cursor.getColumnIndexOrThrow(Thumbnails.DATA);
            if(cursor.moveToNext()){
                data = cursor.getString(dataIndex);
            }
        }finally {
            cursor.close();
        }
        return data;
    }
}
