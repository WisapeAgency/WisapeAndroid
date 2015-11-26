package com.wisape.android.util;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by LeiGuoting on 9/7/15.
 */
public class ZipUtils{

    /**
     * @param source
     * @param targetDir
     * @param targetName
     * @return
     * @throws IOException
     */
    public static Uri zip(Uri source, File targetDir, String targetName) throws IOException{
        File sourceFile = new File(source.getPath());
        if(sourceFile.isFile()){
            return source;
        }

        Log.d("ZipUtils", "#zip source path:" + sourceFile.getPath());
        File target;
        ZipArchiveOutputStream zipOutput = null;
        try{
            target = new File(targetDir,targetName);
            zipOutput = new ZipArchiveOutputStream(new FileOutputStream(target));
            zipOutput.setComment("wisape");
            zipOutput.setUseZip64(Zip64Mode.AsNeeded);
            zipOutput.setEncoding("UTF-8");
            zipDir(sourceFile, sourceFile, zipOutput);
        }finally {
            try{
                if(null != zipOutput){
                    zipOutput.close();
                }
            }catch (IOException e){
                //do nothing
            }
        }
        Uri uri = Uri.fromFile(target);
        Log.d("ZipUtils", "#zip uri:" + uri);
        return uri;
    }

    private static void zipDir(File root, File file, ZipArchiveOutputStream outputStream) throws IOException {
        if(file.isFile()){
            zipSingleFile(root, file, outputStream);
        }else if(file.isDirectory()){
            String[] children = file.list();
            int count = (null == children ? 0 : children.length);
            if(0 == count){
                return;
            }

            Log.d("ZipUtils", "#zipDir count:" + count + ", dir:" + file.getName());
            File childFile;
            ZipArchiveEntry dirEntry;
            for(String child : children){
                Log.d("ZipUtils", "#zipDir child:" + child + ", dir:" + file.getName());
                childFile = new File(file, child);
                if(childFile.isDirectory()){
                    dirEntry = makeEntry(root, childFile);
                    outputStream.putArchiveEntry(dirEntry);
                    outputStream.closeArchiveEntry();

                    zipDir(root, childFile, outputStream);
                }else if(childFile.isFile()){
                    zipSingleFile(root, childFile, outputStream);
                }
            }
        }
    }

    private static void zipSingleFile(File root, File file, ZipArchiveOutputStream outputStream) throws IOException {
        ZipArchiveEntry entry = makeEntry(root, file);
        outputStream.putArchiveEntry(entry);

        InputStream input = null;
        try{
            input = new FileInputStream(file);
            IOUtils.copy(input, outputStream);
            outputStream.closeArchiveEntry();
            outputStream.flush();
        }finally {
            if(null != input){
                input.close();
            }
        }
    }

    private static ZipArchiveEntry makeEntry(File root, File file){
        String entryName = makeEntryName(root.getPath(), file.getPath());
        Log.d("ZipUtils", "#makeEntry file:" + entryName);
        ZipArchiveEntry entry = new ZipArchiveEntry(file, entryName);
        entry.setComment("wisape");
        entry.setTime(SystemClock.uptimeMillis());
        return entry;
    }


    private static String makeEntryName(String root, String path){
        return path.replace(root + File.separator, "");
    }

    public static Uri unzip(Uri source, File targetDir) throws IOException{
        LogUtil.d("开始解压压缩文件："+ source.getPath() + ":" + targetDir.getPath());
        ZipArchiveInputStream zipInput = null;
        try{
            zipInput = new ZipArchiveInputStream(new FileInputStream(new File(source.getPath())));
            ZipArchiveEntry entry;

            int count;
            String entryName;
            int bufferSize = 20480;
            byte[] buffer = new byte[bufferSize];

            File entryFile;
            OutputStream output;
            if(!targetDir.exists() || !targetDir.isDirectory()){
                targetDir.mkdirs();
            }

            for(;null != (entry = zipInput.getNextZipEntry());){
                entryName = entry.getName();
                if(entry.isDirectory()){
                    entryFile = new File(targetDir, entryName);
                    if(!entryFile.exists()){
                        entryFile.mkdirs();
                    }
                }else{
                    entryFile = new File(targetDir, entryName);
                    File parent = entryFile.getParentFile();
                    if(!parent.exists()){
                        parent.mkdirs();
                    }

                    if(!entryFile.exists()){
                        entryFile.createNewFile();
                    }

                    output = new FileOutputStream(entryFile);
                    for(; 0 < (count = zipInput.read(buffer, 0, bufferSize));){
                        output.write(buffer, 0, count);
                    }
                    output.flush();
                    output.close();
                }
            }
        }catch (IOException e) {
            LogUtil.e("zipUtils解压缩失败:"+source.getPath(),e);
        }finally {
            if(null != zipInput){
                try {
                    zipInput.close();
                }catch(IOException e){ }
            }
        }
        return Uri.fromFile(targetDir);
    }
}
