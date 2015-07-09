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

        File target;
        ZipArchiveOutputStream zipOutput = null;
        try{
            target = new File(targetDir,targetName);
            zipOutput = new ZipArchiveOutputStream(new FileOutputStream(target));
            zipOutput.setComment("wisape");
            zipOutput.setUseZip64(Zip64Mode.AsNeeded);
            zipOutput.setEncoding("UTF-8");
            zipDir(sourceFile, zipOutput);
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

    private static void zipDir(File dir, ZipArchiveOutputStream outputStream) throws IOException {
        if(dir.isFile()){
            zipSingleFile(dir, outputStream);
        }else if(dir.isDirectory()){
            String[] children = dir.list();
            int count = (null == children ? 0 : children.length);
            if(0 == count){
                return;
            }

            File file;
            for(String child : children){
                file = new File(child);
                if(file.isDirectory()){
                    outputStream.closeArchiveEntry();
                    zipDir(file, outputStream);
                }else if(file.isFile()){
                    zipSingleFile(file, outputStream);
                }
            }
        }
    }

    private static void zipSingleFile(File file, ZipArchiveOutputStream outputStream) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(file, file.getName());
        entry.setComment("wisape");
        entry.setTime(SystemClock.uptimeMillis());
        outputStream.putArchiveEntry(entry);

        InputStream input = new FileInputStream(file);
        IOUtils.copy(input, outputStream);
        outputStream.closeArchiveEntry();
        outputStream.flush();
    }

    public static Uri unzip(Uri source, File targetDir) throws IOException{
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
            for(;null != (entry = zipInput.getNextZipEntry());){
                entryName = entry.getName();
                if(entry.isDirectory()){
                    //TODO
                }else{
                    entryFile = new File(targetDir, entryName);
                    output = new FileOutputStream(entryFile);
                    for(; 0 < (count = zipInput.read(buffer, 0, bufferSize));){
                        output.write(buffer, 0, count);
                    }
                    output.flush();
                    output.close();
                }
            }
        }finally {
            if(null != zipInput){
                zipInput.close();
            }
        }
        return Uri.fromFile(targetDir);
    }
}
