package com.wisape.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.wisape.android.WisapeApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                for (File file : fileList) {
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

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
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
        if (contentUri == null) {
            return "";
        }
        return contentUri.toString();
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
            return "";
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
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            fileOuputStream = new FileOutputStream(filePath);
            fileOuputStream.write(bytes);
            fileOuputStream.flush();
            fileOuputStream.close();
        } catch (Exception e) {
            LogUtil.e("保存字节到文件出错", e);
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

    /**
     * 复制assest目录下的文件
     *
     * @param context 上下文
     * @param src     源
     * @param dest    目标
     */
    public static void copyAssetsFile(Context context, String src, String dest) {
        InputStream in = null;
        OutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            in = context.getAssets().open(src);
            byte[] buffer = new byte[1024];
            int length = in.read(buffer);
            while (length > 0) {
                out.write(buffer, 0, length);
                length = in.read(buffer);
            }
            out.flush();
            in.close();
            out.close();
        } catch (IOException e) {

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {

                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {

                }
            }
        }
    }


    /**
     * 文件路径替换
     *
     * @param oldPath 需要被替换的路径
     * @param newPath 替换后的路径
     * @param file    要替换路径的文件
     */
    public static void replacePath(String oldPath, String newPath, File file) {
        String result = readFileToString(file).replace(oldPath, newPath);
        saveFile(result, file);
    }

    /**
     * 保存文件
     *
     * @param content 要保存的内容
     * @param file    保存到的文件
     */
    public static void saveFile(String content, File file) {
        BufferedWriter writer = null;
        StringBuilder local = new StringBuilder();
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file.getAbsoluteFile()), "UTF-8"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e1) {

                }
            }
        }
    }


    /**
     * 将文件转换为字符串
     *
     * @param file
     * @return
     */
    public static String readFileToString(File file) {
        BufferedReader reader = null;
        StringBuilder local = new StringBuilder();
        String line = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file.getAbsoluteFile()), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                local.append(line);
            }
            reader.close();
        } catch (IOException e) {

            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e1) {

                }
            }
        }
        return local.toString();
    }

    /*  解压assets的zip压缩文件到指定目录
     * @param context上下文对象
     * @param assetName压缩文件名
     * @param outputDirectory输出目录
     * @param isReWrite是否覆盖
     * @throws IOException
     */
    public static void unZip(Context context, String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        // 创建解压目标目录
        File file = new File(outputDirectory);
        // 如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 打开压缩文件
        InputStream inputStream = context.getAssets().open(assetName);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // 使用1Mbuffer
        byte[] buffer = new byte[1024 * 1024];
        // 解压时字节计数
        int count = 0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            // 如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者是文件不存在
                if (isReWrite || !file.exists()) {
                    file.mkdir();
                }
            } else {
                // 如果是文件
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者文件不存在，则解压文件
                if (isReWrite || !file.exists()) {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.close();
                }
            }
            // 定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 360, 680);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //保存bitmap到指定文件
    public static void saveBitmap(String filePath, Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
           fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            bitmap.recycle();
        } catch (IOException e) {
            LogUtil.e("保存bitmap失败!", e);
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LogUtil.e("关闭文件流出现错误:",e);
                }
            }
        }
    }

    //把bitmap转换成String
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * 传入字符串生成二维码
     * @param str
     * @return
     * @throws WriterException
     */
    public static Bitmap Create2DCode(String str) throws WriterException {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        DisplayMetrics mDisplayMetrics = WisapeApplication.getInstance().getResources().getDisplayMetrics();

        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, 400,400);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
