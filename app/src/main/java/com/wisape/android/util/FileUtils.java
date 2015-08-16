package com.wisape.android.util;

import java.io.File;

/**
 * 文件操作工具
 * Created by hm on 2015/8/14.
 */
public class FileUtils {

    public static  void deleteFileInDir(File dir){
        deleteAllFile(dir.getPath());
        deleteAllFullFolder(dir.getPath());
    }

    /**
     * @param args
     */
    //删除文件夹下的所有文件
    private static boolean deleteAllFile(String folderFullPath){
        boolean ret = false;
        File file = new File(folderFullPath);
        if(file.exists()){
            if(file.isDirectory()){
                File[] fileList = file.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    String filePath = fileList[i].getPath();
                    deleteAllFile(filePath);
                }
            }
            if(file.isFile()){
                file.delete();
            }
        }
        return ret;
    }
    //删除文件夹下的所有空文件夹,注意必须是空文件夹,该方法可能一次不能全部删除,需在调用时多次执行
    private static boolean deleteAllFullFolder(String folderFullPath){
        boolean ret = false;
        File file = new File(folderFullPath);
        if(file.exists()){
            File[] fileList=file.listFiles();
            if(fileList.length>0){
                for(int i=0;i < fileList.length;i++){
                    String filePath = fileList[i].getPath();
                    deleteAllFullFolder(filePath);
                }
            }else{
                file.delete();
            }
        }
        return ret;
    }
}
