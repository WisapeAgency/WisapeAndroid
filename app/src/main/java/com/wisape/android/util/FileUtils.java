package com.wisape.android.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件操作工具
 * Created by hm on 2015/8/14.
 */
public class FileUtils {

    public static void deleteFileInDir(File dir) {
        deleteAllFile(dir.getPath());
        deleteAllFullFolder(dir.getPath());
    }

    /**
     * @param folderFullPath 文件夹路径
     */
    //删除文件夹下的所有文件
    private static boolean deleteAllFile(String folderFullPath) {
        boolean ret = false;
        File folder = new File(folderFullPath);
        if (folder.exists()) {
            if (folder.isDirectory()) {
                File[] fileList = folder.listFiles();
                for (File file: fileList) {
                    deleteAllFile(file.getPath());

                }
                for (int i = 0; i < fileList.length; i++) {
                    String filePath = fileList[i].getPath();
                }
            }
            if (folder.isFile()) {
                folder.delete();
            }
        }
        return ret;
    }

    //删除文件夹下的所有空文件夹,注意必须是空文件夹,该方法可能一次不能全部删除,需在调用时多次执行
    private static boolean deleteAllFullFolder(String folderFullPath) {
        boolean ret = false;
        File file = new File(folderFullPath);
        if (file.exists()) {
            File[] fileList = file.listFiles();
            if (fileList.length > 0) {
                for (int i = 0; i < fileList.length; i++) {
                    String filePath = fileList[i].getPath();
                    deleteAllFullFolder(filePath);
                }
            } else {
                file.delete();
            }
        }
        return ret;
    }

    /**
     * 将uri转换成图片路径
     *
     * @param context
     * @param contentUri 图片Uri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri  == null){
            return "";
        }
        return contentUri.toString();
//        String res = null;
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
//        if (cursor.moveToFirst()) {
//            ;
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            res = cursor.getString(column_index);
//        }
//        cursor.close();
//        return res;
    }

    /**
     * 将图片转换成加密后的字符串
     *
     * @param path 图片路径
     * @return
     */
    public static String base64ForImage(String path) {
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(path));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream(102400 * 2);
            byte[] buffer = new byte[102400];
            int count;
            for (; 0 < (count = inputStream.read(buffer)); ) {
                outputStream.write(buffer, 0, count);
            }

            byte[] data = outputStream.toByteArray();

            String base64 = Base64.encodeToString(data, Base64.DEFAULT);
            return base64;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }

                if (null != outputStream) {
                    outputStream.close();
                }
            } catch (IOException e) {
                //do nothing
            }
        }
    }

    /**
     * 将下载的文件保存到本地
     *
     * @param bytes    服务端获取的文件数组
     * @param filePath 保存到本地的地址
     */
    public static void saveByteToFile(byte[] bytes, String filePath) {
        FileOutputStream fileOuputStream = null;
        try {
            fileOuputStream = new FileOutputStream(filePath);
            fileOuputStream.write(bytes);
            fileOuputStream.flush();
            fileOuputStream.close();
        } catch (Exception e) {
            System.out.print(e.getMessage());
        } finally {
            try {
                if (null != fileOuputStream) {
                    fileOuputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
